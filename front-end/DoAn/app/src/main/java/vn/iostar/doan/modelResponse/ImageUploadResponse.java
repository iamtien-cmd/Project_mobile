package vn.iostar.doan.modelResponse;

 import com.google.gson.annotations.SerializedName;
 public class ImageUploadResponse {
     @SerializedName("imageUrl") private String imageUrl;
     public String getImageUrl() { return imageUrl; }

     public void setImageUrl(String imageUrl) {
         this.imageUrl = imageUrl;
     }
     // Cần setter nếu dùng Gson mặc định
 }