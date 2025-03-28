package vn.iostar.doan.entity;

import java.time.LocalDateTime;
import java.util.List;

public class User {
    private int userId;
    private String fullname;
    private LocalDateTime birthday;
    private String phone;
    private String username;
    private String password;
    private String email;
    private List<Favorite> favorite;
    private List<Address> address;

    public User(LocalDateTime birthday, String phone, String fullname, String username, String password, String email, List<Favorite> favorite, List<Address> address) {
        this.birthday = birthday;
        this.phone = phone;
        this.fullname = fullname;
        this.username = username;
        this.password = password;
        this.email = email;
        this.favorite = favorite;
        this.address = address;
    }

    public List<Favorite> getFavorite() {
        return favorite;
    }


    public void setFavorite(List<Favorite> favorite) {
        this.favorite = favorite;
    }



    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
