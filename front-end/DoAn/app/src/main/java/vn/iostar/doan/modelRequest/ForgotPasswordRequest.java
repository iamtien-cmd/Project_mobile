package vn.iostar.doan.modelRequest;

public class ForgotPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
    private String confirmPassword;

    public ForgotPasswordRequest() {
    }

    public ForgotPasswordRequest(String otp, String email) {
        this.otp = otp;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }
}
