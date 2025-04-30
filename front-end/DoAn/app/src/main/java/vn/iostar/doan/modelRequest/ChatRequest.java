package vn.iostar.doan.modelRequest;

import com.google.gson.annotations.SerializedName;

public class ChatRequest {
    @SerializedName("userMessage") // Phải khớp với tên field trong JSON backend mong đợi
    private String userMessage;

    public ChatRequest(String userMessage) {
        this.userMessage = userMessage;
    }

    // Getter (Gson cần)
    public String getUserMessage() {
        return userMessage;
    }
}
