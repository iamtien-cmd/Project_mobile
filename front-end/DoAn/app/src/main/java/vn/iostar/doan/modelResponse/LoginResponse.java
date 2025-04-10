package vn.iostar.doan.modelResponse;

import java.time.LocalDateTime;

import vn.iostar.doan.model.Type;

public class LoginResponse {
    private Long userId;
    private String email;
    private String fullName;
    private boolean active;
    private Type type;
    private String otpCode;
    private LocalDateTime otpExpiration;

}
