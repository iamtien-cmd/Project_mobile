package vn.iostar.Project_Mobile.service.impl;

import java.util.List;
import java.util.Optional; // Import Optional

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.entity.OrderStatus; // Import Enum OrderStatus
import vn.iostar.Project_Mobile.repository.IOrderRepository; // Import Repository của bạn
import vn.iostar.Project_Mobile.service.IOrderService;
// Import các Exception tùy chỉnh nếu bạn tạo
import vn.iostar.Project_Mobile.exception.ResourceNotFoundException;
@Service
public class OrderServiceImpl implements IOrderService{

    @Autowired
    private IOrderRepository orderRepository;

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserUserId(userId);
    }
    @Override
    @Transactional // Đảm bảo thao tác cập nhật là một transaction
    public Order cancelOrder(Long orderId) throws ResourceNotFoundException, IllegalStateException {
        // 1. Tìm đơn hàng bằng ID, nếu không thấy thì ném ResourceNotFoundException
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // 2. Kiểm tra trạng thái hiện tại của đơn hàng
        // Chỉ cho phép hủy khi trạng thái là WAITING
        if (order.getStatus() != OrderStatus.WAITING) {
            // Ném lỗi nếu trạng thái không cho phép hủy
            throw new IllegalStateException("Không thể hủy đơn hàng này. Trạng thái hiện tại: " + order.getStatus());
            // Hoặc dùng Exception tùy chỉnh:
            // throw new OrderCannotBeCancelledException("Không thể hủy đơn hàng này. Trạng thái hiện tại: " + order.getStatus());
        }

        // 3. Nếu hợp lệ, cập nhật trạng thái thành ERROR
        order.setStatus(OrderStatus.ERROR);

        // 4. Lưu lại đơn hàng đã cập nhật vào database
        Order savedOrder = orderRepository.save(order);

        // 5. Trả về đơn hàng đã được cập nhật
        return savedOrder;
    }
}
