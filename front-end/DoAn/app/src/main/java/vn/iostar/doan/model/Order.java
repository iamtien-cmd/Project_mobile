package vn.iostar.doan.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
public class Order {

    @SerializedName("orderId")
    private Long orderId; // Dùng Long (kiểu đối tượng) để có thể là null

    @SerializedName("totalPrice")
    private Double totalPrice; // Dùng Double

    @SerializedName("orderDate")
    private String orderDate; // Nhận là String

    @SerializedName("predictReceiveDate")
    private String predictReceiveDate;

    @SerializedName("itemsSubtotal")
    private Double itemsSubtotal;

    @SerializedName("paymentMethod")
    private PaymentMethod paymentMethod;

    @SerializedName("status")
    private OrderStatus status;

    @SerializedName("shippingAddress")
    private String shippingAddress;

    @SerializedName("user")
    private User user; // Tham chiếu đến lớp User

    @SerializedName("orderLines")
    private List<OrderLine> orderLines;

    public Order(Long orderId, Double totalPrice, String orderDate, String predictReceiveDate, Double itemsSubtotal, PaymentMethod paymentMethod, OrderStatus status, String shippingAddress, User user, List<OrderLine> orderLines) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.predictReceiveDate = predictReceiveDate;
        this.itemsSubtotal = itemsSubtotal;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.user = user;
        this.orderLines = orderLines;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getPredictReceiveDate() {
        return predictReceiveDate;
    }

    public void setPredictReceiveDate(String predictReceiveDate) {
        this.predictReceiveDate = predictReceiveDate;
    }

    public Double getItemsSubtotal() {
        return itemsSubtotal;
    }

    public void setItemsSubtotal(Double itemsSubtotal) {
        this.itemsSubtotal = itemsSubtotal;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<OrderLine> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
    }
}
