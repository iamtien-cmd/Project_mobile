package vn.iostar.Project_Mobile.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Đảm bảo import này đúng
import org.springframework.stereotype.Service;

import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.repository.IUserRepository;
import vn.iostar.Project_Mobile.service.IUserService;

@Service
public class UserServiceImpl implements IUserService { // Đảm bảo implements đúng interface IUserService
    @Autowired
    private PasswordEncoder passwordEncoder; // Sử dụng PasswordEncoder để mã hóa mật khẩu

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
            User user = userOptional.get();
            user.setActive(true);
            userRepository.save(user);
        } else {
            // Cân nhắc ném một exception cụ thể hơn hoặc trả về boolean/status
            throw new RuntimeException("Không tìm thấy người dùng với email: " + email);
        }
    }

    @Override
    public void saveUser(User user, String otp) {
        // Mã hóa mật khẩu trước khi lưu nếu mật khẩu chưa được mã hóa
        // Giả sử mật khẩu được truyền vào user là mật khẩu thô
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) { // Kiểm tra đơn giản xem có vẻ đã mã hóa chưa
             String encodedPassword = passwordEncoder.encode(user.getPassword());
             user.setPassword(encodedPassword);
        }

        user.setOtpCode(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveOtp(User user, String otp) {
        user.setOtpCode(otp);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
    }

    @Override
    public boolean verifyOtpForgotPassword(String email, String otp) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return false;
        }
        User user = userOpt.get();
        return user.getOtpCode() != null && user.getOtpCode().equals(otp) && user.getOtpExpiration().isAfter(LocalDateTime.now());
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // LUÔN MÃ HÓA MẬT KHẨU MỚI TRƯỚC KHI LƯU
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setOtpCode(null);
            user.setOtpExpiration(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean verifyOtpRegister(String email, String otp) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            return false;
        }
        User user = userOpt.get();
        return user.getOtpCode() != null && user.getOtpCode().equals(otp) && user.getOtpExpiration().isAfter(LocalDateTime.now());
    }

    @Override
    public User save(User user) {
        // Cân nhắc mã hóa mật khẩu ở đây nếu user được truyền vào có thể có mật khẩu thô
        // và hàm này được gọi từ nhiều nơi khác nhau.
        // if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
        //    user.setPassword(passwordEncoder.encode(user.getPassword()));
        // }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByToken(String token) {
        // Đảm bảo IUserRepository có phương thức findByToken
        return userRepository.findByToken(token);
    }

    @Override
    public Optional<User> findById(Long id) {
        // SỬA Ở ĐÂY: Gọi phương thức findById của repository
        if (id == null) { // Thêm kiểm tra null cho id
            return Optional.empty();
        }
        return userRepository.findById(id);
    }
}