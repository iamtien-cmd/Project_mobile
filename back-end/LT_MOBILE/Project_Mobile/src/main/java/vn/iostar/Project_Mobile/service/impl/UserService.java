package vn.iostar.Project_Mobile.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.repository.IUserRepository;
import vn.iostar.Project_Mobile.service.IUserService;

@Service
public class UserService implements IUserService{
    @Autowired
    private PasswordEncoder passwordEncoder;
	 @Autowired
	    private IUserRepository userRepository;

	    @Override
		public boolean emailExists(String email) {
	        return userRepository.existsByEmail(email);
	    }
	
	    @Override
	    public void saveActive(String email) {
	        Optional<User> userOptional = userRepository.findByEmail(email);

	        if (userOptional.isPresent()) {
	            User user = userOptional.get(); // Lấy đối tượng User từ Optional
	            user.setActive(true); // Cập nhật trường active
	            userRepository.save(user); // Lưu lại vào cơ sở dữ liệu
	        } else {
	            throw new RuntimeException("Không tìm thấy người dùng với email: " + email);
	        }
	    }


	    @Override
		public void saveUser(User user, String otp) {
	    	  String encodedPassword = passwordEncoder.encode(user.getPassword());
	           user.setPassword(encodedPassword);
	        user.setOtpCode(otp);
	        user.setOtpExpiration(LocalDateTime.now().plusMinutes(10));  // OTP có hiệu lực trong 10 phút
	        userRepository.save(user);
	    }

	    @Override
		public Optional<User> findByEmail(String email) {
	        return userRepository.findByEmail(email);
	    }

	    @Override
		public void saveOtp(User user, String otp) {
	    	user.setOtpCode(otp);
	    	user.setOtpExpiration(LocalDateTime.now().plusMinutes(10));  // OTP có hiệu lực trong 10 phút
	        userRepository.save(user);
	    }

	

		@Override
		public boolean verifyOtpForgotPassword(String email, String otp) {
		    Optional<User> userOpt = userRepository.findByEmail(email);
		    if (!userOpt.isPresent()) {
		        return false; // Không tìm thấy user
		    }

		    User user = userOpt.get();
		    // Kiểm tra OTP và thời gian hết hạn
		    if (user.getOtpCode() != null && user.getOtpCode().equals(otp) && user.getOtpExpiration().isAfter(LocalDateTime.now())) {
		        return true;
		    }
		    return false;
		}

		@Override
		public boolean resetPassword(String email, String newPassword) {
		    Optional<User> userOpt = userRepository.findByEmail(email);
		    if (userOpt.isPresent()) {
		        User user = userOpt.get();
		        user.setPassword(newPassword); // Mã hóa mật khẩu nếu cần
		        user.setOtpCode(null); // Xóa OTP sau khi reset thành công
		        user.setOtpExpiration(null); // Xóa thời gian hết hạn OTP
		        userRepository.save(user);
		        return true;
		    }
		    return false;
		}

		@Override
		public boolean verifyOtpRegister(String email, String otp) {
			   Optional<User> userOpt = userRepository.findByEmail(email);
			    if (!userOpt.isPresent()) {
			        return false; // Không tìm thấy user
			    }

			    User user = userOpt.get();
			    // Kiểm tra OTP và thời gian hết hạn
			    if (user.getOtpCode() != null && user.getOtpCode().equals(otp) && user.getOtpExpiration().isAfter(LocalDateTime.now())) {
			        return true;
			    }
			    return false;
		}

		
}
