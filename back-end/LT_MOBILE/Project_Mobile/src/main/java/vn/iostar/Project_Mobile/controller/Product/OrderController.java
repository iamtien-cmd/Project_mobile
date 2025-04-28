package vn.iostar.Project_Mobile.controller.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Dùng Spring Security
import org.springframework.security.core.context.SecurityContextHolder; // Dùng Spring Security
import org.springframework.web.bind.annotation.*;
import vn.iostar.Project_Mobile.DTO.CreateOrderRequest;
import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.service.IOrderService;
import vn.iostar.Project_Mobile.service.impl.UserServiceImpl; // Giả sử có UserService để lấy User

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/order")
public class OrderController {

	@Autowired
	private IOrderService orderService;

	@Autowired
	private UserServiceImpl userService; // Để lấy thông tin user đang đăng nhập

	
	public OrderController(IOrderService orderService, UserServiceImpl userService) {
		this.orderService = orderService;
		this.userService = userService;
	}


	@PostMapping("/createOrder")
	public ResponseEntity<?> createOrder( @RequestHeader("Authorization") String authHeader, @RequestBody CreateOrderRequest request) {
		try {
			String token = authHeader.replace("Bearer ", "").trim();
			System.out.println("TOKEN: " + token);
			Optional<User> userOpt = userService.findByToken(token);
			System.out.println("USER: " + userOpt);
			if (!userOpt.isPresent()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ.");
			}

			User currentUser = userOpt.get(); // Lấy thông tin người dùng từ token

				// Tạo đơn hàng mới
			Order createdOrder = orderService.createOrder(currentUser, request);

				// Trả về thông tin đơn hàng vừa tạo
			return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);

		} catch (NoSuchElementException | IllegalArgumentException e) {
				// Lỗi do người dùng cung cấp sai dữ liệu (ID không tồn tại, thiếu thông tin)
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (IllegalStateException e) {
				// Lỗi nghiệp vụ (hết hàng, cart không tìm thấy)
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (Exception e) {
				// Lỗi server không mong muốn
			e.printStackTrace(); // In lỗi ra console (chỉ dùng khi dev)
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while creating the order.");
		}
	}

	// --- CÁC ENDPOINT KHÁC CHO ORDER ---
	@GetMapping("/{orderId}")
	public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId, @RequestHeader("Authorization") String authHeader) {
	    String token = authHeader.replace("Bearer ", "").trim();
	    Optional<User> userOpt = userService.findByToken(token);

	    if (userOpt.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ.");
	    }

	    try {
	        Order order = orderService.getOrderDetailsById(orderId, userOpt.get());
	        return ResponseEntity.ok(order);
	    } catch (NoSuchElementException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Đơn hàng không tồn tại.");
	    } catch (IllegalStateException e) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền xem đơn hàng này.");
	    }
	}


	// @GetMapping("/{orderId}")
	// public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId) { ... }
	// --- ---
}
