package vn.iostar.Project_Mobile.service.impl;

import org.springframework.stereotype.Service;

import vn.iostar.Project_Mobile.service.IChatBotService;

@Service
public class ChatBotServiceImpl implements IChatBotService {

    public String getReply(String message) {
        message = message.toLowerCase();

        if (message.contains("sản phẩm") || message.contains("có gì bán")) {
            return "MANJHA có các sản phẩm handmade như vòng tay, dreamcatcher, móc khóa, túi vải thêu tay, tranh đan sợi... Bạn đang quan tâm đến loại nào ạ?";
        } else if (message.contains("giá") || message.contains("bao nhiêu")) {
            return "Giá sản phẩm bên MANJHA dao động từ 50.000đ đến 300.000đ tùy mẫu. Bạn muốn xem mẫu nào để mình báo giá cụ thể nhé!";
        } else if (message.contains("đặt hàng") || message.contains("mua")) {
            return "Bạn có thể đặt hàng bằng cách thêm sản phẩm vào giỏ, sau đó điền thông tin nhận hàng. Hoặc nhắn tin trực tiếp qua fanpage MANJHA để đặt nhanh!";
        } else if (message.contains("giao hàng") || message.contains("ship")) {
            return "MANJHA giao hàng toàn quốc trong vòng 3-5 ngày làm việc. Phí ship từ 25.000đ tùy địa chỉ.";
        } else if (message.contains("đổi trả") || message.contains("hoàn hàng")) {
            return "Bạn có thể đổi trả trong 3 ngày nếu sản phẩm lỗi do shop. Vui lòng giữ nguyên tem mác và liên hệ fanpage để được hỗ trợ.";
        } else if (message.contains("giới thiệu") || message.contains("manjha là gì")) {
            return "MANJHA là thương hiệu handmade được tạo nên bởi sự đam mê với nghệ thuật thủ công. Mỗi sản phẩm đều được làm tay tỉ mỉ và mang theo thông điệp yêu thương 💕";
        } else if (message.contains("bảo quản") || message.contains("giữ lâu")) {
            return "Để giữ sản phẩm handmade bền đẹp, bạn nên tránh tiếp xúc trực tiếp với nước, tránh ánh nắng gay gắt và không giặt máy.";
        } else if (message.contains("ưu đãi") || message.contains("khuyến mãi")) {
            return "Hiện tại MANJHA đang có khuyến mãi 'Mua 2 tặng 1' với vòng tay, và freeship cho đơn từ 300.000đ. Bạn đừng bỏ lỡ nhé!";
        } else if (message.contains("liên hệ") || message.contains("kết nối")) {
            return "Bạn có thể liên hệ MANJHA qua fanpage Facebook: fb.com/manjha.handmade hoặc Instagram: @manjha.handmade ✨";
        } else {
            return "Xin lỗi, mình chưa hiểu rõ ý bạn. Bạn có thể hỏi theo dạng: 'Sản phẩm hiện có', 'Cách đặt hàng', 'Chính sách đổi trả', 'Thông tin về MANJHA' nhé!";
        }
    }
}