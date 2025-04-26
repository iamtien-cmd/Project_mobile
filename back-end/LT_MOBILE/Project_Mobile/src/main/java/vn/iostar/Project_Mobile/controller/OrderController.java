package vn.iostar.Project_Mobile.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.service.IOrderService;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    // API: Xem danh sách trạng thái đơn hàng của user
    @GetMapping("/status/{userId}")
    public List<Order> getOrderStatusesByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }
}
