package vn.iostar.doan.model;

public class Address  {
    private long addressId;
    private String houseNumber;
    private String district;
    private String city;
    private String country;
    private User1 user;

    public Address() {
    }

    public Address(long addressId, String houseNumber, String district, String city, String country, User1 user) {
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

    public User1 getUser() {
        return user;
    }

    public void setUser(User1 user) {
        this.user = user;
    }
}
