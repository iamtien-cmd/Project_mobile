package vn.iostar.doan.model;

import java.util.Date; // Hoặc dùng String tùy theo API/Gson
import java.util.List;

public class Order {
    private long orderId;
    private double totalPrice;
    private Date orderDate; // Hoặc String
    private Date predictReceiveDate; // Hoặc String
    private OrderStatus status; // Enum
    private String paymentMethod;
    private List<OrderLine> orderLines;

    // *** THÊM TRƯỜNG NÀY ***
    private boolean reviewed; // Lưu trạng thái đã đánh giá

    // Getters and Setters cho các trường cũ
    // ... (Giữ nguyên các getter/setter cũ) ...

    // *** THÊM GETTER VÀ SETTER CHO TRƯỜNG MỚI ***
    public boolean isReviewed() { // Getter cho boolean thường bắt đầu bằng 'is'
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    // Các getter/setter còn lại
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public Date getPredictReceiveDate() { return predictReceiveDate; }
    public void setPredictReceiveDate(Date predictReceiveDate) { this.predictReceiveDate = predictReceiveDate; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<OrderLine> getOrderLines() { return orderLines; }
    public void setOrderLines(List<OrderLine> orderLines) { this.orderLines = orderLines; }
}