package vn.iostar.doan.modelResponse;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {
    // Phải khớp với tên field trong JSON backend trả về
    @SerializedName("userMessage")
    private String userMessage;

    @SerializedName("botReply")
    private String botReply;

    // Getters
    public String getUserMessage() {
        return userMessage;
    }

    public String getBotReply() {
        return botReply;
    }
}
