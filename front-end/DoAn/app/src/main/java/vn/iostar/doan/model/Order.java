package vn.iostar.doan.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import vn.iostar.doan.model.OrderLine;
import vn.iostar.doan.model.OrderStatus;
import vn.iostar.doan.model.User;

public class Order implements Parcelable {
    private long orderId;
    private double totalPrice;
    private String orderDate;
    private String predictReceiveDate;
    private OrderStatus status; // Đổi lại cho đơn giản
    private String paymentMethod;
    private User user;
    private List<OrderLine> orderLines;
    @Override
    public String toString() {
        return "Order ID: " + orderId + ", Status: " + status + ", Customer: " + (user != null ? user.getEmail() : "Unknown");
    }


    public Order(long orderId, double totalPrice, String orderDate, String predictReceiveDate, OrderStatus status, String paymentMethod, User user, List<OrderLine> orderLines) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
        this.predictReceiveDate = predictReceiveDate;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.user = user;
        this.orderLines = orderLines;
    }

    protected Order(Parcel in) {
        orderId = in.readLong();
        totalPrice = in.readDouble();
        orderDate = in.readString();
        predictReceiveDate = in.readString();
        status = OrderStatus.valueOf(in.readString());
        paymentMethod = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
        orderLines = new ArrayList<>();
        in.readList(orderLines, OrderLine.class.getClassLoader());
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(orderId);
        dest.writeDouble(totalPrice);
        dest.writeString(orderDate);
        dest.writeString(predictReceiveDate);
        dest.writeString(status.name());
        dest.writeString(paymentMethod);
        dest.writeParcelable(user, flags);
        dest.writeList(orderLines);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public List<OrderLine> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getPredictReceiveDate() {
        return predictReceiveDate;
    }

    public void setPredictReceiveDate(String predictReceiveDate) {
        this.predictReceiveDate = predictReceiveDate;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
