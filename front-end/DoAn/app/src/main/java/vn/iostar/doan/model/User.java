package vn.iostar.doan.model;

import java.io.Serializable;
import java.util.List;

public class User {
    private long userId;
    private String email;
    private String password;
    private String avatar;
    private String fullName;
    private String phone;
    private String token;
    private String otpCode;
    private String otpExpiration; // LocalDateTime => String
    private boolean active;
    private String type; // Enum => String
    private List<Address> addresses;

    public User() {
    }

    public User(long userId, String email, String password, String avatar, String fullName, String phone, String token, String otpCode, String otpExpiration, boolean active, String type, List<Address> addresses) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.fullName = fullName;
        this.phone = phone;
        this.token = token;
        this.otpCode = otpCode;
        this.otpExpiration = otpExpiration;
        this.active = active;
        this.type = type;
        this.addresses = addresses;
    }

    // Getter và Setter đầy đủ
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getOtpExpiration() {
        return otpExpiration;
    }

    public void setOtpExpiration(String otpExpiration) {
        this.otpExpiration = otpExpiration;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }
}
