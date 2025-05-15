package vn.iostar.Project_Mobile.event;
import org.springframework.context.ApplicationEvent;
import vn.iostar.Project_Mobile.entity.Order;

public class OrderStatusChangedEvent extends ApplicationEvent {
    private final Order order;
    private final String humanReadableMessage; // Thông điệp mô tả trạng thái

    public OrderStatusChangedEvent(Object source, Order order) {
        super(source);
        this.order = order;
        this.humanReadableMessage = generateHumanReadableMessage(order);
    }

    // Constructor nếu bạn muốn truyền message tùy chỉnh từ service
    public OrderStatusChangedEvent(Object source, Order order, String customMessage) {
       super(source);
       this.order = order;
       this.humanReadableMessage = customMessage;
    }


    public Order getOrder() {
        return order;
    }

    public String getHumanReadableMessage() {
        return humanReadableMessage;
    }

    // Helper để tạo thông điệp dựa trên trạng thái
    private String generateHumanReadableMessage(Order order) {
        if (order == null || order.getStatus() == null) return "Trạng thái đơn hàng đã được cập nhật.";
        switch (order.getStatus()) {
            case PENDING: return "Đơn hàng của bạn #" + order.getOrderId() + " đang chờ thanh toán.";
            case WAITING: return "Đơn hàng của bạn #" + order.getOrderId() + " đã được xác nhận và đang chờ xử lý.";
            case SHIPPING: return "Đơn hàng của bạn #" + order.getOrderId() + "  đang được giao.";
            case RECEIVED: return "Đơn hàng của bạn #" + order.getOrderId() + " được giao thành công.";
            case REVIEWED: return "Đơn hàng của bạn #" + order.getOrderId() + " đánh giá thành công.";
            case CANCELLED: return "Đơn hàng của bạn #" + order.getOrderId() + " đã bị hủy.";
            case ERROR: return "Đơn hàng của bạn #" + order.getOrderId() + " đã gặp sự cố trong quá trình xử lý.";
            default: return "Trạng thái đơn hàng của bạn là: " + order.getStatus().name();
        }
    }
}