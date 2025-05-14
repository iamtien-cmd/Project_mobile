package vn.iostar.Project_Mobile.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// Import thêm nếu bạn dùng UserDetails tùy chỉnh
// import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.iostar.Project_Mobile.DTO.CommentRequestDTO;
import vn.iostar.Project_Mobile.DTO.CommentResponse;
import vn.iostar.Project_Mobile.entity.Comment;
import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.exception.UserNotFoundException; // Ví dụ một custom exception
import vn.iostar.Project_Mobile.repository.IOrderRepository;
import vn.iostar.Project_Mobile.service.ICommentService;
import vn.iostar.Project_Mobile.service.IProductService;
import vn.iostar.Project_Mobile.service.IUserService;
import vn.iostar.Project_Mobile.service.IOrderService;
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IProductService productService;
    @Autowired
    private IOrderRepository orderRepository;
    

    // Endpoint cho bình luận chung
    // Client cần gửi userId trong DTO này.
    // Cân nhắc: Endpoint này có nên yêu cầu xác thực không? Nếu có, userId trong DTO có thể không cần thiết.

 // Trong CommentController.java
    @PostMapping
    public ResponseEntity<?> createGeneralComment(@Valid @RequestBody CommentRequestDTO commentRequestDTO) {
        try {
            User user = userService.findById(commentRequestDTO.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + commentRequestDTO.getUserId()));

            Product product = productService.findById(commentRequestDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + commentRequestDTO.getProductId()));

            Order order = orderRepository.findById(commentRequestDTO.getOrderId()) // Sử dụng orderRepository thay vì orderService
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + commentRequestDTO.getOrderId()));

            // Kiểm tra xem user đã review sản phẩm này TRONG đơn hàng này chưa
            if (commentService.hasUserReviewedProduct(user, product, order)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Bạn đã đánh giá sản phẩm này trong đơn hàng này rồi.");
            }

            Comment commentEntity = new Comment();
            commentEntity.setContent(commentRequestDTO.getContent());
            commentEntity.setRating(commentRequestDTO.getRating());
            commentEntity.setImage(commentRequestDTO.getImage());
            commentEntity.setUser(user);
            commentEntity.setProduct(product);
            commentEntity.setOrder(order);
            commentEntity.setReviewed(true); // Đánh dấu comment này là đã review

            Comment savedComment = commentService.createComment(commentEntity); // Service sẽ xử lý việc cập nhật trạng thái Order

            return new ResponseEntity<>(savedComment, HttpStatus.CREATED);

        } catch (RuntimeException e) { // Bắt lỗi chung chung hơn trước
            String errorMessage = e.getMessage();
            // Log lỗi chi tiết ở đây
            System.err.println("SERVER CONTROLLER: Exception during comment creation - " + errorMessage);
            e.printStackTrace(); // In stack trace đầy đủ

            if (e instanceof UserNotFoundException ||
                (errorMessage != null && (errorMessage.contains("User not found") || errorMessage.contains("Product not found") || errorMessage.contains("Order not found")))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            } else if (errorMessage != null && errorMessage.contains("You have already reviewed this product")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
            }
            // Các lỗi RuntimeException khác, bao gồm cả lỗi reflection nếu nó nổi lên đến đây
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi xử lý phía máy chủ: " + errorMessage);
        } catch (Exception e) { // Bắt các lỗi Exception không phải RuntimeException
            System.err.println("SERVER CONTROLLER: Unexpected general Exception during comment creation!");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi không mong muốn.");
        }
    }


    // GET: Lấy bình luận theo sản phẩm
    // Endpoint này có thể không cần xác thực nếu bạn muốn mọi người xem được bình luận
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getCommentsByProductId(@PathVariable Long productId) {
        try {
            Product productEntity = productService.findById(productId)
                  .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
            List<CommentResponse> responses = commentService.getCommentsByProduct(productEntity);

            // SỬA Ở ĐÂY:
            // Luôn trả về danh sách responses, dù nó có rỗng hay không.
            // Spring Boot (với Jackson hoặc Gson làm serializer mặc định) sẽ tự động
            // chuyển đổi danh sách rỗng thành một JSON array rỗng: []
            return ResponseEntity.ok(responses);

        } catch (RuntimeException e) {
            // Kiểm tra cụ thể hơn nếu là lỗi "Product not found"
            if (e.getMessage() != null && e.getMessage().contains("Product not found with id: " + productId)) {
                // Bạn có thể trả về 404 và một JSON error object nếu muốn
                // Hoặc một danh sách rỗng nếu logic của bạn cho phép (nghĩa là sản phẩm không tồn tại thì cũng không có comment)
                // Ở đây, nếu sản phẩm không tồn tại, trả về 404 là hợp lý.
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            // Các lỗi RuntimeException khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching comments: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

   // DELETE: Xóa bình luận theo ID
   // Endpoint này CHẮC CHẮN cần xác thực và kiểm tra quyền
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        // Logic kiểm tra quyền:
        // 1. Lấy thông tin người dùng hiện tại (tương tự như trong submitPurchaseReview)
        // String userIdentifier = ... ;
        // User currentUser = userService.findByUserIdentifier(userIdentifier).orElse(...);
        //
        // 2. Lấy comment cần xóa
        // Optional<Comment> commentOpt = commentService.findCommentById(commentId); // Cần thêm phương thức này vào ICommentService
        // if (commentOpt.isEmpty()) {
        //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found.");
        // }
        // Comment commentToDelete = commentOpt.get();
        //
        // 3. Kiểm tra xem currentUser có quyền xóa commentToDelete không
        //    (ví dụ: currentUser.getUserId().equals(commentToDelete.getUser().getUserId()) || currentUser có vai trò ADMIN)
        // if (!canDelete) {
        //    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this comment.");
        // }

        try {
            // Sau khi đã kiểm tra quyền
            commentService.deleteComment(commentId); // Giả sử deleteComment ném lỗi nếu không tìm thấy
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) { // Ví dụ: CommentNotFoundException từ service
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}