package vn.iostar.doan.model;

import java.time.LocalDateTime;

public class User {
    private long userId;
    private String email;
    private String password;
    private String fullName;
    private String otpCode;
    private LocalDateTime otpExpiration;
    private boolean active;
    private String type;

    public User(long userId, String email, String password, String fullName, String otpCode, LocalDateTime otpExpiration, boolean active, String type) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.otpCode = otpCode;
        this.otpExpiration = otpExpiration;
        this.active = active;
        this.type = type;
    }

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getOtpExpiration() {
        return otpExpiration;
    }

    public void setOtpExpiration(LocalDateTime otpExpiration) {
        this.otpExpiration = otpExpiration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
