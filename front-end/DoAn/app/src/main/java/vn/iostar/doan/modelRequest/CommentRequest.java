package vn.iostar.doan.modelRequest;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

import vn.iostar.doan.model.Product;
import vn.iostar.doan.model.User1;

public class CommentRequest {
    private String content;
    private int rating; // Giữ lại nếu server của bạn chấp nhận và bạn muốn gửi
    private String image;  // Giữ lại nếu server chấp nhận và bạn muốn gửi (có thể là null)

    private LocalDateTime createAt;
    @SerializedName("product") // Đảm bảo tên JSON key là "product"
    private Product productRef; // Sử dụng lớp ProductRef

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setProductRef(Product productRef) {
        this.productRef = productRef;
    }

    public void setUserRef(User1 userRef) {
        this.userRef = userRef;
    }

    @SerializedName("user") // Đảm bảo tên JSON key là "user"
    private User1 userRef;     // Sử dụng lớp UserRef

    // Bỏ qua createdAt vì thường do server tạo

    // Constructor để dễ tạo đối tượng từ Activity
    public CommentRequest(String content, int rating, String image, long productId, long userId) {
        this.content = content;
        this.rating = rating;   // Có thể bạn không cần gửi rating khi tạo? Xác nhận với backend.
        this.image = image;     // Có thể bạn không cần gửi image khi tạo?
        this.productRef = new Product(productId);
        this.userRef = new User1(userId);
    }

    // Getters (không bắt buộc cho việc gửi đi với Gson/Moshi, nhưng có thể hữu ích)
    public String getContent() { return content; }
    public int getRating() { return rating; }
    public String getImage() { return image; }
    public Product getProductRef() { return productRef; }
    public User1 getUserRef() { return userRef; }
}
