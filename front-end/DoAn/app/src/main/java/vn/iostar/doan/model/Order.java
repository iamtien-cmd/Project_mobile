package vn.iostar.doan.model; // Đảm bảo đúng package

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
// import java.util.Date; // Không cần nếu dùng String cho date
import java.util.Date;
import java.util.List;

public class Order {

    @SerializedName("orderId")
    private long orderId;

    @SerializedName("totalPrice")
    private double totalPrice;

    @SerializedName("orderDate") // Key từ JSON
    private Date orderDate;     // Lưu dưới dạng String

    @SerializedName("predictReceiveDate") // Key từ JSON
    private Date predictReceiveDate;  // Lưu dưới dạng String

    @SerializedName("status")
    private OrderStatus status; // Đảm bảo enum OrderStatus được định nghĩa

    @SerializedName("user") // Nếu bạn cần thông tin user
    private User user;      // Đảm bảo User.java POJO được định nghĩa

    @SerializedName("orderLines")
    private List<OrderLine> orderLines = new ArrayList<>(); // Đảm bảo OrderLine.java POJO

    @SerializedName("paymentMethod")
    private PaymentMethod paymentMethod;

    @SerializedName("reviewed")
    private boolean reviewed;

    @SerializedName("shippingAddress")
    private String shippingAddress; // Giữ tên biến đơn giản nếu key JSON là "shippingAddress"

    @SerializedName("itemsSubtotal") // Hoặc "items_subtotal" nếu key JSON là vậy
    private double itemsSubtotal;

    @SerializedName("shippingFee") // Hoặc "shipping_fee"
    private double shippingFee;

    @SerializedName("discountAmount") // Hoặc "discount_amount"
    private double discountAmount;

    @SerializedName("voucherCode") // Hoặc "voucher_code"
    private String voucherCode;

    // --- Constructors (nếu cần) ---
    public Order() {
    }

    // --- Getters and Setters ---
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public Date getOrderDate() { return orderDate; } // Trả về String
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public Date getPredictReceiveDate() { return predictReceiveDate; } // Trả về String
    public void setPredictReceiveDate(Date predictReceiveDate) { this.predictReceiveDate = predictReceiveDate; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public User getUser() { return user; } // Nếu có User
    public void setUser(User user) { this.user = user; }

    public List<OrderLine> getOrderLines() { return orderLines; }
    public void setOrderLines(List<OrderLine> orderLines) { this.orderLines = orderLines; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public double getItemsSubtotal() { return itemsSubtotal; }
    public void setItemsSubtotal(double itemsSubtotal) { this.itemsSubtotal = itemsSubtotal; }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    // Enum PaymentMethod (ví dụ, nên tách ra file riêng nếu phức tạp)
    public enum PaymentMethod {
        COD, VNPAY // Đảm bảo giá trị này khớp với giá trị chuỗi từ JSON
    }

    // Enum OrderStatus (ví dụ, nên tách ra file riêng)
    // public enum OrderStatus {
    //     PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELED, RECEIVED, WAITING, ERROR, REVIEWED
    // }
}