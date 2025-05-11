// Giả sử bạn có các Repository tương ứng
@Autowired private UserRepository userRepository;
@Autowired private ProductRepository productRepository;
@Autowired private OrderItemRepository orderItemRepository;
@Autowired private CommentRepository commentRepository;
@Autowired private PurchaseReviewRepository purchaseReviewRepository; // Repository cho PurchaseReview

public PurchaseReview submitReview(Long userId, Long productId, Long orderItemId, String content, int rating, String image) {
    User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
    OrderItem orderItem = orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new RuntimeException("OrderItem not found"));

    // 1. Kiểm tra điều kiện nghiệp vụ
    if (!orderItem.getOrder().getUser().getUserId().equals(userId)) {
        throw new RuntimeException("User does not own this order item.");
    }
    if (!orderItem.getProduct().getProductId().equals(productId)) {
        throw new RuntimeException("OrderItem does not belong to this product.");
    }
    if (!List.of(OrderStatus.DELIVERED, OrderStatus.RECEIVED).contains(orderItem.getOrder().getStatus())) {
        throw new RuntimeException("Order item is not in a reviewable state.");
    }

    // 2. Kiểm tra xem OrderItem này đã được đánh giá chưa (thông qua bảng PurchaseReview)
    if (purchaseReviewRepository.existsByOrderItem(orderItem)) {
        throw new RuntimeException("This purchase has already been reviewed.");
    }

    // 3. Tạo Comment
    Comment newComment = new Comment();
    newComment.setUser(user);
    newComment.setProduct(product); // Vẫn giữ liên kết này để dễ dàng lấy tất cả comment của sản phẩm
    newComment.setContent(content);
    newComment.setRating(rating);
    newComment.setImage(image);
    newComment.setAvatar(user.getAvatar()); // Hoặc cách bạn lấy avatar
    // newComment.setCreatedAt(...); // Đã tự động
    Comment savedComment = commentRepository.save(newComment);

    // 4. Tạo PurchaseReview để liên kết OrderItem và Comment
    PurchaseReview purchaseReview = new PurchaseReview();
    purchaseReview.setOrderItem(orderItem);
    purchaseReview.setComment(savedComment);
    // purchaseReview.setReviewDate(...); // Đã tự động

    return purchaseReviewRepository.save(purchaseReview); // Nếu orderItem đã có review, DB sẽ báo lỗi UNIQUE constraint
}

// Trong PurchaseReviewRepository:
// public interface PurchaseReviewRepository extends JpaRepository<PurchaseReview, Long> {
//     boolean existsByOrderItem(OrderItem orderItem);
//     // Optional<PurchaseReview> findByOrderItem(OrderItem orderItem);
// }