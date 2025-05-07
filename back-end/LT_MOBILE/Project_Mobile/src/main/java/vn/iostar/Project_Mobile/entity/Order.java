package vn.iostar.Project_Mobile.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString; // Thêm import này nếu chưa có
import vn.iostar.Project_Mobile.util.OrderStatus;
import vn.iostar.Project_Mobile.util.PaymentMethod;
// import vn.iostar.Project_Mobile.entity.User; // Đã có trong @ManyToOne
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "orders") // Đảm bảo tên bảng này khớp với database của bạn
@ToString(exclude = {"user", "orderLines"}) // Giữ nguyên để tránh vòng lặp khi toString
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id") // Thêm @Column nếu tên cột khác orderId
    private long orderId;

    // THÊM TRƯỜNG ITEMS_SUBTOTAL Ở ĐÂY
    @Column(name = "items_subtotal") // Quan trọng: Ánh xạ tới cột 'items_subtotal' trong DB
    private Double itemsSubtotal;    // Sử dụng Double cho giá trị tiền tệ

    @Column(name = "total_price") // Thêm @Column nếu tên cột khác totalPrice
    private double totalPrice;

    @Temporal(TemporalType.DATE)
    @Column(name = "order_date") // Thêm @Column nếu tên cột khác orderDate
    private Date orderDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "predict_receive_date") // Thêm @Column nếu tên cột khác predictReceiveDate
    private Date predictReceiveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status") // Thêm @Column nếu tên cột khác status
    private OrderStatus status;

    @Column(name = "shipping_address") // Thêm @Column nếu tên cột khác shippingAddress
    private String shippingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Đảm bảo đây là tên cột foreign key đúng
    @JsonIgnoreProperties({"password", "token", "otpCode", "otpExpiration",
                           "addresses", "comments", "favorite", "cart", "orders", // Thêm "orders" nếu User có list orders
                           "hibernateLazyInitializer", "handler"})
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true) // Thêm orphanRemoval nếu muốn
    @JsonManagedReference
    private List<OrderLine> orderLines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method") // Thêm @Column nếu tên cột khác paymentMethod
    private PaymentMethod paymentMethod;

    @Column(name = "reviewed") // Thêm @Column nếu tên cột khác reviewed
    private boolean reviewed = false;

    // Lombok sẽ tự tạo getters và setters cho itemsSubtotal
    // Nếu không dùng Lombok Data, bạn cần tự thêm:
    // public Double getItemsSubtotal() {
    //     return itemsSubtotal;
    // }
    //
    // public void setItemsSubtotal(Double itemsSubtotal) {
    //     this.itemsSubtotal = itemsSubtotal;
    // }
}