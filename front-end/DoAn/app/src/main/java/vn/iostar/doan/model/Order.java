package vn.iostar.doan.model; // Đảm bảo đúng package

import com.google.gson.annotations.SerializedName; // <<< Thêm import nếu tên JSON khác tên biến

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Giả sử bạn dùng @Data của Lombok hoặc tự tạo getter/setter
// import lombok.Data;
// @Data
public class Order {

    // Các trường đã có
    private long orderId;
    private double totalPrice;
    private Date orderDate;
    private Date predictReceiveDate;
    private OrderStatus status;
    // private User user; // Có thể có hoặc không tùy nhu cầu
    private List<OrderLine> orderLines = new ArrayList<>();
    private PaymentMethod paymentMethod; // Giả sử có enum PaymentMethod
    private boolean reviewed;

    // === THÊM CÁC TRƯỜNG CÒN THIẾU ===
    @SerializedName("shippingAddress") // Dùng nếu tên JSON key là "shippingAddress"
    private String shippingAddressString; // Đổi tên biến để phân biệt nếu bạn có đối tượng Address riêng

    // Hoặc nếu API trả về đối tượng Address lồng nhau:
    // private Address shippingAddress;

    @SerializedName("itemsSubtotal") // Dùng nếu key trong JSON là "itemsSubtotal"
    private double itemsSubtotal;

    @SerializedName("shippingFee")
    private double shippingFee;

    @SerializedName("discountAmount")
    private double discountAmount;

    @SerializedName("voucherCode")
    private String voucherCode;
    // ===================================

    // === TẠO GETTERS CHO CÁC TRƯỜNG MỚI ===
    // (Hoặc dùng Lombok @Getter trên class)

    public String getShippingAddress() { // <<< GETTER CHO shippingAddressString
        return shippingAddressString;
    }

    public void setShippingAddress(String shippingAddressString) {
        this.shippingAddressString = shippingAddressString;
    }

    // public Address getShippingAddress() { // <<< GETTER NẾU DÙNG ĐỐI TƯỢNG Address
    //     return shippingAddress;
    // }
    //
    // public void setShippingAddress(Address shippingAddress) {
    //     this.shippingAddress = shippingAddress;
    // }


    public double getItemsSubtotal() {
        return itemsSubtotal;
    }

    public void setItemsSubtotal(double itemsSubtotal) {
        this.itemsSubtotal = itemsSubtotal;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    // --- Getters/Setters cho các trường đã có ---
    // (Đảm bảo bạn đã có chúng)
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
    // public User getUser() { return user; }
    // public void setUser(User user) { this.user = user; }
    public List<OrderLine> getOrderLines() { return orderLines; }
    public void setOrderLines(List<OrderLine> orderLines) { this.orderLines = orderLines; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }

    // Enum PaymentMethod (ví dụ)
    public enum PaymentMethod {
        COD, VNPAY
    }
}