package vn.iostar.Project_Mobile.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Address")
@ToString
public class Address {
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private long addressId;
	 
	 	@Column(nullable = true) // Cho phép null nếu dùng chung cho địa chỉ user
	    private String recipientName; // Tên người nhận tại địa chỉ này

	    @Column(nullable = true) // Cho phép null nếu dùng chung cho địa chỉ user
	    private String recipientPhone; // SĐT người nhận tại địa chỉ này

	    // --- Chi tiết địa chỉ ---
	    @Column(nullable = true) // Số nhà, tên đường, ngõ, hẻm...
	    private String streetAddress; // Ví dụ: "189/36 Định Phong Phú" hoặc "Số 203 Đường Điện Biên Phủ"

	    @Column(nullable = true) // Phường / Xã - RẤT QUAN TRỌNG
	    private String ward;          // Ví dụ: "Tăng Nhơn Phú B", "Hoài Đức" (Trong TH Hoài Đức là xã)

	    @Column(nullable = false) // Quận / Huyện / Thị xã / Thành phố thuộc tỉnh
	    private String district;      // Ví dụ: "Thủ Đức", "Hoài Nhơn"

	    @Column(nullable = false) // Tỉnh / Thành phố trực thuộc trung ương
	    private String city;          // Ví dụ: "Hồ Chí Minh", "Bình Định"

	    @Column(nullable = true) // Quốc gia (Có thể mặc định là "Việt Nam" nếu ứng dụng chỉ ở VN)
	    private String country;

	    // --- Trạng thái địa chỉ ---
	    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
	    private boolean isDefault = false; // Đánh dấu địa chỉ mặc định

	    // --- Quan hệ với User ---
	    @ManyToOne(fetch = FetchType.LAZY) // Sử dụng LAZY loading
	    @JoinColumn(name = "user_id", nullable = false) // Khóa ngoại không được null
	    @JsonBackReference // Tránh vòng lặp JSON khi serialize
	    @ToString.Exclude // Tránh vòng lặp khi gọi toString()
	    private User user;

	    // --- (Tùy chọn) Lưu trữ Place ID từ Goong ---
	    @Column(nullable = true)
	    private String goongPlaceId;


}
