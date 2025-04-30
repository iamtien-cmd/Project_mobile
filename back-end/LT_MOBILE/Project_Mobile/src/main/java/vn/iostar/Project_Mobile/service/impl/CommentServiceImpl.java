package vn.iostar.Project_Mobile.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.iostar.Project_Mobile.DTO.CommentResponse;  
import vn.iostar.Project_Mobile.entity.Comment;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.repository.ICommentRepository;
import vn.iostar.Project_Mobile.service.ICommentService;

@Service
public class CommentServiceImpl implements ICommentService {

    @Autowired
    private ICommentRepository commentRepository;

    @Override
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByProduct(Product product) {  // Thay đổi kiểu trả về
        return commentRepository.findByProduct(product)
                .stream()
                .map(comment -> {  // Chuyển đổi từ Comment entity sang CommentResponse
                    CommentResponse response = new CommentResponse();
                    response.setCommentId(comment.getCommentId());
                    response.setImage(comment.getImage());
                    response.setContent(comment.getContent());
                    response.setRating(comment.getRating());
                    response.setCreatedAt(comment.getCreatedAt());
                    response.setFullname(comment.getUser() != null ? comment.getUser().getFullName() : "Unknown");
                    response.setAvatar(comment.getUser().getAvatar());
                    
                    return response;
                })
                .collect(Collectors.toList());  // Trả về danh sách CommentResponse
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
