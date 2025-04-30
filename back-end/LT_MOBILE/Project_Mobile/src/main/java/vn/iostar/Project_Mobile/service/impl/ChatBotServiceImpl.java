package vn.iostar.Project_Mobile.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.iostar.Project_Mobile.service.IChatBotService;

import java.util.Arrays;
import java.util.List;

@Service
public class ChatBotServiceImpl implements IChatBotService {

    private static final Logger log = LoggerFactory.getLogger(ChatBotServiceImpl.class);

    // Danh sách các từ khóa sản phẩm cụ thể cần nhận diện
    private static final List<String> SPECIFIC_PRODUCT_KEYWORDS = Arrays.asList(
            "vòng tay", "dreamcatcher", "móc khóa", "túi vải", "thêu tay", "tranh đan", "sợi"
            // Thêm các từ khóa sản phẩm cụ thể khác vào đây nếu cần
    );

    @Override
    public String getReply(String message) {
        if (message == null || message.isBlank()) {
            return "Vui lòng cung cấp nội dung tin nhắn."; // Xử lý đầu vào trống
        }

        String lowerCaseMessage = message.toLowerCase().trim(); // Chuyển lowercase và trim khoảng trắng
        log.info("Received message (processed): {}", lowerCaseMessage); // Log tin nhắn đã xử lý

        String reply;

        // --- BƯỚC 1: KIỂM TRA TỪ KHÓA SẢN PHẨM CỤ THỂ ---
        for (String productKeyword : SPECIFIC_PRODUCT_KEYWORDS) {
            if (lowerCaseMessage.contains(productKeyword)) {
                // Nếu tìm thấy từ khóa sản phẩm cụ thể, hướng dẫn tìm kiếm trên trang chủ
                reply = "Với sản phẩm bạn quan tâm ('" + productKeyword + "'), bạn vui lòng sử dụng chức năng tìm kiếm trên trang chủ của MANJHA để xem các mẫu hiện có và biết thêm chi tiết nhé!";
                log.info("Replying with specific product search direction: {}", reply);
                return reply; // Trả về ngay lập tức
            }
        }

        // --- BƯỚC 2: KIỂM TRA CÁC TỪ KHÓA CHUNG (NẾU KHÔNG TÌM THẤY SP CỤ THỂ) ---
        if (lowerCaseMessage.contains("sản phẩm") || lowerCaseMessage.contains("có gì bán")) {
            reply = "MANJHA có các sản phẩm handmade như vòng tay, dreamcatcher, móc khóa, túi vải thêu tay, tranh đan sợi... Bạn đang quan tâm đến loại nào ạ? (Bạn cũng có thể tìm kiếm trực tiếp trên trang chủ)";
        } else if (lowerCaseMessage.contains("giá") || lowerCaseMessage.contains("bao nhiêu")) {
            reply = "Giá sản phẩm bên MANJHA dao động từ 20.000đ đến 1.000.000đ tùy mẫu. Bạn muốn xem mẫu nào để mình báo giá cụ thể nhé! (Bạn có thể tìm sản phẩm trên trang chủ để xem giá)";
        } else if (lowerCaseMessage.contains("đặt hàng") || lowerCaseMessage.contains("mua")) {
            reply = "Bạn có thể đặt hàng bằng cách thêm sản phẩm vào giỏ, sau đó điền thông tin nhận hàng. Hoặc nhắn tin trực tiếp qua fanpage MANJHA để đặt nhanh!";
        } else if (lowerCaseMessage.contains("giao hàng") || lowerCaseMessage.contains("ship")) {
            reply = "MANJHA giao hàng toàn quốc trong vòng 3-5 ngày làm việc. Phí ship từ 25.000đ tùy địa chỉ.";
        } else if (lowerCaseMessage.contains("đổi trả") || lowerCaseMessage.contains("hoàn hàng")) {
            reply = "Bạn có thể đổi trả trong 3 ngày nếu sản phẩm lỗi do shop. Vui lòng giữ nguyên tem mác và liên hệ fanpage để được hỗ trợ.";
        } else if (lowerCaseMessage.contains("giới thiệu") || lowerCaseMessage.contains("manjha là gì")) {
            reply = "MANJHA là thương hiệu handmade được tạo nên bởi sự đam mê với nghệ thuật thủ công. Mỗi sản phẩm đều được làm tay tỉ mỉ và mang theo thông điệp yêu thương 💕";
        } else if (lowerCaseMessage.contains("bảo quản") || lowerCaseMessage.contains("giữ lâu")) {
            reply = "Để giữ sản phẩm handmade bền đẹp, bạn nên tránh tiếp xúc trực tiếp với nước, tránh ánh nắng gay gắt và không giặt máy.";
        } else if (lowerCaseMessage.contains("ưu đãi") || lowerCaseMessage.contains("khuyến mãi")) {
            reply = "Hiện tại MANJHA đang có khuyến mãi 'Mua 2 tặng 1' với vòng tay, và freeship cho đơn từ 500.000đ. Bạn đừng bỏ lỡ nhé!";
        } else if (lowerCaseMessage.contains("liên hệ") || lowerCaseMessage.contains("kết nối")) {
            reply = "Bạn có thể liên hệ MANJHA qua fanpage Facebook: fb.com/manjha.handmade hoặc Instagram: @manjha.handmade ✨";
        } else {
            // --- BƯỚC 3: PHẢN HỒI MẶC ĐỊNH ---
            reply = "Xin lỗi, mình chưa hiểu rõ ý bạn. Bạn có thể hỏi theo dạng: 'Sản phẩm hiện có', 'Cách đặt hàng', 'Chính sách đổi trả', 'Thông tin về MANJHA' hoặc tìm kiếm sản phẩm cụ thể trên trang chủ nhé!";
        }

        log.info("Replying with: {}", reply); // Log câu trả lời
        return reply;
    }
}