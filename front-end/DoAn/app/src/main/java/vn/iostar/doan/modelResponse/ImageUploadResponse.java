package vn.iostar.doan.modelResponse; // Hoặc package bạn đã chọn

import com.google.gson.annotations.SerializedName; // Dùng nếu bạn sử dụng Gson làm converter cho Retrofit

public class ImageUploadResponse {

    // QUAN TRỌNG: Tên trong @SerializedName phải KHỚP CHÍNH XÁC
    // với tên key trong JSON mà backend trả về.
    // Nếu backend trả về key là "url" thì phải đổi thành @SerializedName("url")
    @SerializedName("imageUrl")
    private String imageUrl; // Tên biến Java có thể tùy ý, nhưng nên gợi nhớ

    // Constructor mặc định (thường cần thiết cho Gson/Moshi)
    public ImageUploadResponse() {
    }

    // Getter để lấy giá trị URL sau khi Retrofit phân tích JSON
    public String getImageUrl() {
        return imageUrl;
    }

    // Setter thường không cần thiết cho response object,
    // vì Gson/Moshi thường set giá trị trực tiếp vào field.
    // public void setImageUrl(String imageUrl) {
    //     this.imageUrl = imageUrl;
    // }

    // (Optional) toString() để dễ debug
    @Override
    public String toString() {
        return "ImageUploadResponse{" +
                "imageUrl='" + imageUrl + '\'' +
                '}';
    }
}