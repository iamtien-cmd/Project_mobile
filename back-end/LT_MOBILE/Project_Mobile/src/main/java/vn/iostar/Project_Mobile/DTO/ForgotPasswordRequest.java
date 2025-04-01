package vn.iostar.Project_Mobile.DTO;




import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
    private String confirmPassword;
}
