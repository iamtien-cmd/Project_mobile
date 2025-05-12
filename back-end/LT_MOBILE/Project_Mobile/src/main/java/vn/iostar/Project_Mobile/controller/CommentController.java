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
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.exception.UserNotFoundException; // Ví dụ một custom exception
import vn.iostar.Project_Mobile.service.ICommentService;
import vn.iostar.Project_Mobile.service.IProductService;
import vn.iostar.Project_Mobile.service.IUserService;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IProductService productService;

    // Endpoint cho bình luận chung
    // Client cần gửi userId trong DTO này.
    // Cân nhắc: Endpoint này có nên yêu cầu xác thực không? Nếu có, userId trong DTO có thể không cần thiết.

    @PostMapping
    public ResponseEntity<?> createGeneralComment(
            // *** CÂN NHẮC: Lấy user từ token thay vì DTO nếu endpoint này yêu cầu xác thực ***
            // @RequestHeader("Authorization") String authHeader, // Ví dụ
            @Valid @RequestBody CommentRequestDTO commentRequestDTO) {
        try {
            // *** CÂN NHẮC LẠI LOGIC LẤY USER ***
            // Nếu yêu cầu xác thực, lấy user từ token sẽ an toàn hơn
            // String token = authHeader.replace("Bearer ", "").trim();
            // User user = userService.findByToken(token).orElseThrow(...);
            // Nếu không yêu cầu xác thực và tin tưởng userId từ DTO:
            User user = userService.findById(commentRequestDTO.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + commentRequestDTO.getUserId()));

            // Lấy Product
            Product product = productService.findById(commentRequestDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + commentRequestDTO.getProductId())); // Nên dùng ProductNotFoundException

            // Kiểm tra đã review chưa (Logic này có thể giữ lại hoặc chuyển vào service)
            // if (commentService.hasUserReviewedProduct(user, product)) { // hasUserReviewedProduct cần được định nghĩa đúng
            //     return ResponseEntity.status(HttpStatus.CONFLICT).body("You have already reviewed this product.");
            // }

            // Tạo đối tượng Comment Entity
            Comment commentEntity = new Comment();
            commentEntity.setContent(commentRequestDTO.getContent());
            commentEntity.setRating(commentRequestDTO.getRating());
            commentEntity.setImage(commentRequestDTO.getImage());
            commentEntity.setUser(user);
            // commentEntity.setAvatar(user.getAvatar()); // Không cần set ở đây nếu bạn lấy từ user trong CommentResponse
            commentEntity.setProduct(product);
            // commentEntity.setReviewed(true); // Không cần set ở đây nữa, vì mục đích của nó là đánh dấu comment đã xử lý trong Order logic

            // *** THAY ĐỔI CHÍNH Ở ĐÂY ***
            // Comment savedComment = commentService.save(commentEntity); // <<< KHÔNG DÙNG CÁI NÀY
            Comment savedComment = commentService.createComment(commentEntity); // <<< GỌI PHƯƠNG THỨC ĐÚNG CÓ CHECK ORDER

            // Trả về response
            return new ResponseEntity<>(savedComment, HttpStatus.CREATED); // Có thể trả về DTO response thay vì entity

        } catch (RuntimeException e) { // Bắt các lỗi cụ thể hơn
             if (e.getMessage().contains("Product not found")) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
             } else if (e instanceof UserNotFoundException) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
             } else {
                // Các lỗi khác như đã review...
                return ResponseEntity.badRequest().body(e.getMessage());
             }
        } catch (Exception e) {
             System.err.println("Unexpected error creating comment: " + e.getMessage()); // Log lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
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
            if (responses.isEmpty()) {
                return ResponseEntity.ok("No comments found for this product."); // Hoặc trả về danh sách rỗng
            }
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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