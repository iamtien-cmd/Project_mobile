package vn.iostar.Project_Mobile.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor // Constructor không tham số
@AllArgsConstructor // Constructor với tất cả các tham số
public class MessageResponseDTO {

    private boolean success;
    private String message;
    private Object data; // Trường này có thể chứa bất kỳ đối tượng dữ liệu nào bạn muốn trả về

    /**
     * Constructor tiện lợi khi không cần trả về 'data'.
     * @param success Trạng thái thành công của hoạt động.
     * @param message Thông điệp mô tả kết quả.
     */
    public MessageResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null; // Hoặc bạn có thể bỏ qua việc gán nếu data là tùy chọn
    }
}
