package vn.iostar.Project_Mobile.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity; // Import ResponseEntity
import org.springframework.web.bind.annotation.*;

import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.exception.ResourceNotFoundException;
import vn.iostar.Project_Mobile.service.IOrderService;
// Import các Exception tùy chỉnh nếu bạn tạo (xem bước 3)
// import vn.iostar.Project_Mobile.exception.ResourceNotFoundException;
// import vn.iostar.Project_Mobile.exception.OrderCannotBeCancelledException;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    // API: Xem danh sách trạng thái đơn hàng của user
    @GetMapping("/status/{userId}")
    public ResponseEntity<List<Order>> getOrderStatusesByUser(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            if (orders == null || orders.isEmpty()) {
                // Trả về 204 No Content hoặc 404 Not Found nếu không có đơn hàng
                 return ResponseEntity.noContent().build();
                // Hoặc: return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            // Ghi log lỗi ở đây nếu cần
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Trả về lỗi server
        }
    }

    // === API MỚI: Hủy đơn hàng ===
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrderById(@PathVariable Long orderId) {
        try {
            // Gọi phương thức service để xử lý việc hủy đơn
            Order cancelledOrder = orderService.cancelOrder(orderId);
            // Trả về 200 OK cùng với đơn hàng đã được cập nhật trạng thái
            return ResponseEntity.ok(cancelledOrder);
        } catch (ResourceNotFoundException e) { // Bắt lỗi không tìm thấy đơn hàng
            // Trả về 404 Not Found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) { // Bắt lỗi không thể hủy (ví dụ: sai trạng thái)
             // Trả về 400 Bad Request hoặc 409 Conflict đều hợp lý
             // 400: Yêu cầu không hợp lệ do trạng thái đơn hàng
             // 409: Xung đột trạng thái, không thể thực hiện hành động
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Bắt các lỗi không mong muốn khác
            // Log lỗi ở đây (ví dụ: log.error("Error cancelling order: {}", orderId, e);)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Đã xảy ra lỗi không mong muốn khi hủy đơn hàng.");
        }
    }
    // === KẾT THÚC API MỚI ===
}