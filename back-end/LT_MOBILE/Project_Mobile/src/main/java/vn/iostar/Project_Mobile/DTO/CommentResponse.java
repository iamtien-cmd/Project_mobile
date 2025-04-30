package vn.iostar.Project_Mobile.DTO;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentResponse {
    private long commentId;
    private String image;
    private String content;
    private int rating;
    private String fullname; // ➡️ Ai đánh giá
    private LocalDateTime createdAt; // ➡️ Khi nào đánh giá
    private String avatar;
}
