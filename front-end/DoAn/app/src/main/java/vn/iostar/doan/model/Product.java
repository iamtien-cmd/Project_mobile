package vn.iostar.doan.model;

public class Product {
    private long productId;
    private String image;
    private String name;
    private double price;
    private String description;

    private Category category;

    public Product(long productId) {
        this.productId = productId;
    }

    public Product(long productId, String image, String name, double price, String description, Category category) {
        this.productId = productId;
        this.image = image;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}

