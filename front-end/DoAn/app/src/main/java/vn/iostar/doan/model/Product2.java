package vn.iostar.doan.model;

import com.google.gson.annotations.SerializedName; // Import nếu cần

// import lombok.Data;
// @Data
public class Product2 {

    // Các trường đã có
    private long productId;
    // private String image; // Xem giải thích bên dưới
    private String name;
    private double price;
    private String description;
    private int quantity; // Số lượng trong kho
    // private Category category;
    // private List<Comment> comments;
    // private List<ImagesProduct> images; // Nếu có danh sách ảnh

    // === THÊM TRƯỜNG CÒN THIẾU ===
    // Kiểm tra xem API trả về key là "image" hay "imageUrl"
    @SerializedName("image") // Nếu key trong JSON là "image"
    private String imageUrl; // Đặt tên biến là imageUrl cho rõ ràng ở client
    // =============================

    // === TẠO GETTER/SETTER CHO TRƯỜNG MỚI ===
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // --- Getters/Setters cho các trường đã có ---
    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    // ... getters/setters cho category, comments, images nếu cần ...
}