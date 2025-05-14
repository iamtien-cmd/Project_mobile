package vn.iostar.Project_Mobile.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iostar.Project_Mobile.DTO.CommentResponse; // Giữ lại nếu bạn vẫn dùng ở đâu đó
import vn.iostar.Project_Mobile.entity.Comment;
import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.entity.OrderLine;

import vn.iostar.Project_Mobile.repository.*;
import vn.iostar.Project_Mobile.service.ICommentService;
import vn.iostar.Project_Mobile.util.OrderStatus; // Đảm bảo import đúng

@Service
public class CommentServiceImpl implements ICommentService {

    @Autowired
    private ICommentRepository commentRepository;

    @Autowired
    private IOrderRepository orderRepository;

    // @Autowired // Không cần các repo này ở đây nếu không dùng trực tiếp trong các phương thức khác của service này
    // private IUserRepository userRepository;
    // @Autowired
    // private ProductRepository productRepository; // Nếu ProductRepository là interface thì tên nên là IProductRepository
    // @Autowired
    // private OrderLineRepository orderLineRepository;


    // Danh sách các trạng thái đơn hàng mà từ đó chúng ta có thể xét duyệt để chuyển sang REVIEWED
    // Ví dụ: chỉ khi đơn hàng đã GIAO HÀNG THÀNH CÔNG (DELIVERED) hoặc ĐÃ NHẬN (RECEIVED)
    private static final List<OrderStatus> ELIGIBLE_STATUSES_FOR_ORDER_REVIEW_CHECK =
            List.of(OrderStatus.DELIVERED, OrderStatus.RECEIVED); // Hoặc các trạng thái phù hợp khác

    @Override
    @Transactional // Quan trọng để đảm bảo các thao tác DB nhất quán
    public Comment createComment(Comment comment) {
        // Kiểm tra Order và Product từ comment (nếu chưa được set hoặc muốn validate lại)
        if (comment.getUser() == null || comment.getProduct() == null) {
            throw new IllegalArgumentException("User and Product must be set for a comment.");
        }

        // Nếu comment có liên quan đến một Order cụ thể để đánh dấu là đã review cho order đó
        if (comment.getOrder() != null) {
            Order order = orderRepository.findById(comment.getOrder().getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + comment.getOrder().getOrderId()));
            comment.setOrder(order); // Đảm bảo comment entity có order object được quản lý
            // Logic này có thể không cần nếu bạn đã set Reviewed=true từ controller
            // và không có logic setReviewed riêng cho Order
            // if (order.getReviewed() != null && order.getReviewed()) {
            //     comment.setReviewed(true);
            // }
        }
        // Đảm bảo comment được đánh dấu là đã review (reviewed = true)
        // Controller đã set: commentEntity.setReviewed(true);
        Comment savedComment = commentRepository.save(comment);

        // Sau khi lưu comment thành công, kiểm tra và cập nhật trạng thái đơn hàng nếu cần
        // Chỉ thực hiện nếu comment này có liên kết đến một đơn hàng
        if (savedComment.getOrder() != null) {
            try {
                checkAndUpdateOrderStatusIfAllProductsInOrderReviewed(savedComment.getOrder(), savedComment.getUser());
            } catch (Exception e) {
                // Log lỗi nhưng không để nó làm hỏng việc tạo comment
                // Lỗi reflection có thể xảy ra ở đây, cần log chi tiết
                System.err.println("ERROR in checkAndUpdateOrderStatusIfAllProductsInOrderReviewed for orderId "
                        + savedComment.getOrder().getOrderId() + ": " + e.getMessage());
                e.printStackTrace(); // In stack trace để debug
            }
        }

