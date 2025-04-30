package vn.iostar.doan.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private long userId;
    private String email;
    private String otp;
    private String password;
    private String fullName;

    public User(long userId) {
        this.userId = userId;
    }

    public User() {
    }

    public User(long userId, String email, String password, String fullName, String otp) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.otp = otp;
    }

    protected User(Parcel in) {
        userId = in.readLong();
        email = in.readString();
        password = in.readString();
        fullName = in.readString();
        otp =  in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(userId);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(fullName);
        dest.writeString(otp);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters và setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
