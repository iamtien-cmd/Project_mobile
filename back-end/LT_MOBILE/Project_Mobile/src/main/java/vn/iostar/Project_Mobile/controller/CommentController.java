package vn.iostar.Project_Mobile.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Import cho @Valid
import vn.iostar.Project_Mobile.DTO.CommentRequestDTO; // Import DTO mới
import vn.iostar.Project_Mobile.DTO.CommentResponse;
import vn.iostar.Project_Mobile.entity.Comment;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.entity.User; // Cần User entity
import vn.iostar.Project_Mobile.service.ICommentService;
import vn.iostar.Project_Mobile.service.IProductService; // Giả sử bạn có ProductService
import vn.iostar.Project_Mobile.service.IUserService;   // Giả sử bạn có UserService

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @Autowired
    private IUserService userService; // Tiêm UserService

    @Autowired
    private IProductService productService; // Tiêm ProductService

    // POST: Thêm bình luận
    @PostMapping
    public ResponseEntity<Comment> createComment(@Valid @RequestBody CommentRequestDTO commentRequestDTO) {
        // Chuyển đổi từ DTO sang Entity Comment
        Comment newComment = new Comment();
        newComment.setContent(commentRequestDTO.getContent());
        newComment.setRating(commentRequestDTO.getRating());
        newComment.setImage(commentRequestDTO.getImage()); // Hoặc imageUrl tùy theo DTO
        newComment.setAvatar(commentRequestDTO.getAvatar()); // Nếu có

        // Lấy User từ userId
        User user = userService.findById(commentRequestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + commentRequestDTO.getUserId())); // Hoặc exception phù hợp hơn
        newComment.setUser(user);

        // Lấy Product từ productId
        Product product = productService.findById(commentRequestDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + commentRequestDTO.getProductId())); // Hoặc exception phù hợp hơn
        newComment.setProduct(product);

        // createdAt sẽ được tự động gán trong Entity Comment

        Comment savedComment = commentService.save(newComment);
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }

    // GET: Lấy bình luận theo sản phẩm
    @GetMapping("/product/{productId}")
    public List<CommentResponse> getCommentsByProductId(@PathVariable Long productId) {
        // Logic này có vẻ ổn, nhưng đảm bảo commentService.getCommentsByProduct
        // thực sự tìm Product bằng ID rồi mới query comment
        Product product = new Product(); // Cách này không tốt lắm để query
        product.setProductId(productId);
        // Tốt hơn là:
        // Product productEntity = productService.findById(productId)
        //       .orElseThrow(() -> new RuntimeException("Product not found"));
        // return commentService.getCommentsByProduct(productEntity);
        return commentService.getCommentsByProduct(product);
    }

   // DELETE: Xóa bình luận theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}