        return savedComment;
    }

    /**
     * Kiểm tra xem tất cả sản phẩm trong một đơn hàng đã được người dùng đánh giá hay chưa.
     * Nếu tất cả đã được đánh giá, cập nhật trạng thái đơn hàng thành REVIEWED.
     *
     * @param order The order to check.
     * @param user  The user who submitted the reviews.
     */
    private void checkAndUpdateOrderStatusIfAllProductsInOrderReviewed(Order order, User user) {
        System.out.println("DEBUG SERVICE: Checking if all products are reviewed for Order ID: " + order.getOrderId() + " by User ID: " + user.getUserId());

        // 1. Đảm bảo đơn hàng đang ở trạng thái cho phép xét duyệt (ví dụ: DELIVERED, RECEIVED)
        // Điều này quan trọng để không cập nhật trạng thái của đơn hàng đang PENDING chẳng hạn.
        if (!ELIGIBLE_STATUSES_FOR_ORDER_REVIEW_CHECK.contains(order.getStatus())) {
            System.out.println("DEBUG SERVICE: Order ID " + order.getOrderId() + " status (" + order.getStatus() + ") is not eligible for review check. Skipping.");
            return;
        }

        // 2. Lấy tất cả các OrderLine (chi tiết sản phẩm) của đơn hàng này
        List<OrderLine> orderLines = order.getOrderLines();
        if (orderLines == null || orderLines.isEmpty()) {
            System.out.println("DEBUG SERVICE: Order ID " + order.getOrderId() + " has no order lines. Skipping.");
            return; // Không có sản phẩm để đánh giá
        }
        System.out.println("DEBUG SERVICE: Order ID " + order.getOrderId() + " has " + orderLines.size() + " unique product lines.");

        // 3. Lấy ra tập hợp các ID sản phẩm duy nhất trong đơn hàng
        Set<Long> productIdsInOrder;
        try {
            productIdsInOrder = orderLines.stream()
                    .map(orderLine -> {
                        Product product = orderLine.getProduct();
                        if (product == null) {
                            // Trường hợp hiếm, nên log lại nếu xảy ra
                            System.err.println("ERROR SERVICE: OrderLine ID " + orderLine.getOrderLineId() + " in Order ID " + order.getOrderId() + " has a null product reference!");
                            return null; // Sẽ được filter out
                        }
                        // Dòng này có thể gây lỗi reflection nếu Product.java có vấn đề
                        return product.getProductId();
                    })
                    .filter(productId -> productId != null) // Loại bỏ các product null nếu có
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            // Bắt lỗi reflection ngay tại đây nếu nó xảy ra khi truy cập product.getProductId()
            System.err.println("ERROR SERVICE: Exception while extracting product IDs from order lines for Order ID " + order.getOrderId());
            e.printStackTrace();
            return; // Không thể tiếp tục nếu không lấy được ID sản phẩm
        }

        if (productIdsInOrder.isEmpty()) {
            System.out.println("DEBUG SERVICE: No valid product IDs found in order lines for Order ID " + order.getOrderId() + ". Skipping.");
            return;
        }
        System.out.println("DEBUG SERVICE: Unique Product IDs in Order " + order.getOrderId() + ": " + productIdsInOrder);

        // 4. Đếm xem có bao nhiêu sản phẩm trong số đó đã được người dùng này đánh giá (comment.isReviewed() == true)
        //    và thuộc về đơn hàng này (comment.getOrder().getOrderId() == order.getOrderId())
        //    Chúng ta cần một phương thức repository phù hợp hơn.
        //    Ví dụ: countReviewedCommentsByUserForProductsInOrder(User user, Order order, Set<Long> productIds)
        //    Hiện tại, countByUserAndProductInAndReviewedTrue không lọc theo Order, nó sẽ đếm tất cả review của user cho các product đó.
        //    Điều này có thể không đúng nếu user mua cùng sản phẩm trong nhiều đơn hàng.
        //    Tuy nhiên, nếu logic của bạn là "miễn user đã review sản phẩm đó (dù ở đơn nào) thì tính là reviewed cho order này",
        //    thì query hiện tại có thể chấp nhận được.
        //    Nếu bạn muốn chính xác "đã review SẢN PHẨM NÀY trong ĐƠN HÀNG NÀY", bạn cần query phức tạp hơn.

        // Giả sử query hiện tại là ý đồ: đếm số sản phẩm user đã từng review trong danh sách productIdsInOrder
        long reviewedProductCountInThisOrderContext = commentRepository.countDistinctProductsReviewedByUserForOrder(user, order, productIdsInOrder);
        // Hoặc nếu dùng query cũ:
        // long reviewedProductCount = commentRepository.countByUserAndProductInAndReviewedTrue(user, productIdsInOrder);

        System.out.println("DEBUG SERVICE: User ID " + user.getUserId() + " has reviewed " + reviewedProductCountInThisOrderContext +
                " distinct products from the set " + productIdsInOrder.size() + " for Order ID " + order.getOrderId());

        // 5. So sánh
        if (reviewedProductCountInThisOrderContext >= productIdsInOrder.size()) {
            // Tất cả sản phẩm trong đơn hàng này đã được người dùng đánh giá
            System.out.println("INFO SERVICE: All products in Order ID " + order.getOrderId() + " have been reviewed by User ID " + user.getUserId() + ". Updating order status to REVIEWED.");
            order.setStatus(OrderStatus.REVIEWED);
            // order.setReviewed(true); // Nếu bạn dùng cờ boolean thay vì status
            orderRepository.save(order);
        } else {
            System.out.println("DEBUG SERVICE: Not all products reviewed yet for Order ID " + order.getOrderId() + " by User ID " + user.getUserId() +
                    ". (Reviewed: " + reviewedProductCountInThisOrderContext + ", Total unique products in order: " + productIdsInOrder.size() + ")");
        }
    }

    // Các phương thức khác của ICommentService như getCommentsByProduct, deleteComment, hasUserReviewedProduct...
    // ... (giữ nguyên code hiện tại của bạn cho các phương thức đó, chỉ sửa createComment và thêm checkAndUpdate...)
    @Override
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByProduct(Product product) {
        return commentRepository.findByProduct(product).stream().map(comment -> {
            CommentResponse response = new CommentResponse();
            response.setCommentId(comment.getCommentId());
            response.setImage(comment.getImage());
            response.setContent(comment.getContent());
            response.setRating(comment.getRating());
            response.setCreatedAt(comment.getCreatedAt());
            if (comment.getUser() != null) {
                response.setFullname(comment.getUser().getFullName());
                response.setAvatar(comment.getUser().getAvatar());
            } else {
                response.setFullname("Anonymous User");
            }
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

	@Override
	public boolean hasUserReviewedProduct(User user, Product product, Order order) {
		return commentRepository.existsByUserAndProductAndReviewedIsTrue(user, product);
	}
	 private static final List<OrderStatus> ELIGIBLE_STATUSES_FOR_REVIEW =
	            List.of(OrderStatus.RECEIVED, OrderStatus.DELIVERED);


	 @Transactional
	 public Comment createComment(Comment comment) {
	     if (comment.getOrder() != null) {
	         Order order = orderRepository.findById(comment.getOrder().getOrderId())
	                 .orElseThrow(() -> new RuntimeException("Order not found with id: " + comment.getOrder().getOrderId()));

	         if (order.getReviewed() != null && order.getReviewed()) {
	             comment.setReviewed(true);
	         }
	     }

	     // Lưu bình luận
	     Comment savedComment = commentRepository.save(comment);

	     // Gọi kiểm tra và cập nhật trạng thái đơn hàng
	     checkAndUpdateOrderStatusIfAllReviewed(comment.getUser(), comment.getProduct());

	     return savedComment;
	 }

	    private void checkAndUpdateOrderStatusIfAllReviewed(User user, Product commentedProduct) {
	        // 4. Find candidate orders
	        List<Order> candidateOrders = orderRepository.findCandidateOrdersForReviewCheck(
	                user, commentedProduct, ELIGIBLE_STATUSES_FOR_REVIEW);

	        // 5. Iterate through each candidate order
	        for (Order order : candidateOrders) {
	            // 6. Get all unique product IDs in this order
	            Set<Long> productIdsInOrder = order.getOrderLines().stream()
	                    .map(orderLine -> orderLine.getProduct().getProductId())
	                    .collect(Collectors.toSet());

	            // 7. Count how many of these products the user has reviewed
	            long reviewedProductCount = commentRepository.countByUserAndProductInAndReviewedTrue(user, productIdsInOrder);

	            // 8. Check if all products in the order have been reviewed
	            if (reviewedProductCount == productIdsInOrder.size()) {
	                // 9. Update the order status (Recommended)
	                order.setStatus(OrderStatus.REVIEWED);
	                // OR update the boolean flag (Alternative)
	                // order.setReviewed(true);

	                orderRepository.save(order); // Persist the change
	            }
	        }
	    }
	    @Override
	    public void resetReviewStatus(User user, Product product) {
	        List<Comment> comments = commentRepository.findByUserAndProduct(user, product);
	        for (Comment comment : comments) {
	            comment.setReviewed(false);
	            commentRepository.save(comment);
	        }
	    }


}