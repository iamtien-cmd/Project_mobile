package vn.iostar.Project_Mobile.service;

public interface IEmailService {
    void sendOtp(String email, String otp);

	void sendRegisterOtp(String email, String otp);

	String generateOTP();
}
