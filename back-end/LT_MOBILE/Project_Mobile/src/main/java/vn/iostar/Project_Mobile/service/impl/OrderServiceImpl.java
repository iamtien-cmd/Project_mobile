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

import java.sql.Date;
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

        // Cập nhật dòng logger này
        logger.debug("Default address found for user {}. Recipient Name: '{}', Recipient Phone: '{}'. This info will be used for the order's shippingAddress field.",
                currentUser.getUserId(),
                defaultAddress.getRecipientName(),
                defaultAddress.getRecipientPhone());

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

        double itemsSubtotal = 0;
        List<OrderLine> tempOrderLines = new ArrayList<>();
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
            double productPrice = product.getPrice();
            itemsSubtotal += productPrice * item.getQuantity();
            OrderLine line = new OrderLine();
            line.setProduct(product);
            line.setQuantity(item.getQuantity());
            line.setPrice(productPrice);
            tempOrderLines.add(line);
        }
        logger.debug("Calculated itemsSubtotal for user {}: {}", currentUser.getUserId(), itemsSubtotal);

        int totalItemsCount = selectedCartItems.stream().mapToInt(CartItem::getQuantity).sum();
        double calculatedShippingFee = calculateShippingFee(defaultAddress, totalItemsCount, itemsSubtotal);
        logger.debug("Calculated shipping fee for user {}: {}", currentUser.getUserId(), calculatedShippingFee);
        double finalTotalPrice = itemsSubtotal + calculatedShippingFee;
        logger.debug("Calculated finalTotalPrice for user {}: {}", currentUser.getUserId(), finalTotalPrice);

        Order newOrder = new Order();
        newOrder.setUser(currentUser);
        newOrder.setItemsSubtotal(itemsSubtotal);
        newOrder.setTotalPrice(finalTotalPrice);
        // Sử dụng phương thức formatShippingAddress đã được sửa đổi
        newOrder.setShippingAddress(formatShippingAddress(defaultAddress));
        Instant now = Instant.now();
        newOrder.setOrderDate(new java.sql.Date(Date.from(now).getTime()));
        Instant predictDate = now.plus(5, ChronoUnit.DAYS);
        newOrder.setPredictReceiveDate(new java.sql.Date(Date.from(predictDate).getTime()));
        newOrder.setPaymentMethod(request.getPaymentMethod());

        String paymentUrl = null;
        Order savedOrder;

        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            newOrder.setStatus(OrderStatus.PENDING);
            logger.debug("Payment method is VNPAY. Setting status to PENDING for user {}.", currentUser.getUserId());
            savedOrder = orderRepository.save(newOrder);
            logger.info("PENDING Order saved successfully with ID: {} for user {}", savedOrder.getOrderId(), currentUser.getUserId());

            List<OrderLine> finalOrderLines = new ArrayList<>();
            for (OrderLine line : tempOrderLines) {
                line.setOrder(savedOrder);
                OrderLine savedLine = orderLineRepository.save(line);
                finalOrderLines.add(savedLine);
            }
            savedOrder.setOrderLines(finalOrderLines);

            try {
                paymentUrl = vnpayService.createPaymentUrl(savedOrder, httpServletRequest);
                logger.info("Generated VNPAY URL for pending order {}: {}", savedOrder.getOrderId(), paymentUrl);
            } catch (Exception e) {
                logger.error("Error generating VNPAY URL for order {}: {}", savedOrder.getOrderId(), e.getMessage(), e);
            }
        } else {
            newOrder.setStatus(OrderStatus.WAITING);
            logger.debug("Payment method is {}. Setting status to {} for user {}.", request.getPaymentMethod().name(), newOrder.getStatus().name(), currentUser.getUserId());
            savedOrder = orderRepository.save(newOrder);
            logger.info("Order (Payment Method: {}) saved successfully with ID: {} for user {}", request.getPaymentMethod().name(), savedOrder.getOrderId(), currentUser.getUserId());

            List<OrderLine> finalOrderLines = new ArrayList<>();
            for (OrderLine line : tempOrderLines) {
                line.setOrder(savedOrder);
                OrderLine savedLine = orderLineRepository.save(line);
                finalOrderLines.add(savedLine);

                Product productToUpdate = line.getProduct();
                int newStock = productToUpdate.getQuantity() - line.getQuantity();
                if (newStock < 0) {
                    logger.error("Stock calculation error after saving OrderLine! Product ID: {}, New Stock calculated: {}", productToUpdate.getProductId(), newStock);
                    throw new IllegalStateException("Lỗi nghiêm trọng: Số lượng tồn kho âm sau khi cập nhật cho sản phẩm ID " + productToUpdate.getProductId());
                }
                productToUpdate.setQuantity(newStock);
                productRepository.save(productToUpdate);
            }
            savedOrder.setOrderLines(finalOrderLines);
            cartItemRepository.deleteAll(selectedCartItems);
            logger.info("Deleted {} cart items for user {} after order creation (ID: {}).", selectedCartItems.size(), currentUser.getUserId(), savedOrder.getOrderId());

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
                    List<Comment> previousReviews = commentRepository.findByUserAndProductAndReviewedIsTrue(user, productInOrder);
                    if (!previousReviews.isEmpty()) {
                        logger.debug("Found {} previous review(s) to reset for User ID: {} and Product ID: {}",
                                previousReviews.size(), user.getUserId(), productInOrder.getProductId());
                        for (Comment oldReview : previousReviews) {
                            oldReview.setReviewed(false);
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
        if (totalItems == 0) return 0.0;
        if (itemsSubtotal > 700000) return 0.0;
        if (totalItems <= 3) return 15000.0;
        if (totalItems <= 7) return 25000.0;
        return 35000.0;
    }


    private String formatShippingAddress(Address address) {
        if (address == null) {
            logger.warn("Cannot format shipping address and recipient info because Address object is null.");

            return "||"; 
        }

        List<String> parts = new ArrayList<>();

        // 1. Tên người nhận
        String recipientName = address.getRecipientName();
        parts.add(recipientName != null && !recipientName.trim().isEmpty() ? recipientName.trim() : "");

        // 2. Số điện thoại người nhận
        String recipientPhone = address.getRecipientPhone();
        parts.add(recipientPhone != null && !recipientPhone.trim().isEmpty() ? recipientPhone.trim() : "");

        // 3. Địa chỉ đường (StreetAddress)
        String streetAddress = address.getStreetAddress();
        parts.add(streetAddress != null && !streetAddress.trim().isEmpty() ? streetAddress.trim() : "");

        // Nối các phần lại bằng dấu "|"
        String result = String.join("|", parts);

        logger.debug("Formatted shipping string for address ID {}: {}", address.getAddressId(), result);
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
        if (!order.getUser().getUserId().equals(user.getUserId())) {
             logger.warn("Forbidden access attempt: User {} tried to access order {} owned by user {}",
                        user.getUserId(), orderId, order.getUser().getUserId());
            throw new IllegalStateException("Bạn không có quyền xem đơn hàng này.");
        }
        logger.info("SHIPPING_ADDRESS_FROM_DB for order ID {}: '{}'", orderId, order.getShippingAddress());
        order.getOrderLines().forEach(line -> {
            if (line.getProduct() != null) line.getProduct().getName();
        });
        logger.debug("Order details retrieved successfully for Order ID: {}", orderId);
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
                  throw new IllegalArgumentException("VNPAY IPN signature verification failed.");
             }
             logger.debug("VNPAY IPN signature is valid.");

             Long orderId = Long.parseLong(vnpayData.get("vnp_TxnRef"));
             String vnp_ResponseCode = vnpayData.get("vnp_ResponseCode");
             String vnp_TransactionStatus = vnpayData.get("vnp_TransactionStatus");
             long vnp_AmountReported = Long.parseLong(vnpayData.get("vnp_Amount"));

             Optional<Order> orderOpt = orderRepository.findById(orderId);
             if (orderOpt.isEmpty()) {
                 logger.warn("VNPAY IPN: Order not found for ID: {}", orderId);
                 throw new NoSuchElementException("Không tìm thấy đơn hàng với ID: " + orderId);
             }
             Order order = orderOpt.get();

             if (order.getStatus() != OrderStatus.PENDING) {
                 logger.warn("VNPAY IPN: Order ID {} already processed or in invalid state. Current status: {}. Expected PENDING.", orderId, order.getStatus());
                 throw new IllegalStateException("Đơn hàng " + orderId + " không ở trạng thái chờ thanh toán: " + order.getStatus().name());
             }

             long orderAmountInSystemTimes100 = (long) (order.getTotalPrice() * 100);
             if (vnp_AmountReported != orderAmountInSystemTimes100) {
                  logger.warn("VNPAY IPN amount mismatch for Order ID {}. VNPAY Amount (x100): {}, System Amount (x100): {}", orderId, vnp_AmountReported, orderAmountInSystemTimes100);
                  throw new IllegalStateException("Số tiền thanh toán không khớp cho đơn hàng " + orderId);
             }
             logger.debug("Order {} status, amount, and signature are valid for IPN processing.", orderId);

             if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
                 logger.info("VNPAY IPN: Payment successful for Order ID: {}", orderId);
                 order.setStatus(OrderStatus.WAITING);

                 for (OrderLine line : order.getOrderLines()) {
                     Product productToUpdate = line.getProduct();
                     Optional<Product> freshProductOpt = productRepository.findById(productToUpdate.getProductId());
                     if(freshProductOpt.isPresent()){
                         Product freshProduct = freshProductOpt.get();
                         int newStock = freshProduct.getQuantity() - line.getQuantity();
                         if (newStock < 0) {
                             logger.error("VNPAY IPN - Stock error! Product ID: {}, New Stock calculated: {}. Order cannot be fulfilled despite successful payment.",
                                     freshProduct.getProductId(), newStock);
                             order.setStatus(OrderStatus.ERROR);
                             break;
                         }
                         freshProduct.setQuantity(newStock);
                         productRepository.save(freshProduct);
                         logger.debug("VNPAY IPN - Updated stock for Product ID: {}. New stock: {}", freshProduct.getProductId(), newStock);
                     } else {
                          logger.error("VNPAY IPN - Product ID {} not found during stock update for order {}.", productToUpdate.getProductId(), orderId);
                          order.setStatus(OrderStatus.ERROR);
                          break;
                     }
                 }

                 if (order.getStatus() != OrderStatus.ERROR) {
                     Cart userCart = cartRepository.findByUser_UserId(order.getUser().getUserId())
                             .orElse(null);
                     if (userCart != null) {
                         List<Long> productIdsInOrder = order.getOrderLines().stream()
                                 .map(ol -> ol.getProduct().getProductId())
                                 .collect(Collectors.toList());
                         List<CartItem> itemsToDelete = cartItemRepository.findByCart_CartIdAndProduct_ProductIdIn(userCart.getCartId(), productIdsInOrder);
                         if (!itemsToDelete.isEmpty()) {
                             cartItemRepository.deleteAll(itemsToDelete);
                             logger.info("VNPAY IPN - Deleted {} cart items for user {} after successful payment for order {}.",
                                     itemsToDelete.size(), order.getUser().getUserId(), orderId);
                         }
                     }
                     resetPreviousProductReviews(order.getUser(), order);
                 }

             } else {
                 logger.warn("VNPAY IPN: Payment failed/cancelled for Order ID: {}. VNPAY Response Code: {}, Transaction Status: {}",
                         orderId, vnp_ResponseCode, vnp_TransactionStatus);
                 order.setStatus(OrderStatus.CANCELLED);
             }

             orderRepository.save(order);
             logger.info("Order status updated to {} for Order ID {} via VNPAY IPN.", order.getStatus().name(), orderId);

         } catch (NumberFormatException e) {
             logger.error("Error parsing VNPAY IPN data (Order ID or Amount).", e);
             throw new NumberFormatException("Error parsing VNPAY IPN data: " + e.getMessage());
         } catch (NoSuchElementException | IllegalArgumentException | IllegalStateException e) {
             logger.warn("Business rule violation or data issue during VNPAY IPN processing: {}", e.getMessage());
             throw e;
         } catch (Exception e) {
              logger.error("Unexpected error processing VNPAY IPN for order data potentially related to order {}: {}", vnpayData.get("vnp_TxnRef"), e.getMessage(), e);
              throw new RuntimeException("Unexpected error processing VNPAY IPN: " + e.getMessage(), e);
         }
     }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        logger.debug("Fetching orders for User ID: {}", userId);
        List<Order> orders = orderRepository.findByUser_UserIdOrderByOrderDateDesc(userId);
        orders.forEach(order -> {
            order.getOrderLines().size();
        });
        logger.info("Found {} orders for User ID: {}", orders.size(), userId);
        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderDetailsById(Long orderId) throws ResourceNotFoundException {
        logger.debug("Fetching order details for Order ID: {}", orderId);
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.getOrderLines().size();
            for (OrderLine line : order.getOrderLines()) {
                if (line.getProduct() != null) {
                    line.getProduct().getName();
                }
            }
            if (order.getUser() != null) {
                order.getUser().getFullName();
            }
            logger.debug("Order details retrieved for Order ID: {}", orderId);
            return order;
        } else {
            logger.warn("Order not found for ID: {}", orderId);
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

        if (order.getStatus() != OrderStatus.WAITING && order.getStatus() != OrderStatus.PENDING) {
            logger.warn("Cancel failed: Order ID {} cannot be cancelled. Current status: {}", orderId, order.getStatus());
            throw new IllegalStateException("Không thể hủy đơn hàng này. Trạng thái hiện tại: " + order.getStatus());
        }

        if (order.getStatus() == OrderStatus.WAITING) {
            for (OrderLine line : order.getOrderLines()) {
                Product productToRestore = line.getProduct();
                 Optional<Product> freshProductOpt = productRepository.findById(productToRestore.getProductId());
                 if(freshProductOpt.isPresent()){
                     Product freshProduct = freshProductOpt.get();
                     int newStock = freshProduct.getQuantity() + line.getQuantity();
                     freshProduct.setQuantity(newStock);
                     productRepository.save(freshProduct);
                     logger.debug("Restored stock for Product ID: {}. New stock: {}", freshProduct.getProductId(), newStock);
                 } else {
                     logger.error("Error restoring stock: Product ID {} not found for order {}.", productToRestore.getProductId(), orderId);
                 }
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        logger.info("Order ID: {} has been cancelled successfully.", orderId);
        return savedOrder;
    }
}