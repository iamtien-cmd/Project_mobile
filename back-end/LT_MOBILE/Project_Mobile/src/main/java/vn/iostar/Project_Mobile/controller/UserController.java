package vn.iostar.Project_Mobile.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // Constructor-based injection for userService and emailService
    public UserController(IUserService userService, IEmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOpt = userService.findByUsername(loginRequest.getUsername());
        if (!userOpt.isPresent() || !userOpt.get().getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Thông tin đăng nhập không chính xác!");
        }
        return ResponseEntity.ok("Đăng nhập thành công.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email không tồn tại.");
        }

        // Generate OTP
        String otp = emailService.generateOTP();
        
        // Send OTP to the user's email
        emailService.sendOtp(request.getEmail(),otp);

        // Save OTP to the database for validation later
        userService.saveOtp(userOpt.get(), otp);

        return ResponseEntity.ok("OTP quên mật khẩu đã được gửi qua email.");
    }
}
