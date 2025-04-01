package vn.iostar.Project_Mobile.service.impl;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.iostar.Project_Mobile.service.IEmailService;
@Service
public class EmailServiceImpl  implements IEmailService {

	 @Autowired
	    private JavaMailSender javaMailSender;

	    @Override
	    public void sendOtp(String email, String otp) {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(email);
	        message.setSubject("Lấy lại mật khẩu ");
	        message.setText("Your OTP code is: " + otp);
	        
	        // Gửi email
	        javaMailSender.send(message);
	    }
	    @Override
	    public void sendRegisterOtp(String email, String otp) {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(email);
	        message.setSubject("Xác nhận đăng kí tài khoản ");
	        message.setText("Your OTP code is: " + otp);
	        
	        // Gửi email
	        javaMailSender.send(message);
	    }
		@Override
		public String generateOTP() {
			 int length = 6; // Độ dài của OTP
		        StringBuilder otp = new StringBuilder();
		        Random random = new Random();

		        for (int i = 0; i < length; i++) {
		            int digit = random.nextInt(10); // Tạo số ngẫu nhiên từ 0-9
		            otp.append(digit);
		        }

		        return otp.toString();
		}
		@Override
		public void sendForgotPasswordOtp(String email, String otp) {
			// TODO Auto-generated method stub
			SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(email);
	        message.setSubject("Xác nhận mã quên mật khẩu ");
	        message.setText("Your OTP code is: " + otp);
	        
	        // Gửi email
	        javaMailSender.send(message);
			
		}
}
