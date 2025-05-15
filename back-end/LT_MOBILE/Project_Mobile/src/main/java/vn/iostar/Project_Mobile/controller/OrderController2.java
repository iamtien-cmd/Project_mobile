package vn.iostar.Project_Mobile.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.iostar.Project_Mobile.DTO.MessageResponseDTO;
import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.exception.ResourceNotFoundException;
import vn.iostar.Project_Mobile.service.IOrderService;
import vn.iostar.Project_Mobile.util.OrderStatus;


@RestController
@RequestMapping("/api/orders")
public class OrderController2 { // Đã đổi tên để tránh trùng với OrderController gốc nếu có

    private static final Logger controllerLogger = LoggerFactory.getLogger(OrderController2.class);

    @Autowired
    private IOrderService orderService;

    // API: Xem danh sách trạng thái đơn hàng của user
    @GetMapping("/status/{userId}")
    public ResponseEntity<List<Order>> getOrderStatusesByUser(@PathVariable Long userId) {
        try {
            controllerLogger.info("Fetching orders for UserID: {}", userId);
            List<Order> orders = orderService.getOrdersByUserId(userId);
            if (orders == null || orders.isEmpty()) {
                 controllerLogger.info("No orders found for UserID: {}", userId);
                 return ResponseEntity.noContent().build();
            }
            controllerLogger.info("Found {} orders for UserID: {}", orders.size(), userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            controllerLogger.error("Error fetching orders for UserID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetailById(@PathVariable Long orderId) {
        try {
            controllerLogger.info("Fetching detail for OrderID: {}", orderId);
            Order orderDetail = orderService.getOrderDetailsById(orderId);
            return ResponseEntity.ok(orderDetail);
        } catch (ResourceNotFoundException e) {
            controllerLogger.warn("Order not found when fetching detail for OrderID {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO(false, e.getMessage()));
        } catch (Exception e) {
            controllerLogger.error("Error fetching order detail for OrderID {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new MessageResponseDTO(false, "Đã xảy ra lỗi không mong muốn khi lấy chi tiết đơn hàng."));
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrderById(@PathVariable Long orderId) {
        try {
            controllerLogger.info("Attempting to cancel order with ID: {}", orderId);
            Order cancelledOrder = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(new MessageResponseDTO(true, "Hủy đơn hàng thành công!", cancelledOrder));
        } catch (ResourceNotFoundException e) {
            controllerLogger.warn("Order not found for cancellation, ID: {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO(false, e.getMessage()));
        } catch (IllegalStateException e) {
            controllerLogger.warn("Illegal state for cancelling order ID {}: {}", orderId, e.getMessage());
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(false, e.getMessage()));
        } catch (Exception e) {
            controllerLogger.error("Error cancelling order ID {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new MessageResponseDTO(false, "Đã xảy ra lỗi không mong muốn khi hủy đơn hàng."));
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatusByAdminOrLogic(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> payload
    ) {
        String newStatusString = payload.get("newStatus");
        String customMessage = payload.get("message"); // This can be optional

        if (newStatusString == null || newStatusString.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(false, "Trường 'newStatus' là bắt buộc."));
        }

        OrderStatus newStatusEnum;
        try {
            newStatusEnum = OrderStatus.valueOf(newStatusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            controllerLogger.warn("Invalid status value received: '{}' for order ID: {}", newStatusString, orderId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(false, "Giá trị trạng thái không hợp lệ: " + newStatusString));
        }

        try {
            controllerLogger.info("Attempting to update order ID {} to status {} with custom message: '{}'", orderId, newStatusEnum, customMessage);
            Order updatedOrder = orderService.updateOrderStatus(orderId, newStatusEnum, customMessage);
            return ResponseEntity.ok(new MessageResponseDTO(true, "Cập nhật trạng thái đơn hàng thành công!", updatedOrder));
        } catch (ResourceNotFoundException e) {
            controllerLogger.error("Order not found for status update: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO(false, e.getMessage()));
        } catch (IllegalStateException e) {
            controllerLogger.error("Illegal state for status update on order {}: {}", orderId, e.getMessage()); // Don't log full exception for IllegalState to avoid noise if it's a business rule
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(false, e.getMessage()));
        } catch (Exception e) {
            controllerLogger.error("Unexpected error updating status for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new MessageResponseDTO(false, "Lỗi không mong muốn khi cập nhật trạng thái đơn hàng."));
        }
    }
}