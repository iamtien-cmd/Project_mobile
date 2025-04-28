package vn.iostar.doan.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Category {
    // Giữ tên biến Java là categoryId nhưng ánh xạ đúng với JSON key "categoryId"
    @SerializedName("categoryId")
    private long categoryId;

    // Giữ tên biến Java là name và ánh xạ đúng với JSON key "name"
    @SerializedName("name")
    private String name;

    // Giữ tên biến Java là imageCate và ánh xạ đúng với JSON key "imageCate"
    @SerializedName("imageCate")
    private String imageCate;


    public Category() {
    }

    public Category(long categoryId, String name, String imageCate) {
        this.categoryId = categoryId;
        this.name = name;
        this.imageCate = imageCate;
    }

    // Getters and Setters giữ nguyên
    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getImageCate() {
        return imageCate;
    }

    public void setImageCate(String imageCate) {
        this.imageCate = imageCate;
    }
}