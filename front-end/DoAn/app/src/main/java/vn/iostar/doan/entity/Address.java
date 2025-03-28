package vn.iostar.doan.entity;

public class Address {
    private int addressId;
    private String houseNumber;
    private String street;
    private String city;
    private String country;
    private User user;

    public Address(int addressId, String houseNumber, String street, String city, String country, User user) {
        this.addressId = addressId;
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.country = country;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Address(int addressId, String houseNumber, String street, String city, String country) {
        this.addressId = addressId;
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.country = country;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
