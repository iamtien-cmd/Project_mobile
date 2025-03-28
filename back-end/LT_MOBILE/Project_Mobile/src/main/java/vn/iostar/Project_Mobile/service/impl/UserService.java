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
		public boolean existsByPhone(String phone) {
	        return userRepository.existsByPhone(phone);
	    }
	    @Override
		public boolean existsByUsername(String username) {
	        return userRepository.existsByUsername(username);
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
		public Optional<User> findByUsername(String username) {
			// TODO Auto-generated method stub
			return userRepository.findByUsername(username);
		}
		public boolean verifyOtpRegister(String email, String otp) {
		    Optional<User> user = userRepository.findByEmail(email);
		    if (user.isPresent()) {
		        // Kiểm tra OTP với dữ liệu đã lưu trong cơ sở dữ liệu
		        return otp.equals(user.get().getOtpCode());
		    }
		    return false;
		}


}
