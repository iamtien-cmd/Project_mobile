package vn.iostar.Project_Mobile.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iostar.Project_Mobile.DTO.CreateOrderRequest;
import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.exception.ResourceNotFoundException;
import vn.iostar.Project_Mobile.repository.AddressRepository;
import vn.iostar.Project_Mobile.repository.CartItemRepository;
import vn.iostar.Project_Mobile.repository.CartRepository;
import vn.iostar.Project_Mobile.repository.OrderLineRepository;
import vn.iostar.Project_Mobile.repository.OrderRepository;
import vn.iostar.Project_Mobile.repository.ProductRepository;


@Service
public interface IOrderService {

	Order createOrder(User currentUser, CreateOrderRequest request);

	public static final OrderRepository orderRepository = null;
	public static final OrderLineRepository orderLineRepository = null;
	public static final CartItemRepository cartItemRepository = null;
	public static final ProductRepository productRepository = null;
	public static final AddressRepository addressRepository = null;
	public static final CartRepository cartRepository = null;
	Order getOrderDetailsById(Long orderId, User user);
	public List<Order> getOrdersByUserId(Long userId);
	Order cancelOrder(Long orderId) throws ResourceNotFoundException, IllegalStateException;



}
