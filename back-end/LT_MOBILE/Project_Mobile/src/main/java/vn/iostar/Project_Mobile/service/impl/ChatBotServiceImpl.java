package vn.iostar.Project_Mobile.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.iostar.Project_Mobile.service.IChatBotService; // Đảm bảo import đúng

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class ChatBotServiceImpl implements IChatBotService {

    private static final Logger log = LoggerFactory.getLogger(ChatBotServiceImpl.class);

    // Danh sách các từ khóa sản phẩm cụ thể cần nhận diện
    private static final List<String> SPECIFIC_PRODUCT_KEYWORDS = Arrays.asList(
            "vòng tay", "dreamcatcher", "móc khóa", "túi vải", "thêu tay", "tranh đan", "sợi",
            "len", "macrame", "hoa tai", "dây chuyền", "kẹp tóc", "đồ trang trí", "phụ kiện"
            // Thêm các từ khóa sản phẩm cụ thể khác vào đây nếu cần
    );

    private static final Random random = new Random();

    // Mảng các câu chào/cảm ơn đáp lại
    private static final String[] GREETING_RESPONSES = {
            "MANJHA chào bạn ạ!",
            "Chào bạn, MANJHA có thể giúp gì cho bạn?",
            "Dạ, MANJHA nghe ạ!"
    };

    private static final String[] THANKS_RESPONSES = {
            "MANJHA rất vui khi được hỗ trợ bạn! 😊",
            "Không có chi ạ! Cần hỗ trợ thêm bạn cứ nhắn MANJHA nhé.",
            "Rất vui vì bạn đã quan tâm đến MANJHA! 💕"
    };


    @Override
    public String getReply(String message) {
        if (message == null || message.isBlank()) {
            return "Vui lòng cung cấp nội dung tin nhắn.";
        }

        String lowerCaseMessage = message.toLowerCase().trim();
        log.info("Received message (processed): {}", lowerCaseMessage);

        String reply;

        // --- BƯỚC 0: CHÀO HỎI VÀ CẢM ƠN ---
        if (isGreeting(lowerCaseMessage)) {
            reply = GREETING_RESPONSES[random.nextInt(GREETING_RESPONSES.length)];
            log.info("Replying with greeting: {}", reply);
            return reply;
        }

        if (isThanks(lowerCaseMessage)) {
            reply = THANKS_RESPONSES[random.nextInt(THANKS_RESPONSES.length)];
            log.info("Replying with thanks response: {}", reply);
            return reply;
        }


        // --- BƯỚC 1: KIỂM TRA TỪ KHÓA SẢN PHẨM CỤ THỂ ---
        for (String productKeyword : SPECIFIC_PRODUCT_KEYWORDS) {
            if (lowerCaseMessage.contains(productKeyword)) {
                reply = "Với sản phẩm bạn quan tâm ('" + productKeyword + "'), bạn vui lòng sử dụng chức năng tìm kiếm trên trang chủ của MANJHA để xem các mẫu hiện có và biết thêm chi tiết (giá, chất liệu, kích thước) nhé!";
                log.info("Replying with specific product search direction: {}", reply);
                return reply;
            }
        }

        // --- BƯỚC 2: KIỂM TRA CÁC TỪ KHÓA CHUNG (NẾU KHÔNG TÌM THẤY SP CỤ THỂ) ---
        // Các câu hỏi thường gặp đã có
        if (lowerCaseMessage.contains("sản phẩm") || lowerCaseMessage.contains("có gì bán") || lowerCaseMessage.contains("mặt hàng")) {
            reply = "MANJHA có các sản phẩm handmade như vòng tay, dreamcatcher, móc khóa, túi vải thêu tay, tranh đan sợi, phụ kiện trang trí... Bạn đang quan tâm đến loại nào cụ thể ạ? (Bạn cũng có thể tìm kiếm trực tiếp trên trang chủ)";
        } else if (lowerCaseMessage.contains("giá") || lowerCaseMessage.contains("bao nhiêu tiền") || lowerCaseMessage.contains("giá cả")) {
            reply = "Giá sản phẩm bên MANJHA dao động từ 40.000đ đến 1.000.000đ tùy mẫu và độ phức tạp. Bạn muốn xem mẫu nào để mình báo giá cụ thể nhé! (Bạn có thể tìm sản phẩm trên trang chủ để xem giá)";
        } else if (lowerCaseMessage.contains("đặt hàng") || lowerCaseMessage.contains("mua hàng") || lowerCaseMessage.contains("đặt mua")) {
            reply = "Bạn có thể đặt hàng dễ dàng bằng cách thêm sản phẩm bạn thích vào giỏ hàng trên website, sau đó điền thông tin nhận hàng. Nếu cần hỗ trợ nhanh, bạn cũng có thể nhắn tin trực tiếp qua fanpage MANJHA nhé!";
        } else if (lowerCaseMessage.contains("giao hàng") || lowerCaseMessage.contains("ship") || lowerCaseMessage.contains("vận chuyển")) {
            reply = "MANJHA giao hàng toàn quốc trong vòng 3-5 ngày làm việc. Phí ship sẽ được tính dựa trên địa chỉ và số lượng sản phẩm trong đơn hàng của bạn, thường từ 15.000đ.";
        } else if (lowerCaseMessage.contains("đổi trả") || lowerCaseMessage.contains("hoàn hàng") || lowerCaseMessage.contains("bảo hành")) {
            reply = "MANJHA hỗ trợ đổi trả trong vòng 3 ngày nếu sản phẩm có lỗi từ nhà sản xuất. Bạn vui lòng giữ nguyên tem mác và liên hệ fanpage để được hướng dẫn chi tiết nhé.";
        } else if (lowerCaseMessage.contains("giới thiệu") || lowerCaseMessage.contains("manjha là gì") || lowerCaseMessage.contains("về manjha")) {
            reply = "MANJHA là thương hiệu handmade được tạo nên bởi sự đam mê với nghệ thuật thủ công. Mỗi sản phẩm đều được làm tay tỉ mỉ, chăm chút và mang theo thông điệp yêu thương. Tụi mình mong muốn mang đến những món đồ nhỏ xinh, ý nghĩa cho bạn. 💕";
        } else if (lowerCaseMessage.contains("bảo quản") || lowerCaseMessage.contains("giữ gìn") || lowerCaseMessage.contains("giặt")) {
            reply = "Để giữ sản phẩm handmade bền đẹp, bạn nên: \n1. Tránh tiếp xúc trực tiếp với nước và hóa chất. \n2. Tránh ánh nắng gay gắt trong thời gian dài. \n3. Với các sản phẩm vải, nên giặt tay nhẹ nhàng. \n4. Cất giữ nơi khô ráo, thoáng mát.";
        } else if (lowerCaseMessage.contains("ưu đãi") || lowerCaseMessage.contains("khuyến mãi") || lowerCaseMessage.contains("giảm giá") || lowerCaseMessage.contains("voucher")) {
            reply = "MANJHA thường xuyên có các chương trình ưu đãi hấp dẫn! Hiện tại, shop đang có khuyến mãi freeship cho đơn hàng từ 500.000đ. Bạn theo dõi fanpage và website để không bỏ lỡ nhé!";
        } else if (lowerCaseMessage.contains("liên hệ") || lowerCaseMessage.contains("kết nối") || lowerCaseMessage.contains("địa chỉ") || lowerCaseMessage.contains("fanpage")) {
            reply = "Bạn có thể liên hệ MANJHA qua các kênh sau:\n- Fanpage Facebook: fb.com/manjha.handmade\n- Instagram: @manjha.handmade\n- Email: manjha.handmade@email.com\nMANJHA chưa có cửa hàng offline ạ, tụi mình bán online là chính nhé. ✨";
        }
        // Kịch bản mới
        else if (lowerCaseMessage.contains("chất liệu") || lowerCaseMessage.contains("làm bằng gì") || lowerCaseMessage.contains("vật liệu")) {
            reply = "Các sản phẩm của MANJHA được làm từ nhiều chất liệu thân thiện và an toàn như sợi cotton, len, hạt gỗ, vải canvas, charm kim loại không gỉ... Mỗi sản phẩm sẽ có mô tả chi tiết về chất liệu trên trang web, bạn tham khảo thêm nhé!";
        } else if (lowerCaseMessage.contains("theo yêu cầu") || lowerCaseMessage.contains("custom") || lowerCaseMessage.contains("riêng") || lowerCaseMessage.contains("thiết kế riêng")) {
            reply = "Dạ, MANJHA có nhận làm một số sản phẩm theo yêu cầu đơn giản (ví dụ: thay đổi màu sắc, thêm charm...). Bạn vui lòng nhắn tin chi tiết yêu cầu qua fanpage để MANJHA xem xét và báo giá cụ thể nhé!";
        } else if (lowerCaseMessage.contains("ý nghĩa dreamcatcher")) {
            reply = "Dreamcatcher (bùa ngủ ngon) theo truyền thuyết của người da đỏ Ojibwa có ý nghĩa bắt giữ những giấc mơ đẹp và xua đi những cơn ác mộng, mang lại giấc ngủ bình yên. Nó cũng là một vật trang trí rất xinh xắn nữa đó bạn!";
        } else if (lowerCaseMessage.contains("gói quà") || lowerCaseMessage.contains("làm quà tặng") || lowerCaseMessage.contains("tặng quà")) {
            reply = "Sản phẩm handmade của MANJHA rất thích hợp làm quà tặng ý nghĩa đó ạ! Nếu bạn có nhu cầu gói quà, MANJHA có hỗ trợ gói quà xinh xắn với một khoản phụ phí nhỏ. Bạn ghi chú lại khi đặt hàng hoặc nhắn tin cho shop nhé.";
        } else if (lowerCaseMessage.contains("kích thước") || lowerCaseMessage.contains("size") || lowerCaseMessage.contains("to không") || lowerCaseMessage.contains("nhỏ không")) {
            reply = "Kích thước của mỗi sản phẩm sẽ được ghi rõ trong phần mô tả trên website. Với vòng tay, một số mẫu có thể điều chỉnh được kích thước. Bạn xem kỹ thông tin hoặc nhắn MANJHA nếu cần tư vấn thêm nhé!";
        } else if (lowerCaseMessage.contains("ai làm") || lowerCaseMessage.contains("nguồn gốc") || lowerCaseMessage.contains("tự làm")) {
            reply = "Tất cả sản phẩm của MANJHA đều do đội ngũ thợ thủ công khéo léo của shop tự tay thực hiện với tất cả tâm huyết. Tụi mình tự hào về điều đó! 😊";
        } else if (lowerCaseMessage.contains("sản phẩm lỗi") || lowerCaseMessage.contains("không giống hình") || lowerCaseMessage.contains("giao hàng chậm") || lowerCaseMessage.contains("chất lượng kém")) {
            reply = "MANJHA rất xin lỗi nếu bạn gặp trải nghiệm không tốt ạ. Bạn vui lòng nhắn tin chi tiết vấn đề bạn gặp phải (kèm hình ảnh nếu có) qua fanpage để MANJHA kiểm tra và hỗ trợ bạn nhanh nhất có thể nhé. Sự hài lòng của bạn rất quan trọng với tụi mình!";
        } else if (lowerCaseMessage.contains("bán sỉ") || lowerCaseMessage.contains("mua sỉ") || lowerCaseMessage.contains("hợp tác")) {
            reply = "MANJHA có chính sách ưu đãi cho khách hàng mua sỉ hoặc muốn hợp tác. Bạn vui lòng gửi thông tin chi tiết về nhu cầu và số lượng qua email manjha.handmade@email.com để bộ phận kinh doanh của MANJHA trao đổi cụ thể hơn nhé.";
        } else if (lowerCaseMessage.contains("shopee") || lowerCaseMessage.contains("lazada") || lowerCaseMessage.contains("tiki") || lowerCaseMessage.contains("sàn khác")) {
            reply = "Hiện tại MANJHA chủ yếu bán hàng qua website chính thức và fanpage Facebook. Bạn có thể theo dõi để cập nhật nếu MANJHA có mở thêm kênh bán hàng mới nhé!";
        } else if (lowerCaseMessage.contains("mấy giờ") || lowerCaseMessage.contains("online") || lowerCaseMessage.contains("trả lời tin nhắn")) {
            reply = "MANJHA cố gắng phản hồi tin nhắn của bạn nhanh nhất có thể trong giờ hành chính (9h - 18h, Thứ 2 - Thứ 7). Ngoài giờ, phản hồi có thể chậm hơn một chút, mong bạn thông cảm nhé!";
        } else if (lowerCaseMessage.contains("sản phẩm mới") || lowerCaseMessage.contains("mẫu mới") || lowerCaseMessage.contains("sắp có gì")) {
            reply = "MANJHA luôn ấp ủ những ý tưởng mới! Bạn hãy theo dõi fanpage và website của MANJHA để cập nhật những sản phẩm mới nhất nhé. Bật mí là sắp có bộ sưu tập rất xinh đó! 😉";
        } else if (lowerCaseMessage.contains("dạy làm") || lowerCaseMessage.contains("workshop") || lowerCaseMessage.contains("tự làm được không")) {
            reply = "Hiện tại MANJHA chưa tổ chức các lớp workshop dạy làm đồ handmade. Tuy nhiên, đây là một ý tưởng rất hay và MANJHA sẽ xem xét trong tương lai. Cảm ơn bạn đã quan tâm nhé!";
        } else if (isIrrelevantQuestion(lowerCaseMessage)) {
            reply = "MANJHA rất sẵn lòng hỗ trợ bạn các thông tin về sản phẩm và dịch vụ của shop. Với câu hỏi này, có lẽ MANJHA chưa giúp được bạn rồi. 😊";
        }
        else {
            // --- BƯỚC 3: PHẢN HỒI MẶC ĐỊNH ---
            reply = "Xin lỗi, MANJHA chưa hiểu rõ ý bạn. Bạn có thể hỏi cụ thể hơn về sản phẩm (ví dụ: 'vòng tay giá bao nhiêu', 'dreamcatcher có màu gì'), chính sách (ví dụ: 'cách đặt hàng', 'đổi trả thế nào') hoặc tìm kiếm sản phẩm bạn quan tâm trên trang chủ MANJHA nhé!";
        }

        log.info("Replying with: {}", reply);
        return reply;
    }

    // Hàm kiểm tra câu chào
    private boolean isGreeting(String message) {
        return message.equals("hi") || message.equals("hello") || message.equals("chào") || message.equals("chào shop") ||
               message.startsWith("xin chào") || message.equals("shop ơi");
    }

    // Hàm kiểm tra câu cảm ơn
    private boolean isThanks(String message) {
        return message.contains("cảm ơn") || message.contains("thank you") || message.contains("thanks") ||
               message.equals("ok") || message.equals("okie") || message.equals("oke") || message.equals("okela") ||
               message.equals("tuyệt vời") || message.equals("tuyệt");
    }

    // Hàm kiểm tra câu hỏi không liên quan (ví dụ đơn giản)
    private boolean isIrrelevantQuestion(String message) {
        List<String> irrelevantKeywords = Arrays.asList("ăn gì", "khỏe không", "thời tiết", "ngày mấy", "mấy giờ rồi");
        for (String keyword : irrelevantKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}