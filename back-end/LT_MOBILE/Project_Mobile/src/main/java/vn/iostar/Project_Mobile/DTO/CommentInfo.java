package vn.iostar.Project_Mobile.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Hoặc Date, tùy thuộc vào kiểu dữ liệu trong entity Comment

// DTO cho thông tin bình luận
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentInfo {
    private long commentId;
    private String image;
	private String content;
	private int rating;

    private UserInfo user;
}