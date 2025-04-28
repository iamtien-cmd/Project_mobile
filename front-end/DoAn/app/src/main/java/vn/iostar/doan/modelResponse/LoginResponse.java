package vn.iostar.doan.modelResponse;

import java.time.LocalDateTime;
import java.util.List;

import vn.iostar.doan.model.Address;
import vn.iostar.doan.model.Type;

public class LoginResponse {
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

}
