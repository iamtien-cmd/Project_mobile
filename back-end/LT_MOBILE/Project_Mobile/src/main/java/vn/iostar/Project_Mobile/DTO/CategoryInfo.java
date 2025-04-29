package vn.iostar.Project_Mobile.DTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO đơn giản cho thông tin danh mục
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryInfo {
    private long categoryId;
    @JsonProperty("name") // Giống với cách bạn định nghĩa trong Category entity
    private String categoryName;
}
