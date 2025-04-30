package vn.iostar.Project_Mobile.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import vn.iostar.Project_Mobile.DTO.CommentResponse;
import vn.iostar.Project_Mobile.entity.Comment;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.service.ICommentService;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    // POST: Thêm bình luận
    @PostMapping
    public Comment createComment(@RequestBody Comment comment) {
        return commentService.save(comment);
    }

    // GET: Lấy bình luận theo sản phẩm
    @GetMapping("/product/{productId}")
    public List<CommentResponse> getCommentsByProductId(@PathVariable Long productId) {
        // Gọi service để lấy danh sách comments cho product
        Product product = new Product();
        product.setProductId(productId);  // Giả sử bạn tìm Product theo ID
        return commentService.getCommentsByProduct(product);
    }

   // DELETE: Xóa bình luận theo ID
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
    }
}
