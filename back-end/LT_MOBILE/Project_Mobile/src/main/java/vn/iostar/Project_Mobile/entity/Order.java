package vn.iostar.Project_Mobile.entity;

// === THÊM CÁC IMPORT CẦN THIẾT ===
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.iostar.Project_Mobile.util.OrderStatus;
import vn.iostar.Project_Mobile.util.PaymentMethod;
import vn.iostar.Project_Mobile.entity.User;
import java.util.Date; // <<< Đổi sang java.util.Date
import java.util.List;
import java.util.ArrayList; // <<< Import nếu khởi tạo list rỗng

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderId;

    private double totalPrice;

    // === SỬA KIỂU DATE VÀ THÊM @Temporal ===
    @Temporal(TemporalType.DATE) // Chỉ lưu ngày
    private Date orderDate;

    @Temporal(TemporalType.DATE)
    private Date predictReceiveDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String shippingAddress;
    // === THÊM FetchType.LAZY VÀ @JsonIgnoreProperties CHO USER ===
    @ManyToOne(fetch = FetchType.LAZY) // <<< Tải trễ User
    @JoinColumn(name = "user_id")
    // Bỏ qua các trường không cần thiết hoặc gây lỗi từ User khi serialize Order
    // Thêm các trường bạn *không* muốn thấy từ User vào đây
    @JsonIgnoreProperties({"password", "token", "otpCode", "otpExpiration",
                           "addresses", "comments", "favorite", "cart",
                           "hibernateLazyInitializer", "handler"}) // Bỏ qua các trường nhạy cảm, các list và proxy
    private User user;

    // === THÊM @JsonManagedReference, FetchType.LAZY, CascadeType ===
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // <<< Tải trễ OrderLine
    @JsonManagedReference // Phía chính của quan hệ Order <-> OrderLine
    private List<OrderLine> orderLines = new ArrayList<>(); // Khởi tạo để tránh NullPointerException
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    // Thêm trường này nếu cần cho logic review ở frontend
    private boolean reviewed = false;
}