package vn.iostar.Project_Mobile.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Quan trọng
import vn.iostar.Project_Mobile.DTO.CreateOrderRequest;
import vn.iostar.Project_Mobile.entity.*;
import vn.iostar.Project_Mobile.repository.*;
import vn.iostar.Project_Mobile.service.IOrderService;
import vn.iostar.Project_Mobile.util.OrderStatus; // Import enum OrderStatus

import java.sql.Date; // Hoặc java.time.LocalDate nếu dùng kiểu mới
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit; // Ví dụ tính ngày giao hàng

@Service
public class OrderServiceImpl implements IOrderService {
	@Autowired
	private AddressRepository addressRepository;
	
	@Autowired
    private CartItemRepository cartItemRepository;
	
	@Autowired
    private OrderRepository orderRepository;
	
	@Autowired
    private OrderLineRepository orderLineRepository;
	
	@Autowired
    private ProductRepository productRepository;

	// Sử dụng @Transactional để đảm bảo tất cả thao tác thành công hoặc rollback
	@Override
	@Transactional
	public Order createOrder(User currentUser, CreateOrderRequest request) {

		// 1. Validate input cơ bản
		if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
			throw new IllegalArgumentException("Please select items to order.");
		}
		if (request.getAddressId() == null) {
			throw new IllegalArgumentException("Please select a shipping address.");
		}
		if (request.getPaymentMethod() == null) {
			throw new IllegalArgumentException("Please select a payment method.");
		}

		// 2. Lấy Cart của User
		Cart userCart = currentUser.getCart(); // Giả sử User entity đã load Cart
		if (userCart == null) {
			throw new IllegalStateException("User cart not found.");
		}

		// 3. Lấy Address và kiểm tra quyền sở hữu
		Address shippingAddress = addressRepository
				.findByAddressIdAndUser_UserId(request.getAddressId(), currentUser.getUserId()).orElseThrow(
						() -> new NoSuchElementException("Shipping address not found or does not belong to the user."));

		// 4. Lấy các CartItem được chọn và kiểm tra quyền sở hữu + tồn kho
		List<CartItem> selectedCartItems = cartItemRepository.findByCartItemIdInAndCart_CartId(request.getCartItemIds(),
				userCart.getCartId());

		// Kiểm tra xem có lấy đủ số lượng item không (phòng trường hợp ID không tồn tại
		// hoặc không thuộc cart)
		if (selectedCartItems.size() != request.getCartItemIds().size()) {
			throw new NoSuchElementException(
					"One or more selected cart items are invalid or do not belong to your cart.");
		}

		double totalPrice = 0;
		List<OrderLine> tempOrderLines = new ArrayList<>(); // List tạm để chứa các OrderLine sẽ tạo

		for (CartItem item : selectedCartItems) {
			Product product = item.getProduct();
			if (product == null) {
				throw new IllegalStateException("Product data missing for cart item: " + item.getCartItemId());
			}

			// --- KIỂM TRA TỒN KHO (QUAN TRỌNG) ---
			// Giả sử Product có trường 'stock'
			// if (product.getStock() < item.getQuantity()) {
			// throw new IllegalStateException("Product '" + product.getName() + "' is out
			// of stock or insufficient quantity.");
			// }
			// --- HẾT KIỂM TRA TỒN KHO ---

			// Tính tổng giá
			totalPrice += product.getPrice() * item.getQuantity();
			OrderLine line = new OrderLine();
			line.setProduct(product);
			line.setQuantity(item.getQuantity());
			tempOrderLines.add(line);
		}

		// 5. Tạo Order
		Order newOrder = new Order();
		newOrder.setUser(currentUser);
		newOrder.setTotalPrice(totalPrice);
		newOrder.setOrderDate(new Date(System.currentTimeMillis())); // Ngày hiện tại
		long predictMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5);
		newOrder.setPredictReceiveDate(new Date(predictMillis));
		newOrder.setPaymentMethod(request.getPaymentMethod());
		newOrder.setStatus(OrderStatus.Waiting);


		

		Order savedOrder = orderRepository.save(newOrder);

		List<OrderLine> finalOrderLines = new ArrayList<>();
		for (OrderLine line : tempOrderLines) {
			line.setOrder(savedOrder); // Liên kết với Order đã có ID
			finalOrderLines.add(orderLineRepository.save(line));

			Product productToUpdate = line.getProduct();
			int newStock = productToUpdate.getQuantity() - line.getQuantity();
			productToUpdate.setQuantity(newStock);
			productRepository.save(productToUpdate); // Cần ProductRepository
		}
		savedOrder.setOrderLines(finalOrderLines);

		cartItemRepository.deleteAll(selectedCartItems); // Xóa các CartItem đã xử lý

		return savedOrder;
	}
	@Override
	public Order getOrderDetailsById(Long orderId, User user) {
	    Order order = orderRepository.findById(orderId)
	            .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng."));
	    return order;
	}
}
