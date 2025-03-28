package vn.iostar.Project_Mobile.service.impl;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.repository.IUserRepository;

@Service
public class AuthenticationService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IUserRepository userRepository;

    public boolean authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Kiểm tra mật khẩu đã mã hóa với mật khẩu nhập vào
            return passwordEncoder.matches(password, user.getPassword());
        }


        return false;
    }
}
