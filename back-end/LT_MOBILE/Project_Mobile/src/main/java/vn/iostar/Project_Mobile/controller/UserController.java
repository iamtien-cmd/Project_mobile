package vn.iostar.Project_Mobile.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import vn.iostar.Project_Mobile.DTO.ForgotPasswordRequest;
import vn.iostar.Project_Mobile.DTO.LoginRequest;
import vn.iostar.Project_Mobile.DTO.UserUpdateDTO;
import vn.iostar.Project_Mobile.entity.Address;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.repository.AddressRepository;
import vn.iostar.Project_Mobile.service.IEmailService;
import vn.iostar.Project_Mobile.service.IUserService;

@RestController
@RequestMapping("/api/auth")
public class UserController {

	@Autowired
	private AddressRepository addressRepository;

	 private final IUserService userService;
	    private final IEmailService emailService;
	    private final PasswordEncoder passwordEncoder;
	    @Autowired
	    public UserController(IUserService userService, IEmailService emailService, PasswordEncoder passwordEncoder) {
	        this.userService = userService;
	        this.emailService = emailService;
	        this.passwordEncoder = passwordEncoder;
	    }

	    @PostMapping("/login")
	    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
	        Optional<User> userOpt = userService.findByEmail(loginRequest.getEmail());

	        if (!userOpt.isPresent()) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body("Email không tồn tại trong hệ thống!");
	        }

	        User user = userOpt.get();

	        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body("Mật khẩu không chính xác!");
	        }

	        String token = UUID.randomUUID().toString();

	        // ✅ Lưu token đó vào DB (thêm cột token trong bảng users)
	        user.setToken(token);
	        userService.save(user);
	        
	        // Log dữ liệu nếu bạn muốn kiểm tra
	        System.out.println("User từ database: " + user);

	        // Trả về dữ liệu người dùng
	        Map<String, Object> response = new HashMap<>();
	        response.put("message", "Đăng nhập thành công.");
	        response.put("email", user.getEmail());
	        response.put("fullName", user.getFullName());
	        response.put("userType", user.getType()); // hoặc role gì đó nếu có
	        // response.put("password", user.getPassword()); // ❌ Không nên gửi password

	        return ResponseEntity.ok(response);
	    }
	    // Lấy thông tin của user để hiển thị
	    @GetMapping("/info")
	    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authHeader) {
	        String token = authHeader.replace("Bearer ", "").trim();

	        Optional<User> userOpt = userService.findByToken(token);
	        if (!userOpt.isPresent()) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ.");
	        }

	        User user = userOpt.get();
	        Map<String, Object> response = new HashMap<>();
	        response.put("email", user.getEmail());
	        response.put("fullName", user.getFullName());
	        response.put("phone", user.getPhone());
	        response.put("avatar", user.getAvatar());
	        List<Map<String, Object>> addressList = new ArrayList<>();
	        if (user.getAddresses() != null) {
	            for (Address addr : user.getAddresses()) {
	                Map<String, Object> addressMap = new HashMap<>();
	                addressMap.put("addressId", addr.getAddressId()); 
	                addressMap.put("houseNumber", addr.getHouseNumber());
	                addressMap.put("street", addr.getStreet());
	                addressMap.put("city", addr.getCity());
	                addressMap.put("country", addr.getCountry());
	                addressList.add(addressMap);
	            }
	        }
	        response.put("addresses", addressList);
	        return ResponseEntity.ok(response);
	    }

	    //update thông tin
	    @PutMapping("/update")
	    public ResponseEntity<?> updateUser(
	            @RequestHeader("Authorization") String authHeader,
	            @RequestBody UserUpdateDTO request
	    ) {
	        String token = authHeader.replace("Bearer ", "").trim();
	        Optional<User> userOpt = userService.findByToken(token);

	        if (!userOpt.isPresent()) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ.");
	        }

	        User user = userOpt.get();
	        user.setFullName(request.getFullName());
	        user.setAvatar(request.getAvatar());
	        user.setPhone(request.getPhone());

	        if (request.getAddressIds() != null) {
	            List<Address> addressList = addressRepository.findAllById(request.getAddressIds());
	            user.setAddresses(addressList);
	        }

	        userService.save(user);

	        return ResponseEntity.ok("Cập nhật thông tin thành công!");
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
