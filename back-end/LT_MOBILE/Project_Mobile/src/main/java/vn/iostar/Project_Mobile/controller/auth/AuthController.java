package vn.iostar.Project_Mobile.controller.auth;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.iostar.Project_Mobile.DTO.RegisterRequest;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.repository.IUserRepository;
import vn.iostar.Project_Mobile.service.IEmailService;
import vn.iostar.Project_Mobile.service.IUserService;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IEmailService emailService;

    @Autowired
    private IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        // Kiểm tra xem email đã tồn tại chưa
        if (userService.emailExists(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email đã tồn tại!");
        }

        // Tạo OTP và gửi qua email
        String otp = emailService.generateOTP();

        // Ensure this method sends the OTP to the user's email address
        emailService.sendRegisterOtp(user.getEmail(), otp);
        
        // Lưu OTP và trạng thái chưa xác nhận vào cơ sở dữ liệu
        userService.saveUser(user, otp);

        return ResponseEntity.ok("OTP đăng kí đã được gửi qua email.");
    }
    @PostMapping("/verifyOtpRegister")
    public ResponseEntity<?> verifyOtp(@RequestBody RegisterRequest otpRequest) {
        String email = otpRequest.getEmail();
        String otp = otpRequest.getOtp();
        
        // Kiểm tra OTP
        boolean isOtpValid = userService.verifyOtpRegister(email, otp);

        if (isOtpValid) {
            userService.saveActive(email);
            return ResponseEntity.ok("OTP hợp lệ, đăng ký thành công.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP không hợp lệ.");
        }
    }

}
