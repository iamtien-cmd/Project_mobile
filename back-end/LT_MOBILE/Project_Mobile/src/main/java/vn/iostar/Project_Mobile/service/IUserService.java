package vn.iostar.Project_Mobile.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.iostar.Project_Mobile.entity.User;

@Service
public interface IUserService {

	Optional<User> findByUsername(String username);


	void saveOtp(User user, String otp);

	Optional<User> findByEmail(String email);

	void saveUser(User user, String otp);

	boolean existsByUsername(String username);

	boolean existsByPhone(String phone);

	boolean emailExists(String email);


	boolean verifyOtpRegister(String email, String otp);

	void saveActive(String email);

}
