package vn.iostar.doan.model;

import java.io.Serializable;

public class Address  {
    private long addressId;
    private String houseNumber;
    private String district;
    private String city;
    private String country;
    private User user;

    public Address() {
    }

    public Address(long addressId, String houseNumber, String district, String city, String country, User user) {
        this.addressId = addressId;
        this.houseNumber = houseNumber;
        this.district = district;
        this.city = city;
        this.country = country;
        this.user = user;
    }

    // Getter và Setter
    public long getAddressId() {
        return addressId;
    }

    public void setAddressId(long addressId) {
        this.addressId = addressId;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
