package vn.iostar.Project_Mobile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.repository.IOrderRepository;
import vn.iostar.Project_Mobile.service.IEmailService;
import vn.iostar.Project_Mobile.service.IOrderService;

@Service
public class OrderServiceImpl implements IOrderService{

    @Autowired
    private IOrderRepository orderRepository;

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserUserId(userId);
    }
}
