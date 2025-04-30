package vn.iostar.Project_Mobile.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.exception.ResourceNotFoundException;

@Service
public interface IOrderService {
	 public List<Order> getOrdersByUserId(Long userId);
	  Order cancelOrder(Long orderId) throws ResourceNotFoundException, IllegalStateException;

}
