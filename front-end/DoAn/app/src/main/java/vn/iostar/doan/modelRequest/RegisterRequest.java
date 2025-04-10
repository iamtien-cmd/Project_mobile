package vn.iostar.doan.modelRequest;

public class RegisterRequest {
    private String email;
    private String otp;

    public RegisterRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }
}
