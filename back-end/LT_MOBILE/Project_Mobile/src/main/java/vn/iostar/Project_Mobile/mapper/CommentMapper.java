package vn.iostar.Project_Mobile.mapper;

import vn.iostar.Project_Mobile.DTO.CommentResponse;
import vn.iostar.Project_Mobile.entity.Comment;

public class CommentMapper {
    
    public static CommentResponse toCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        
        response.setCommentId(comment.getCommentId());
        response.setImage(comment.getImage());
        response.setContent(comment.getContent());
        response.setRating(comment.getRating());
        response.setCreatedAt(comment.getCreatedAt());
        response.setAvatar(comment.getUser().getAvatar());
        // Lấy username từ User (nếu có)
        if (comment.getUser() != null) {
            response.setFullname(comment.getUser().getFullName());
        } else {
            response.setFullname("Unknown"); // Nếu không có user thì để Unknown
        }
        
        return response;
    }
}
