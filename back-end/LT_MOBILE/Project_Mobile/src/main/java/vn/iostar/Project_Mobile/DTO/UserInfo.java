package vn.iostar.Project_Mobile.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO đơn giản cho thông tin người dùng trong bình luận
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
	  private long userId;
	    private String name;
	    private String avatar; 
}
