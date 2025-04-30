package vn.iostar.Project_Mobile.controller.auth;



import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.time.LocalDateTime;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private IEmailService emailService;

	@Autowired
	private IUserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

@Autowired
private IUserRepository userRepository;


	    @Autowired
	    public AuthController(IEmailService emailService, IUserService userService,
	                          PasswordEncoder passwordEncoder, IUserRepository userRepository) {
	        this.emailService = emailService;
	        this.userService = userService;
	        this.passwordEncoder = passwordEncoder;
	        this.userRepository = userRepository;
	    }

    public void saveUser(User user) {
        // Mã hóa mật khẩu trước khi lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }


    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        // Kiểm tra xem email đã tồn tại chưa
        if (userService.emailExists(user.getEmail())) {
        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // Tạo OTP và gửi qua email
        String otp = emailService.generateOTP();

        // Ensure this method sends the OTP to the user's email address
        emailService.sendRegisterOtp(user.getEmail(), otp);
        
        // Lưu OTP và trạng thái chưa xác nhận vào cơ sở dữ liệu
        userService.saveUser(user, otp);
       

        return ResponseEntity.ok(user);
    }
    @PostMapping("/verifyOtpRegister")
    public ResponseEntity<User> verifyOtp(@RequestBody RegisterRequest otpRequest) {
        String email = otpRequest.getEmail();
        String otp = otpRequest.getOtp();
        
        if (userService.verifyOtpRegister(email, otp)) {
            userService.saveActive(email);
            Optional<User> userOptional = userService.findByEmail(email);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get(); // lấy ra User từ Optional
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Không tìm thấy user
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // OTP không hợp lệ
        }
    }



}
