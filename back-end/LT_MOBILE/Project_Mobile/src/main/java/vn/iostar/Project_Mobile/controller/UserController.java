package vn.iostar.Project_Mobile.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import vn.iostar.Project_Mobile.DTO.ForgotPasswordRequest;
import vn.iostar.Project_Mobile.DTO.LoginRequest;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.service.IEmailService;
import vn.iostar.Project_Mobile.service.IUserService;

@RestController
public class UserController {

    private final IUserService userService;
    private final IEmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Constructor-based injection for userService and emailService
    public UserController(IUserService userService, IEmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        // Tìm kiếm người dùng bằng username
        Optional<User> userOpt = userService.findByEmail(loginRequest.getUsername());

        // Kiểm tra nếu không tìm thấy user hoặc mật khẩu không khớp
        if (!userOpt.isPresent() || 
            !passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Thông tin đăng nhập không chính xác!");
        }

        // Đăng nhập thành công
        return ResponseEntity.ok("Đăng nhập thành công.");
    }

    

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email không tồn tại trong hệ thống.");
        }

        // Generate OTP
        String otp = emailService.generateOTP();

        // Send OTP to the user's email
        emailService.sendOtp(request.getEmail(), otp);

        // Save OTP and expiration time to the database
        userService.saveOtp(userOpt.get(), otp);

        return ResponseEntity.ok("OTP quên mật khẩu đã được gửi qua email. Vui lòng kiểm tra hộp thư đến.");
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ForgotPasswordRequest resetRequest) {
        String email = resetRequest.getEmail();
        String newPassword = resetRequest.getNewPassword();
        String confirmPassword = resetRequest.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu xác nhận không khớp.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        // Cập nhật mật khẩu
        boolean isReset = userService.resetPassword(email, encodedPassword);
        if (isReset) {
            return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể đặt lại mật khẩu. Vui lòng thử lại.");
        }
    }



    @PostMapping("/verifyOtpForgotPassword")
    public ResponseEntity<?> verifyOtpForgotPassword(@RequestBody ForgotPasswordRequest otpRequest) {
        String email = otpRequest.getEmail();
        String otp = otpRequest.getOtp();

        // Kiểm tra OTP từ userService
        boolean isOtpValid = userService.verifyOtpForgotPassword(email, otp);

        if (isOtpValid) {
            return ResponseEntity.ok("OTP hợp lệ. Bạn có thể đặt lại mật khẩu.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP không hợp lệ hoặc đã hết hạn.");
        }
    }

}
