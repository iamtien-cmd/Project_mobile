package vn.iostar.Project_Mobile.config; // Hoặc vn.iostar.Project_Mobile.listener

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async; // THÊM IMPORT NÀY
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.event.OrderStatusChangedEvent; // Import your event

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderWebhookNotifier {
    private static final Logger logger = LoggerFactory.getLogger(OrderWebhookNotifier.class);
    private final RestTemplate restTemplate;

    @Value("${app.webhook.order-status-update-url}")
    private String orderStatusWebhookUrl;

    public OrderWebhookNotifier(RestTemplate restTemplate) { // Inject existing RestTemplate bean
        this.restTemplate = restTemplate;
    }

    @Async // Thêm @Async để không block luồng sau khi commit
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChange(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        String humanReadableMessage = event.getHumanReadableMessage(); // SỬA Ở ĐÂY: Lấy từ event

        // Kiểm tra null cho message để đảm bảo an toàn
        if (humanReadableMessage == null) {
            logger.warn("Human readable message is null for Order ID: {}. Using a default message.", order.getOrderId());
            // Bạn có thể tạo một message mặc định ở đây nếu cần, hoặc dựa vào logic trong sendOrderStatusUpdateWebhookInternal
            // Ví dụ: humanReadableMessage = "Trạng thái đơn hàng đã được cập nhật.";
            // Hoặc để sendOrderStatusUpdateWebhookInternal tự xử lý nếu nó có logic mặc định
        }

        logger.info("Transaction committed for Order ID: {}. Preparing to send webhook for status: {}. Message: {}",
                    order.getOrderId(), order.getStatus(), humanReadableMessage);
        sendOrderStatusUpdateWebhookInternal(order, humanReadableMessage); // Truyền message đã lấy được
    }

    // Phương thức sendOrderStatusUpdateWebhookInternal giữ nguyên như bạn đã viết
    private void sendOrderStatusUpdateWebhookInternal(Order order, String humanReadableMessage) {
        if (orderStatusWebhookUrl == null || orderStatusWebhookUrl.trim().isEmpty()) {
            logger.warn("Webhook URL for order status update (app.webhook.order-status-update-url) is not configured. Skipping webhook call for order ID: {}", order.getOrderId());
            return;
        }
        // Kiểm tra user và email trước khi cố gắng truy cập
        if (order.getUser() == null || order.getUser().getEmail() == null || order.getUser().getEmail().trim().isEmpty()) {
            logger.warn("Customer or customer email is missing for Order ID: {}. Skipping webhook call.", order.getOrderId());
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("orderId", order.getOrderId());
            payload.put("newStatus", order.getStatus() != null ? order.getStatus().name() : "UNKNOWN");
            payload.put("customerEmail", order.getUser().getEmail());
            payload.put("customerName", order.getUser().getFullName() != null ? order.getUser().getFullName() : "Quý khách");
            payload.put("totalPrice", order.getTotalPrice());
            payload.put("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : "");
            // Đảm bảo humanReadableMessage không null khi đưa vào payload
            // Nếu nó có thể null từ event, bạn có thể muốn một giá trị mặc định ở đây
            payload.put("statusMessage", humanReadableMessage != null ? humanReadableMessage : "Trạng thái đơn hàng đã được cập nhật.");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            logger.info("Sending order status update webhook for Order ID: {} (New Status: {}) to URL: {}. Payload: {}",
                        order.getOrderId(), order.getStatus(), orderStatusWebhookUrl, payload);
            restTemplate.postForObject(orderStatusWebhookUrl, entity, String.class);
            logger.info("Successfully sent order status update webhook for Order ID: {}", order.getOrderId());
        } catch (HttpClientErrorException e) {
            logger.error("HttpClientErrorException while sending webhook for Order ID: {}. Status: {}, Response Body: {}",
                         order.getOrderId(), e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            logger.error("RestClientException while sending webhook for Order ID: {}: {}",
                         order.getOrderId(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while preparing or sending webhook for Order ID: {}: {}",
                         order.getOrderId(), e.getMessage(), e);
        }
    }
}