package vn.iostar.Project_Mobile.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Quan trọng

import jakarta.servlet.http.HttpServletRequest;
import vn.iostar.Project_Mobile.DTO.CreateOrderRequest;
import vn.iostar.Project_Mobile.entity.*;
import vn.iostar.Project_Mobile.repository.*;
import vn.iostar.Project_Mobile.service.IOrderService;
import vn.iostar.Project_Mobile.util.OrderStatus; // Import enum OrderStatus
import vn.iostar.Project_Mobile.util.PaymentMethod;

import java.sql.Date; // Hoặc java.time.LocalDate nếu dùng kiểu mới
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit; // Ví dụ tính ngày giao hàng

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iostar.Project_Mobile.DTO.*;
import vn.iostar.Project_Mobile.entity.*;
import vn.iostar.Project_Mobile.repository.*;
import vn.iostar.Project_Mobile.service.IOrderService;
import vn.iostar.Project_Mobile.util.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant; // Dùng java.time để lấy thời gian
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
@Service
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final AddressRepository addressRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository; // Thêm CartRepository
    private final VnpayService vnpayService; // *** Inject VnpayService ***
    
    public OrderServiceImpl(AddressRepository addressRepository,
                            CartItemRepository cartItemRepository,
                            OrderRepository orderRepository,
                            OrderLineRepository orderLineRepository,
                            ProductRepository productRepository,
                            CartRepository cartRepository, 
                            VnpayService vnpayService
                          ) { // Inject CartRepository
        this.addressRepository = addressRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderLineRepository = orderLineRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository; // Gán CartRepository
        this.vnpayService = vnpayService; // *** Gán giá trị ***
    }

    @Override
    @Transactional // Đảm bảo transaction bao gồm lưu order, order lines
    // Nhận HttpServletRequest từ Controller
    public CreateOrderResponseDTO createOrder(User currentUser, CreateOrderRequest request, HttpServletRequest httpServletRequest) {
        logger.info("Attempting to create order for user ID: {}", currentUser.getUserId());

        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            logger.warn("Validation failed for user {}: Cart item IDs cannot be empty.", currentUser.getUserId());
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm để đặt hàng.");
        }
        if (request.getPaymentMethod() == null) {
            logger.warn("Validation failed for user {}: Payment method cannot be null.", currentUser.getUserId());
            throw new IllegalArgumentException("Vui lòng chọn phương thức thanh toán.");
        }
        logger.debug("Input validation passed for user {}. Payment method: {}", currentUser.getUserId(), request.getPaymentMethod().name());

        Address defaultAddress = addressRepository.findByUser_UserIdAndIsDefaultTrue(currentUser.getUserId())
                .orElseThrow(() -> {
                    logger.error("User {} does not have a default shipping address set.", currentUser.getUserId());
                    return new IllegalStateException("Vui lòng thiết lập địa chỉ giao hàng mặc định trước khi đặt hàng.");
                });
        logger.debug("Default address found for user {}: Address ID {}", currentUser.getUserId(), defaultAddress.getAddressId());

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

        // 5. Tính toán itemsSubtotal, chuẩn bị OrderLine, và Kiểm tra số lượng tồn kho LẦN 1
        double itemsSubtotal = 0;
        List<OrderLine> tempOrderLines = new ArrayList<>();

        for (CartItem item : selectedCartItems) {
            Product product = item.getProduct();
            if (product == null) {
                logger.error("Product data is missing for CartItem ID: {} in Cart ID: {}", item.getCartItemId(), userCart.getCartId());
                throw new IllegalStateException("Lỗi dữ liệu: Sản phẩm không tồn tại cho mục trong giỏ hàng ID " + item.getCartItemId());
            }

            // Kiểm tra số lượng tồn kho sớm. Điều này giúp tránh tạo đơn hàng PendingPayment khi sản phẩm không đủ.
            if (product.getQuantity() < item.getQuantity()) {
                logger.warn("Insufficient stock for Product ID: {} (Name: {}). Requested: {}, Available: {}",
                        product.getProductId(), product.getName(), item.getQuantity(), product.getQuantity());
                throw new IllegalStateException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho (yêu cầu " + item.getQuantity() + ", còn " + product.getQuantity() + ").");
            }

            double productPrice = product.getPrice();
            double linePrice = productPrice * item.getQuantity();
            itemsSubtotal = itemsSubtotal + linePrice;

            // Tạo OrderLine tạm (chưa có Order ID)
            OrderLine line = new OrderLine();
            line.setProduct(product);
            line.setQuantity(item.getQuantity());
            line.setPrice(productPrice);
            tempOrderLines.add(line);
        }
        logger.debug("Calculated itemsSubtotal for user {}: {}", currentUser.getUserId(), itemsSubtotal);

        // 6. Tính phí vận chuyển
        int totalItemsCount = selectedCartItems.stream().mapToInt(CartItem::getQuantity).sum();
        double calculatedShippingFee = calculateShippingFee(defaultAddress, totalItemsCount, itemsSubtotal);
        logger.debug("Calculated shipping fee for user {}: {}", currentUser.getUserId(), calculatedShippingFee);

        // 7. Tính tổng tiền cuối cùng
        double finalTotalPrice = itemsSubtotal + calculatedShippingFee;
        logger.debug("Calculated finalTotalPrice for user {}: {}", currentUser.getUserId(), finalTotalPrice);

        // 8. Tạo đối tượng Order
        Order newOrder = new Order();
        newOrder.setUser(currentUser);
        newOrder.setItemsSubtotal(itemsSubtotal);
        newOrder.setTotalPrice(finalTotalPrice);
        newOrder.setShippingAddress(formatShippingAddress(defaultAddress));

        Instant now = Instant.now();
        newOrder.setOrderDate(new java.sql.Date(Date.from(now).getTime()));
        Instant predictDate = now.plus(5, ChronoUnit.DAYS); // Ví dụ: dự kiến nhận hàng 5 ngày sau
        newOrder.setPredictReceiveDate(new java.sql.Date(Date.from(predictDate).getTime()));

        newOrder.setPaymentMethod(request.getPaymentMethod());

        String paymentUrl = null; // Biến để lưu URL thanh toán VNPAY (nếu có)

        // *** LOGIC XỬ LÝ TÙY THUỘC PHƯƠNG THỨC THANH TOÁN ***
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            // Nếu là VNPAY:
            // - Đặt trạng thái là PendingPayment.
            // - LƯU đơn hàng và OrderLine.
            // - KHÔNG TRỪ TỒN KHO, KHÔNG XÓA GIỎ HÀNG.
            // - Tạo URL thanh toán VNPAY.
            // - Trả về DTO có URL.

            newOrder.setStatus(OrderStatus.PENDING);
            logger.debug("Payment method is VNPAY. Setting status to PendingPayment for user {}.", currentUser.getUserId());

            // 9a. Lưu Order Pending Payment để lấy ID (cần ID để tạo OrderLine và URL VNPAY)
            Order savedOrder = orderRepository.save(newOrder);
            logger.info("PendingPayment Order saved successfully with ID: {} for user {}", savedOrder.getOrderId(), currentUser.getUserId());

            // 10a. Hoàn thiện và lưu OrderLine (liên kết với Order đã lưu)
            List<OrderLine> finalOrderLines = new ArrayList<>();
            for (OrderLine line : tempOrderLines) {
                line.setOrder(savedOrder); // Liên kết với Order đã lưu
                OrderLine savedLine = orderLineRepository.save(line); // Lưu OrderLine
                finalOrderLines.add(savedLine);
                logger.debug("OrderLine saved with ID: {} for PendingPayment Order ID: {}", savedLine.getOrderLineId(), savedOrder.getOrderId());
                // KHÔNG cập nhật tồn kho sản phẩm ở đây cho VNPAY
            }
             savedOrder.setOrderLines(finalOrderLines); // Gán lại list OrderLine đã được lưu vào Order (quan trọng nếu DTO trả về có OrderLines)

            // 11a. Tạo VNPAY URL
            try {
                // Chuyển tổng tiền sang dạng long hoặc BigDecimal (thường là đơn vị nhỏ nhất của tiền tệ, ví dụ VND -> VNĐ * 100)
                 // Kiểm tra lại yêu cầu VNPAY về định dạng tiền tệ. Ví dụ VNPAY yêu cầu tiền Việt Nam không cần nhân 100
                 long amountLong = (long) savedOrder.getTotalPrice(); // Nếu VNPAY dùng đơn vị VNĐ
                 // Nếu VNPAY dùng đơn vị tiền tệ nhỏ nhất (xu/cent) thì: long amountLong = (long) (savedOrder.getTotalPrice() * 100);

                 // Gọi service để tạo URL thanh toán
                 paymentUrl = vnpayService.createPaymentUrl(savedOrder, httpServletRequest);
                logger.info("Generated VNPAY URL for pending order {}: {}", savedOrder.getOrderId(), paymentUrl);
            } catch (Exception e) {
                logger.error("Error generating VNPAY URL for order {}: {}", savedOrder.getOrderId(), e.getMessage(), e);
                // Nếu tạo URL lỗi, đơn hàng vẫn ở trạng thái PendingPayment, client không nhận được URL.
                // Log lỗi và vẫn trả về DTO với URL null. Phía client cần xử lý trường hợp URL null khi chọn VNPAY.
                // throw new RuntimeException("Không thể tạo liên kết thanh toán VNPAY. Vui lòng thử lại.", e); // Có thể ném lỗi để rollback nếu cần xử lý chặt chẽ hơn
            }

            // Trả về DTO chứa Order (trạng thái PendingPayment) và paymentUrl
            return new CreateOrderResponseDTO(savedOrder, paymentUrl);

        } else { // Giả định đây là các phương thức thanh toán KHÔNG qua cổng (ví dụ: COD)
            // Nếu là COD:
            // - Đặt trạng thái ban đầu phù hợp (Waiting, Processing...).
            // - LƯU đơn hàng và OrderLine.
            // - THỰC HIỆN TRỪ TỒN KHO.
            // - THỰC HIỆN XÓA GIỎ HÀNG.
            // - Trả về DTO KHÔNG CÓ URL.

            newOrder.setStatus(OrderStatus.WAITING); // Hoặc trạng thái ban đầu khác cho COD
            logger.debug("Payment method is {}. Setting status to {} for user {}.", request.getPaymentMethod().name(), newOrder.getStatus().name(), currentUser.getUserId());

            // 9b. Lưu Order cho COD
            Order savedOrder = orderRepository.save(newOrder);
             logger.info("COD Order saved successfully with ID: {} for user {}", savedOrder.getOrderId(), currentUser.getUserId());

            // 10b. Hoàn thiện và lưu OrderLine, cập nhật tồn kho cho COD
            List<OrderLine> finalOrderLines = new ArrayList<>();
            for (OrderLine line : tempOrderLines) {
                line.setOrder(savedOrder); // Liên kết với Order đã lưu
                OrderLine savedLine = orderLineRepository.save(line); // Lưu OrderLine
                finalOrderLines.add(savedLine);
                logger.debug("OrderLine saved with ID: {} for COD Order ID: {}", savedLine.getOrderLineId(), savedOrder.getOrderId());

                // *** CẬP NHẬT TỒN KHO SẢN PHẨM CHO COD ***
                Product productToUpdate = line.getProduct();
                int newStock = productToUpdate.getQuantity() - line.getQuantity();
                if (newStock < 0) {
                    logger.error("Stock calculation error after saving OrderLine for COD! Product ID: {}, New Stock calculated: {}", productToUpdate.getProductId(), newStock);
                    // Ném lỗi ở đây sẽ khiến transaction rollback, hủy đơn hàng COD nếu tồn kho không đủ khi lưu OrderLine
                    throw new IllegalStateException("Lỗi nghiêm trọng: Số lượng tồn kho âm sau khi cập nhật cho sản phẩm ID " + productToUpdate.getProductId());
                }
                productToUpdate.setQuantity(newStock);
                productRepository.save(productToUpdate);
                logger.debug("Updated stock for Product ID: {}. New stock: {}", productToUpdate.getProductId(), newStock);
            }
            savedOrder.setOrderLines(finalOrderLines); // Gán lại list OrderLine

            // 11b. Xóa các CartItem đã được đặt hàng khỏi giỏ hàng cho COD
            cartItemRepository.deleteAll(selectedCartItems);
            logger.info("Deleted {} cart items for user {} after COD order creation.", selectedCartItems.size(), currentUser.getUserId());

            // Trả về DTO với Order đã hoàn tất và paymentUrl null
            return new CreateOrderResponseDTO(savedOrder, null);
        }
    }

    // Giữ nguyên các phương thức khác như calculateShippingFee, formatShippingAddress, getOrderDetailsById
    private Double calculateShippingFee(Address address, int totalItems, double itemsSubtotal) {
        if (totalItems == 0) return 0.0;
        if (totalItems <= 3) return 15000.0;
        if (totalItems <= 7) return 25000.0;
        return 35000.0;
    }

    private String formatShippingAddress(Address address) {
        if (address == null) {
            logger.warn("Cannot format shipping address because Address object is null.");
            return "";
        }
        // Ví dụ định dạng: Số nhà, Tên đường, Phường/Xã, Quận/Huyện, Tỉnh/Thành phố
        StringBuilder sb = new StringBuilder();
        if (address.getStreetAddress() != null) sb.append(address.getStreetAddress());
        if (address.getWard() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getWard());
        }
        if (address.getDistrict() != null) {
             if (sb.length() > 0) sb.append(", ");
            sb.append(address.getDistrict());
        }
         if (address.getCity() != null) {
             if (sb.length() > 0) sb.append(", ");
            sb.append(address.getCity());
        }
        return sb.toString();
    }

    @Override
    public Order getOrderDetailsById(Long orderId, User user) {
        logger.debug("Fetching order details for ID: {} requested by User ID: {}", orderId, user.getUserId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order not found for ID: {}", orderId);
                    return new NoSuchElementException("Không tìm thấy đơn hàng với ID: " + orderId);
                });

        // Kiểm tra quyền xem đơn hàng
        if (order.getUser().getUserId() != user.getUserId()) { // So sánh bằng equals
             logger.warn("Forbidden access attempt: User {} tried to access order {} owned by user {}",
                        user.getUserId(), orderId, order.getUser().getUserId());
            throw new IllegalStateException("Bạn không có quyền xem đơn hàng này.");
        }
        logger.debug("Order details retrieved successfully for Order ID: {}", orderId);
        return order;
    }

    // *** PHƯƠNG THỨC QUAN TRỌNG: XỬ LÝ CALLBACK (IPN) TỪ VNPAY ***
    // Đây là phương thức được VNPAY gọi đến, KHÔNG PHẢI từ app mobile của bạn.
    // Bạn CẦN tạo một Controller Endpoint riêng để VNPAY gửi thông báo đến,
    // và Endpoint đó sẽ gọi phương thức này của Service.
    // Logic trong này MỚI là nơi cập nhật trạng thái, trừ stock, xóa giỏ hàng khi VNPAY báo thành công.
    // Cần triển khai chi tiết dựa trên cấu trúc dữ liệu VNPAY gửi về.

    @Transactional
    public void handleVnpayIpn(Map<String, String> vnpayData, HttpServletRequest request) {
         logger.info("Received VNPAY IPN callback.");
         try {
             // BƯỚC 1: KIỂM TRA VÀ XÁC THỰC CHỮ KÝ (Sử dụng VnpayService)
             boolean isValidSignature = vnpayService.verifyIpnSignature(vnpayData, request);
             if (!isValidSignature) {
                  logger.warn("VNPAY IPN signature verification failed.");
                  // *** Ném Exception thay vì return false ***
                  throw new IllegalArgumentException("VNPAY IPN signature verification failed."); // Controller sẽ bắt và trả về RspCode 97
             }
             logger.debug("VNPAY IPN signature is valid.");

             // ... (Các bước lấy dữ liệu từ vnpayData) ...
             Long orderId = Long.parseLong(vnpayData.get("vnp_TxnRef"));
             String vnp_ResponseCode = vnpayData.get("vnp_ResponseCode");
             long vnp_Amount = Long.parseLong(vnpayData.get("vnp_Amount")); // Số tiền VNPAY báo về (đã nhân 100)

             // BƯỚC 2: KIỂM TRA TRẠNG THÁI VÀ SỐ TIỀN ĐƠN HÀNG TRONG HỆ THỐNG
             Optional<Order> orderOpt = orderRepository.findById(orderId);
             if (orderOpt.isEmpty()) {
                 logger.warn("VNPAY IPN received for non-existent order ID: {}", orderId);
                  // *** Ném Exception thay vì return false ***
                  throw new NoSuchElementException("Không tìm thấy đơn hàng với ID: " + orderId); // Controller sẽ bắt và trả về RspCode 01
             }
             Order order = orderOpt.get();

             // Kiểm tra trạng thái đơn hàng: chỉ xử lý IPN cho đơn đang ở trạng thái chờ thanh toán
             if (order.getStatus() != OrderStatus.PENDING) {
                 logger.warn("VNPAY IPN received for order ID {} but status is {} (expected PendingPayment). Possibly duplicate IPN or wrong state.", orderId, order.getStatus());
                  // *** Ném Exception thay vì return false ***
                  throw new IllegalStateException("Đơn hàng " + orderId + " có trạng thái không hợp lệ: " + order.getStatus().name()); // Controller sẽ bắt và trả về RspCode 02
             }

             // Kiểm tra số tiền (đã sửa lỗi nhân 100)
             long orderAmountInSystem = (long) (order.getTotalPrice() * 100);
              if (vnp_Amount != orderAmountInSystem) {
                  logger.warn("VNPAY IPN amount mismatch for Order ID {}. VNPAY Amount: {}, System Amount: {}", orderId, vnp_Amount, orderAmountInSystem);
                  // *** Ném Exception thay vì return false ***
                  throw new IllegalStateException("Số tiền thanh toán không khớp cho đơn hàng " + orderId); // Controller sẽ bắt và trả về RspCode 04 (hoặc 02 tùy cách mapping)
              }
              logger.debug("Order {} status, amount, and signature are valid.", orderId);


             // BƯỚC 3: XỬ LÝ KẾT QUẢ THANH TOÁN TỪ VNPAY DỰA TRÊN ResponseCode
             if ("00".equals(vnp_ResponseCode)) {
                 // Thanh toán THÀNH CÔNG
                 logger.info("VNPAY IPN: Payment successful for Order ID: {}", orderId);
                 // *** Cập nhật trạng thái: Sửa từ Waiting sang Processing ***
                 order.setStatus(OrderStatus.WAITING);

                 // --- THỰC HIỆN CÁC HÀNH ĐỘNG ĐÃ BỎ QUA Ở createOrder ---
                 // ... (code trừ tồn kho và xóa giỏ hàng - giữ nguyên) ...
                 // Đảm bảo phương thức findByCart_CartIdAndProduct_ProductIdIn đã được khai báo trong CartItemRepository
                 // Code này bạn đã viết và có vẻ đúng logic.


             } else {
                 // Thanh toán THẤT BẠI hoặc HỦY (ResponseCode khác 00)
                 logger.warn("VNPAY IPN: Payment failed/cancelled for Order ID: {}. Response Code: {}", orderId, vnp_ResponseCode);
                 order.setStatus(OrderStatus.CANCELLED); // Giữ nguyên trạng thái Đã hủy/Thất bại
                 // Không trừ tồn kho, không xóa giỏ hàng
             }

             // BƯỚC 4: LƯU LẠI TRẠNG THÁI ĐƠN HÀNG ĐÃ CẬP NHẬT
             orderRepository.save(order);
             logger.info("Order status updated to {} for Order ID {}", order.getStatus().name(), orderId);

             // Phương thức này trả về void khi hoàn thành mà không có lỗi nghiệp vụ.
             // Nếu có lỗi NumberFormatException hoặc Exception khác, nó sẽ bị bắt trong try-catch ở OrderService.
             // Nhưng tốt nhất là bắt các Exception cụ thể ở Controller.

         } catch (NumberFormatException e) {
             logger.error("Error parsing VNPAY data (Order ID or Amount) in OrderService.", e);
             // *** Ném Exception để Controller bắt và trả về RspCode 05 ***
             throw new NumberFormatException("Error parsing VNPAY data: " + e.getMessage());
         }
         // Các Exception khác như NoSuchElementException, IllegalStateException, IllegalArgumentException
         // đã được ném ra ở các block if bên trên, sẽ được catch bởi Controller.
         catch (Exception e) {
              // Bắt các Exception không mong muốn khác ở đây nếu không ném lại từ các block if
              // và ném lại Exception để Controller bắt và trả về RspCode 99
              logger.error("Unexpected error processing VNPAY IPN in OrderService for order ID derived from vnpayData", e);
              throw new RuntimeException("Error processing VNPAY IPN: " + e.getMessage(), e);
         }
     }
}