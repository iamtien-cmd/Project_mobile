package vn.iostar.Project_Mobile.DTO;

import com.google.gson.annotations.SerializedName; // Nếu dùng Gson và tên biến khác key JSON

public class CommentRequest {

    private String content;

    // Backend DTO dùng "rating" kiểu Integer
    private Integer rating;

    // Backend DTO dùng "image" hoặc "imageUrl". Giả sử là "imageUrl"
    // Nếu JSON key là "imageUrl" nhưng biến là "image", dùng @SerializedName
    @SerializedName("imageUrl") // Hoặc "image" nếu DTO backend là "image"
    private String image; // Tên biến có thể khác, nhưng @SerializedName quan trọng

    // Backend DTO dùng "productId" kiểu Long
    @SerializedName("productId")
    private Long productIdValue; // Đặt tên biến khác để tránh nhầm lẫn với biến thành viên của Activity

    // Backend DTO dùng "userId" kiểu Long
    @SerializedName("userId")
    private Long userIdValue; // Đặt tên biến khác

    // Constructor
    public CommentRequest(String content, Integer rating, String image, Long productId, Long userId) {
        this.content = content;
        this.rating = rating;
        this.image = image;
        this.productIdValue = productId;
        this.userIdValue = userId;
    }

    // Getters (Gson sẽ dùng để tạo JSON)
    public String getContent() { return content; }
    public Integer getRating() { return rating; }
    public String getImage() { return image; } // Sẽ được serialize với key "imageUrl" do @SerializedName
    public Long getProductIdValue() { return productIdValue; } // Sẽ được serialize với key "productId"
    public Long getUserIdValue() { return userIdValue; }     // Sẽ được serialize với key "userId"
}