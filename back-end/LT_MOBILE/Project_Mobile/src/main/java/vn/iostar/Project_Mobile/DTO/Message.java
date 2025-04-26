package vn.iostar.Project_Mobile.DTO;

public class Message {
    private String userMessage;
    private String botReply;

    // Constructors
    public Message() {}
    public Message(String userMessage, String botReply) {
        this.userMessage = userMessage;
        this.botReply = botReply;
    }

    // Getters & Setters
    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getBotReply() {
        return botReply;
    }

    public void setBotReply(String botReply) {
        this.botReply = botReply;
    }
}