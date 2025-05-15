package vn.iostar.Project_Mobile.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iostar.Project_Mobile.DTO.CreateOrderRequest;
import vn.iostar.Project_Mobile.DTO.CreateOrderResponseDTO;
import vn.iostar.Project_Mobile.entity.*;
import vn.iostar.Project_Mobile.exception.ResourceNotFoundException;
import vn.iostar.Project_Mobile.repository.*;
import vn.iostar.Project_Mobile.service.IOrderService;
import vn.iostar.Project_Mobile.util.OrderStatus;
import vn.iostar.Project_Mobile.util.PaymentMethod;

import java.sql.Date; // Đảm bảo import đúng java.sql.Date
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final AddressRepository addressRepository;
    private final CartItemRepository cartItemRepository;
    private final IOrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final VnpayService vnpayService;
    private final ICommentRepository commentRepository;

    public OrderServiceImpl(AddressRepository addressRepository,
                            CartItemRepository cartItemRepository,
                            IOrderRepository orderRepository,
                            OrderLineRepository orderLineRepository,
                            ProductRepository productRepository,
                            CartRepository cartRepository,
                            VnpayService vnpayService,
                            ICommentRepository commentRepository) {
        this.addressRepository = addressRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderLineRepository = orderLineRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.vnpayService = vnpayService;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public CreateOrderResponseDTO createOrder(User currentUser, CreateOrderRequest request, HttpServletRequest httpServletRequest) {
        logger.info("Attempting to create order for user ID: {}", currentUser.getUserId());

        // --- VALIDATION ---
        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            logger.warn("Validation failed for user {}: Cart item IDs cannot be empty.", currentUser.getUserId());
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm để đặt hàng.");
        }
        if (request.getPaymentMethod() == null) {
            logger.warn("Validation failed for user {}: Payment method cannot be null.", currentUser.getUserId());
            throw new IllegalArgumentException("Vui lòng chọn phương thức thanh toán.");
        }
        logger.debug("Input validation passed for user {}. Payment method: {}", currentUser.getUserId(), request.getPaymentMethod().name());

        // --- LẤY ĐỊA CHỈ GIAO HÀNG MẶC ĐỊNH ---
        // Địa chỉ này sẽ được dùng để format thành chuỗi cho Order.shippingAddress
        Address shippingAddressEntity = addressRepository.findByUser_UserIdAndIsDefaultTrue(currentUser.getUserId())
                .orElseThrow(() -> {
                    logger.error("User {} does not have a default shipping address set.", currentUser.getUserId());
                    return new IllegalStateException("Vui lòng thiết lập địa chỉ giao hàng mặc định trước khi đặt hàng.");
                });
        logger.debug("Default address entity found for user {}: Address ID {}. Recipient: '{}', Phone: '{}', Street: '{}'",
                currentUser.getUserId(), shippingAddressEntity.getAddressId(),
                shippingAddressEntity.getRecipientName(), shippingAddressEntity.getRecipientPhone(), shippingAddressEntity.getStreetAddress());


        // --- KIỂM TRA GIỎ HÀNG VÀ SẢN PHẨM TRONG GIỎ HÀNG ---
        Optional<Cart> userCartOpt = cartRepository.findByUser_UserId(currentUser.getUserId());
        if (userCartOpt.isEmpty()) {
            logger.error("Cart not found for user {}.", currentUser.getUserId());
            throw new IllegalStateException("Không tìm thấy giỏ hàng của người dùng.");
        }
        Cart userCart = userCartOpt.get();
        logger.debug("Cart found for user {}: Cart ID {}", currentUser.getUserId(), userCart.getCartId());

        List<CartItem> selectedCartItems = cartItemRepository.findByCartItemIdInAndCart_CartId(request.getCartItemIds(), userCart.getCartId());
        if (selectedCartItems.size() != request.getCartItemIds().size()) {
            List<Long> foundIds = selectedCartItems.stream().map(CartItem::getCartItemId).collect(Collectors.toList());
            List<Long> missingIds = request.getCartItemIds().stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            logger.warn("Mismatch in cart items for user {}. Requested: {}, Found in cart: {}. Missing/Invalid IDs: {}",
                    currentUser.getUserId(), request.getCartItemIds(), foundIds, missingIds);
            throw new NoSuchElementException("Một hoặc nhiều sản phẩm trong giỏ hàng không hợp lệ hoặc không tìm thấy: " + missingIds);
        }
        logger.debug("Fetched {} selected cart items for user {}.", selectedCartItems.size(), currentUser.getUserId());


        // --- TÍNH TOÁN GIÁ TRỊ ĐƠN HÀNG VÀ TẠO ORDER LINES ---
        double itemsSubtotal = 0;
        List<OrderLine> tempOrderLines = new ArrayList<>(); // Danh sách tạm thời để lưu các OrderLine
        for (CartItem item : selectedCartItems) {
            Product product = item.getProduct();
            if (product == null) {
                logger.error("Product data is missing for CartItem ID: {} in Cart ID: {}", item.getCartItemId(), userCart.getCartId());
                throw new IllegalStateException("Lỗi dữ liệu: Sản phẩm không tồn tại cho mục trong giỏ hàng ID " + item.getCartItemId());
            }
            if (product.getQuantity() < item.getQuantity()) {
                logger.warn("Insufficient stock for Product ID: {} (Name: {}). Requested: {}, Available: {}",
                        product.getProductId(), product.getName(), item.getQuantity(), product.getQuantity());
                throw new IllegalStateException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho (yêu cầu " + item.getQuantity() + ", còn " + product.getQuantity() + ").");
            }
            double productPriceAtOrderTime = product.getPrice(); // Lấy giá sản phẩm tại thời điểm đặt hàng
            itemsSubtotal += productPriceAtOrderTime * item.getQuantity();

            OrderLine line = new OrderLine();
            line.setProduct(product);
            line.setQuantity(item.getQuantity());
            line.setPrice(productPriceAtOrderTime); // Lưu giá tại thời điểm đặt hàng vào OrderLine
            // line.setOrder() sẽ được thực hiện sau khi Order được lưu lần đầu
            tempOrderLines.add(line);
        }
        logger.debug("Calculated itemsSubtotal for user {}: {}", currentUser.getUserId(), itemsSubtotal);

        int totalItemsCount = selectedCartItems.stream().mapToInt(CartItem::getQuantity).sum();
        double calculatedShippingFee = calculateShippingFee(shippingAddressEntity, totalItemsCount, itemsSubtotal);
        logger.debug("Calculated shipping fee for user {}: {}", currentUser.getUserId(), calculatedShippingFee);
        double finalTotalPrice = itemsSubtotal + calculatedShippingFee; // Cần trừ discount nếu có
        logger.debug("Calculated finalTotalPrice for user {}: {}", currentUser.getUserId(), finalTotalPrice);


        // --- TẠO ĐỐI TƯỢNG ORDER ---
        Order newOrder = new Order();
        newOrder.setUser(currentUser);
        newOrder.setItemsSubtotal(itemsSubtotal);
        newOrder.setTotalPrice(finalTotalPrice);
        // Sử dụng phương thức formatShippingAddress đã được sửa đổi để tạo chuỗi cho Order.shippingAddress
        newOrder.setShippingAddress(formatShippingAddressToString(shippingAddressEntity));
        Instant now = Instant.now();
        newOrder.setOrderDate(Date.from(now)); // Chuyển Instant sang java.util.Date nếu Order entity dùng nó
        Instant predictDate = now.plus(5, ChronoUnit.DAYS);
        newOrder.setPredictReceiveDate(Date.from(predictDate)); // Chuyển đổi
        newOrder.setPaymentMethod(request.getPaymentMethod());
        newOrder.setReviewed(false); // Mặc định là chưa review


        // --- XỬ LÝ THEO PHƯƠNTHỨC THANH TOÁN ---
        String paymentUrl = null;
        Order savedOrder; // Khai báo ở đây để có thể truy cập sau khối if/else

        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            newOrder.setStatus(OrderStatus.PENDING); // Trạng thái chờ thanh toán VNPAY
            logger.debug("Payment method is VNPAY. Setting status to PENDING for user {}.", currentUser.getUserId());
            // Lưu Order trước để có Order ID cho VNPAY và gán cho OrderLine
            savedOrder = orderRepository.save(newOrder);
            logger.info("PENDING Order (VNPAY) initially saved with ID: {} for user {}", savedOrder.getOrderId(), currentUser.getUserId());

            // Tạo và lưu các OrderLine, liên kết với Order đã lưu
            List<OrderLine> finalOrderLines = new ArrayList<>();
            for (OrderLine line : tempOrderLines) {
                line.setOrder(savedOrder); // Gán Order đã lưu cho từng OrderLine
                OrderLine savedLine = orderLineRepository.save(line);
                finalOrderLines.add(savedLine);
            }
            savedOrder.setOrderLines(finalOrderLines); // Gán danh sách OrderLine hoàn chỉnh vào Order
            // Không cần lưu lại savedOrder ở đây nếu cascade hoạt động đúng, nhưng nếu muốn chắc chắn:
            // orderRepository.save(savedOrder); // Có thể không cần thiết

            try {
                paymentUrl = vnpayService.createPaymentUrl(savedOrder, httpServletRequest);
                logger.info("Generated VNPAY URL for pending order {}: {}", savedOrder.getOrderId(), paymentUrl);
            } catch (Exception e) {
                // Quan trọng: Nếu không tạo được URL thanh toán, đơn hàng PENDING này cần xử lý.
                // Có thể rollback, hoặc để trạng thái PENDING nhưng ghi log lỗi rõ ràng.
                logger.error("Error generating VNPAY URL for order {}: {}", savedOrder.getOrderId(), e.getMessage(), e);
                // throw new RuntimeException("Không thể tạo URL thanh toán VNPAY.", e); // Hoặc xử lý khác
            }
            // Không cập nhật tồn kho hay xóa giỏ hàng cho VNPAY ở đây, chờ IPN xác nhận thanh toán thành công.
            // Không reset review ở đây cho VNPAY.

        } else { // COD hoặc các phương thức thanh toán khác
            newOrder.setStatus(OrderStatus.WAITING); // Trạng thái chờ xử lý/đóng gói
            logger.debug("Payment method is {}. Setting status to {} for user {}.", request.getPaymentMethod().name(), newOrder.getStatus().name(), currentUser.getUserId());
            // Lưu Order
            savedOrder = orderRepository.save(newOrder);
            logger.info("Order (Payment Method: {}) saved successfully with ID: {} for user {}", request.getPaymentMethod().name(), savedOrder.getOrderId(), currentUser.getUserId());

            // Tạo và lưu các OrderLine, liên kết với Order đã lưu
            List<OrderLine> finalOrderLines = new ArrayList<>();
            for (OrderLine line : tempOrderLines) {
                line.setOrder(savedOrder);
                OrderLine savedLine = orderLineRepository.save(line);
                finalOrderLines.add(savedLine);

                // Cập nhật tồn kho sản phẩm (chỉ khi đơn hàng được xác nhận, ví dụ COD)
                Product productToUpdate = line.getProduct();
                int newStock = productToUpdate.getQuantity() - line.getQuantity();
                if (newStock < 0) {
                    // Đây là lỗi nghiêm trọng, nên rollback giao dịch
                    logger.error("Stock calculation error after saving OrderLine! Product ID: {}, New Stock calculated: {}. Order creation will be rolled back.", productToUpdate.getProductId(), newStock);
                    throw new IllegalStateException("Lỗi nghiêm trọng: Số lượng tồn kho không đủ cho sản phẩm '" + productToUpdate.getName() + "' sau khi kiểm tra lại.");
                }
                productToUpdate.setQuantity(newStock);
                productRepository.save(productToUpdate);
                logger.debug("Updated stock for Product ID: {}. New stock: {}", productToUpdate.getProductId(), newStock);
            }
            savedOrder.setOrderLines(finalOrderLines);
            // orderRepository.save(savedOrder); // Có thể không cần thiết

            // Xóa các mục đã chọn khỏi giỏ hàng
            cartItemRepository.deleteAll(selectedCartItems);
            logger.info("Deleted {} cart items for user {} after order creation (ID: {}).", selectedCartItems.size(), currentUser.getUserId(), savedOrder.getOrderId());

            // Reset trạng thái review của các sản phẩm trong đơn hàng mới cho user này
            resetPreviousProductReviews(currentUser, savedOrder);
        }

        return new CreateOrderResponseDTO(savedOrder, paymentUrl);
    }


    private void resetPreviousProductReviews(User user, Order order) {
        if (order != null && order.getOrderLines() != null) {
            logger.info("Attempting to reset previous reviews for user ID: {} for products in Order ID: {}", user.getUserId(), order.getOrderId());
            for (OrderLine ol : order.getOrderLines()) {
                Product productInOrder = ol.getProduct();
                if (productInOrder != null) {
                    // Tìm tất cả các Comment cũ của user cho productInOrder mà có reviewed = true
                    List<Comment> previousReviews = commentRepository.findByUserAndProductAndReviewedIsTrue(user, productInOrder);
                    if (!previousReviews.isEmpty()) {
                        logger.debug("Found {} previous review(s) to reset for User ID: {} and Product ID: {}",
                                previousReviews.size(), user.getUserId(), productInOrder.getProductId());
                        for (Comment oldReview : previousReviews) {
                            oldReview.setReviewed(false); // Set reviewed = false
                            commentRepository.save(oldReview);
                            logger.info("Reset 'reviewed' flag to false for old Comment ID: {} for User ID: {} and Product ID: {}",
                                    oldReview.getCommentId(), user.getUserId(), productInOrder.getProductId());
                        }
                    } else {
                        logger.debug("No previous reviews with reviewed=true found to reset for User ID: {} and Product ID: {}",
                                user.getUserId(), productInOrder.getProductId());
                    }
                }
            }
        }
    }


    private Double calculateShippingFee(Address address, int totalItems, double itemsSubtotal) {
        // Logic tính phí ship của bạn
        if (totalItems == 0) return 0.0;
        if (itemsSubtotal > 700000) return 0.0; // Ví dụ: Miễn phí ship cho đơn > 700k
        if (totalItems <= 3) return 15000.0;
        if (totalItems <= 7) return 25000.0;
        return 35000.0;
    }


    /**
     * Formats the shipping address entity into a pipe-separated string:
     * "Recipient Name|Recipient Phone|Street Address, Ward, District, City"
     * Handles null or empty fields gracefully.
     *
     * @param address The Address entity.
     * @return A formatted string.
     */
    private String formatShippingAddressToString(Address address) {
        if (address == null) {
            logger.warn("Cannot format shipping address and recipient info because Address object is null. Returning empty placeholders.");
            return "||"; // Trả về chuỗi với 3 phần rỗng
        }

        // Phần 1: Tên người nhận
        String recipientName = (address.getRecipientName() != null && !address.getRecipientName().trim().isEmpty())
                ? address.getRecipientName().trim() : "";

        // Phần 2: Số điện thoại người nhận
        String recipientPhone = (address.getRecipientPhone() != null && !address.getRecipientPhone().trim().isEmpty())
                ? address.getRecipientPhone().trim() : "";

        // Phần 3: Địa chỉ chi tiết (gộp street, ward, district, city)
        StringBuilder detailedAddressBuilder = new StringBuilder();
        if (address.getStreetAddress() != null && !address.getStreetAddress().trim().isEmpty()) {
            detailedAddressBuilder.append(address.getStreetAddress().trim());
        }
        if (address.getWard() != null && !address.getWard().trim().isEmpty()) {
            if (detailedAddressBuilder.length() > 0) detailedAddressBuilder.append(", ");
            detailedAddressBuilder.append(address.getWard().trim());
        }
        if (address.getDistrict() != null && !address.getDistrict().trim().isEmpty()) {
            if (detailedAddressBuilder.length() > 0) detailedAddressBuilder.append(", ");
            detailedAddressBuilder.append(address.getDistrict().trim());
        }
        if (address.getCity() != null && !address.getCity().trim().isEmpty()) {
            if (detailedAddressBuilder.length() > 0) detailedAddressBuilder.append(", ");
            detailedAddressBuilder.append(address.getCity().trim());
        }
        String detailedAddress = detailedAddressBuilder.toString();
        if (detailedAddress.isEmpty() && recipientName.isEmpty() && recipientPhone.isEmpty()){
             logger.warn("All parts of address are empty for Address ID {}. Returning empty placeholders.", address.getAddressId());
             return "||"; // Nếu tất cả đều rỗng, trả về placeholder
        }


        String result = String.join("|", recipientName, recipientPhone, detailedAddress);
        logger.debug("Formatted shipping string for address ID {}: '{}'", address.getAddressId(), result);
        return result;
    }


    @Override
    public Order getOrderDetailsById(Long orderId, User user) {
        logger.debug("Fetching order details for ID: {} requested by User ID: {}", orderId, user.getUserId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order not found for ID: {}", orderId);
                    return new NoSuchElementException("Không tìm thấy đơn hàng với ID: " + orderId);
                });

        // Kiểm tra quyền truy cập
        if (!order.getUser().getUserId().equals(user.getUserId())) {
             logger.warn("Forbidden access attempt: User {} tried to access order {} owned by user {}",
                        user.getUserId(), orderId, order.getUser().getUserId());
            throw new IllegalStateException("Bạn không có quyền xem đơn hàng này.");
        }

        // Log giá trị shippingAddress từ DB
        logger.info("SHIPPING_ADDRESS_FROM_DB for order ID {}: '{}'", orderId, order.getShippingAddress());

        // Eagerly fetch OrderLines and associated Product names (nếu cần và chưa được fetch)
        // Điều này giúp tránh LazyInitializationException nếu client cố gắng truy cập sau khi session đóng
        order.getOrderLines().forEach(line -> {
            if (line.getProduct() != null) {
                // Chỉ cần truy cập một thuộc tính để trigger fetch, ví dụ getName()
                // Không cần gán lại hay làm gì khác.
                line.getProduct().getName();
            }
        });
        logger.debug("Order details (with eager-loaded lines/products) retrieved successfully for Order ID: {}", orderId);
        return order;
    }


    @Transactional
    @Override
    public void handleVnpayIpn(Map<String, String> vnpayData, HttpServletRequest request) {
         logger.info("Received VNPAY IPN callback with data: {}", vnpayData);
         try {
             boolean isValidSignature = vnpayService.verifyIpnSignature(vnpayData, request);
             if (!isValidSignature) {
                  logger.warn("VNPAY IPN signature verification failed.");
                  // Quan trọng: Không ném lỗi nếu muốn VNPAY coi là đã nhận được IPN.
                  // Thay vào đó, ghi log và có thể không xử lý đơn hàng.
                  // Hoặc, nếu nghiệp vụ yêu cầu phải xác thực chữ ký, thì ném lỗi.
                  // Hiện tại: ném lỗi để Controller trả về lỗi cho VNPAY (ví dụ: RspCode 97)
                  throw new IllegalArgumentException("VNPAY IPN signature verification failed.");
             }
             logger.debug("VNPAY IPN signature is valid.");

             Long orderId = Long.parseLong(vnpayData.get("vnp_TxnRef"));
             String vnp_ResponseCode = vnpayData.get("vnp_ResponseCode");
             String vnp_TransactionStatus = vnpayData.get("vnp_TransactionStatus"); // Trạng thái giao dịch của VNPAY
             long vnp_AmountReported = Long.parseLong(vnpayData.get("vnp_Amount")); // Số tiền VNPAY báo về (VNPAY gửi số tiền * 100)

             Optional<Order> orderOpt = orderRepository.findById(orderId);
             if (orderOpt.isEmpty()) {
                 logger.warn("VNPAY IPN: Order not found for ID: {}. RspCode: 01", orderId);
                 // Không ném lỗi ở đây để VNPAY coi là IPN đã được xử lý.
                 // Nếu bạn muốn VNPAY thử lại, hãy ném lỗi hoặc không trả về "00" ở cuối.
                 // Tuy nhiên, nếu đơn hàng không tồn tại, thường không có gì để VNPAY thử lại.
                 // Trả về lỗi cho VNPAY (thông qua controller) để VNPAY ghi nhận.
                 throw new NoSuchElementException("Không tìm thấy đơn hàng với ID: " + orderId + " (RspCode: 01)");
             }
             Order order = orderOpt.get();

             // Kiểm tra xem đơn hàng đã được xử lý IPN trước đó chưa
             if (order.getStatus() != OrderStatus.PENDING) {
                 logger.warn("VNPAY IPN: Order ID {} already processed or in invalid state. Current status: {}. Expected PENDING. RspCode: 02", orderId, order.getStatus());
                 // Đơn hàng đã được xử lý, không cần làm gì thêm.
                 // Nếu muốn VNPAY không gửi lại, Controller cần trả về "00"
                 // Nếu ném lỗi ở đây, VNPAY có thể hiểu là xử lý thất bại và gửi lại.
                 // Hiện tại: Ném lỗi để báo cho VNPAY biết là có vấn đề.
                 throw new IllegalStateException("Đơn hàng " + orderId + " không ở trạng thái chờ thanh toán (RspCode: 02). Trạng thái hiện tại: " + order.getStatus().name());
             }

             // Kiểm tra số tiền (VNPAY trả về amount * 100)
             long orderAmountInSystemTimes100 = (long) (order.getTotalPrice() * 100);
             if (vnp_AmountReported != orderAmountInSystemTimes100) {
                  logger.warn("VNPAY IPN amount mismatch for Order ID {}. VNPAY Amount (x100): {}, System Amount (x100): {}. RspCode: 04",
                          orderId, vnp_AmountReported, orderAmountInSystemTimes100);
                  throw new IllegalStateException("Số tiền thanh toán không khớp cho đơn hàng " + orderId + " (RspCode: 04)");
             }
             logger.debug("Order {} status, amount, and signature are valid for IPN processing.", orderId);

             if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) { // Giao dịch thành công
                 logger.info("VNPAY IPN: Payment successful for Order ID: {}", orderId);
                 order.setStatus(OrderStatus.WAITING); // Chuyển sang trạng thái chờ xử lý/đóng gói

                 // --- THỰC HIỆN CÁC HÀNH ĐỘNG KHI THANH TOÁN VNPAY THÀNH CÔNG ---
                 // 1. Cập nhật tồn kho sản phẩm
                 for (OrderLine line : order.getOrderLines()) {
                     Product productToUpdate = line.getProduct();
                     // Re-fetch product để đảm bảo dữ liệu mới nhất và tránh optimistic locking nếu có
                     Optional<Product> freshProductOpt = productRepository.findById(productToUpdate.getProductId());
                     if(freshProductOpt.isPresent()){
                         Product freshProduct = freshProductOpt.get();
                         int newStock = freshProduct.getQuantity() - line.getQuantity();
                         if (newStock < 0) {
                             logger.error("VNPAY IPN - Stock error! Product ID: {}, New Stock calculated: {}. Order cannot be fulfilled despite successful payment. Marking order as ERROR.",
                                     freshProduct.getProductId(), newStock);
                             // Đây là trường hợp xấu: tiền đã nhận nhưng không đủ hàng.
                             order.setStatus(OrderStatus.ERROR); // Đánh dấu đơn hàng có vấn đề
                             // Không ném lỗi ở đây để IPN vẫn được ghi nhận là "đã xử lý" bởi VNPAY (Controller trả về 00)
                             // nhưng cần thông báo cho quản trị viên.
                             break; // Dừng cập nhật stock cho các sản phẩm khác nếu một sản phẩm lỗi
                         }
                         freshProduct.setQuantity(newStock);
                         productRepository.save(freshProduct);
                         logger.debug("VNPAY IPN - Updated stock for Product ID: {}. New stock: {}", freshProduct.getProductId(), newStock);
                     } else {
                          logger.error("VNPAY IPN - Product ID {} not found during stock update for order {}. Marking order as ERROR.", productToUpdate.getProductId(), orderId);
                          order.setStatus(OrderStatus.ERROR); // Đánh dấu đơn hàng có vấn đề
                          break;
                     }
                 }

                 // 2. Xóa các CartItem đã được đặt hàng khỏi giỏ hàng (chỉ khi không có lỗi stock)
                 if (order.getStatus() != OrderStatus.ERROR) {
                     Cart userCart = cartRepository.findByUser_UserId(order.getUser().getUserId())
                             .orElse(null); // Không ném lỗi nếu không tìm thấy giỏ hàng
                     if (userCart != null) {
                         List<Long> productIdsInOrder = order.getOrderLines().stream()
                                 .map(ol -> ol.getProduct().getProductId())
                                 .collect(Collectors.toList());
                         // Cần cẩn thận nếu một sản phẩm có thể có nhiều cartItem (ví dụ: kích thước khác nhau)
                         // Nếu cartItemIds được lưu khi tạo order PENDING thì có thể dùng lại.
                         // Hiện tại, giả sử xóa dựa trên product ID trong giỏ hàng của user.
                         List<CartItem> itemsToDelete = cartItemRepository.findByCart_CartIdAndProduct_ProductIdIn(userCart.getCartId(), productIdsInOrder);
                         if (!itemsToDelete.isEmpty()) {
                             cartItemRepository.deleteAll(itemsToDelete);
                             logger.info("VNPAY IPN - Deleted {} cart items for user {} after successful payment for order {}.",
                                     itemsToDelete.size(), order.getUser().getUserId(), orderId);
                         }
                     }
                     // Reset trạng thái review của các sản phẩm trong đơn hàng này cho user này
                     resetPreviousProductReviews(order.getUser(), order);
                 }

             } else { // Giao dịch thất bại hoặc bị hủy bởi người dùng trên cổng VNPAY
                 logger.warn("VNPAY IPN: Payment failed/cancelled for Order ID: {}. VNPAY Response Code: {}, Transaction Status: {}",
                         orderId, vnp_ResponseCode, vnp_TransactionStatus);
                 order.setStatus(OrderStatus.CANCELLED); // Hoặc một trạng thái khác như PAYMENT_FAILED
             }

             orderRepository.save(order); // Lưu lại trạng thái cuối cùng của đơn hàng
             logger.info("Order status updated to {} for Order ID {} via VNPAY IPN.", order.getStatus().name(), orderId);

             // Không return gì ở đây vì đây là void method. Controller sẽ xử lý việc trả response cho VNPAY.

         } catch (NumberFormatException e) { // Lỗi parse ID đơn hàng hoặc số tiền từ VNPAY
             logger.error("Error parsing VNPAY IPN data (Order ID or Amount). RspCode: 99 (Generic error)", e);
             throw new NumberFormatException("Error parsing VNPAY IPN data: " + e.getMessage()); // Controller sẽ bắt và trả về RspCode 99
         } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
             // Các lỗi này đã được log với RspCode cụ thể ở trên. Ném lại để Controller xử lý.
             logger.warn("Business rule violation or data issue during VNPAY IPN processing: {}. Propagating to controller.", e.getMessage());
             throw e;
         } catch (Exception e) { // Các lỗi không mong muốn khác
              logger.error("Unexpected error processing VNPAY IPN for order data (TxnRef: {}). RspCode: 99", vnpayData.get("vnp_TxnRef"), e);
              // Ném lỗi runtime để Controller bắt và trả về RspCode 99 (lỗi chung)
              throw new RuntimeException("Unexpected error processing VNPAY IPN: " + e.getMessage(), e);
         }
     }


    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        logger.debug("Fetching orders for User ID: {}", userId);
        List<Order> orders = orderRepository.findByUser_UserIdOrderByOrderDateDesc(userId);
        // Eagerly fetch collections if needed (Ví dụ)
        orders.forEach(order -> {
            order.getOrderLines().size(); // Trigger load OrderLines
            // Nếu bạn muốn cả Product details, có thể làm sâu hơn:
            // order.getOrderLines().forEach(ol -> { if(ol.getProduct() != null) ol.getProduct().getName(); });
        });
        logger.info("Found {} orders for User ID: {}", orders.size(), userId);
        return orders;
    }


    @Override
    @Transactional(readOnly = true) // Đảm bảo transaction cho việc lazy loading nếu cần
    public Order getOrderDetailsById(Long orderId) throws ResourceNotFoundException {
        logger.debug("Fetching public order details for Order ID: {}", orderId);
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            // Đảm bảo các collection được tải nếu chúng là LAZY và client cần
            order.getOrderLines().size(); // Trigger fetch OrderLines
            for (OrderLine line : order.getOrderLines()) {
                if (line.getProduct() != null) {
                    line.getProduct().getName(); // Trigger fetch Product
                }
            }
            if (order.getUser() != null) { // Trigger fetch User
                order.getUser().getFullName();
            }
            logger.debug("Public order details retrieved for Order ID: {}", orderId);
            return order;
        } else {
            logger.warn("Public order details: Order not found for ID: {}", orderId);
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId);
        }
    }


    @Override
    @Transactional
    public Order cancelOrder(Long orderId) throws ResourceNotFoundException, IllegalStateException {
        logger.info("Attempting to cancel order with ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Cancel failed: Order not found with ID: {}", orderId);
                    return new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId);
                });

        // Kiểm tra trạng thái cho phép hủy
        if (order.getStatus() != OrderStatus.WAITING && order.getStatus() != OrderStatus.PENDING) {
            logger.warn("Cancel failed: Order ID {} cannot be cancelled. Current status: {}", orderId, order.getStatus());
            throw new IllegalStateException("Không thể hủy đơn hàng này. Trạng thái hiện tại: " + order.getStatus());
        }

        // Nếu là đơn WAITING (COD đã trừ kho), cần hoàn lại tồn kho.
        // Đơn PENDING (VNPAY chưa thanh toán) thì không cần hoàn kho vì kho chưa bị trừ.
        if (order.getStatus() == OrderStatus.WAITING) {
            logger.info("Order ID {} is WAITING, restoring product stock.", orderId);
            for (OrderLine line : order.getOrderLines()) {
                Product productToRestore = line.getProduct();
                 // Re-fetch để tránh stale data và đảm bảo an toàn
                 Optional<Product> freshProductOpt = productRepository.findById(productToRestore.getProductId());
                 if(freshProductOpt.isPresent()){
                     Product freshProduct = freshProductOpt.get();
                     int newStock = freshProduct.getQuantity() + line.getQuantity();
                     freshProduct.setQuantity(newStock);
                     productRepository.save(freshProduct);
                     logger.debug("Restored stock for Product ID: {}. New stock: {}", freshProduct.getProductId(), newStock);
                 } else {
                     // Lỗi này không nên xảy ra nếu dữ liệu nhất quán.
                     logger.error("Error restoring stock: Product ID {} not found for order {}. Stock not restored for this item.",
                             productToRestore.getProductId(), orderId);
                     // Cân nhắc: Có nên ném lỗi và rollback không? Hay chỉ log và tiếp tục hủy đơn?
                     // Hiện tại: Log và tiếp tục.
                 }
            }
        } else if (order.getStatus() == OrderStatus.PENDING) {
             logger.info("Order ID {} is PENDING (VNPAY), no stock restoration needed as stock was not deducted.", orderId);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        logger.info("Order ID: {} has been cancelled successfully.", orderId);
        return savedOrder;
    }
}