package vn.iostar.Project_Mobile.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.iostar.Project_Mobile.entity.Comment;

import java.util.List;

// DTO này dùng để trả về thông tin chi tiết sản phẩm
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
	 private long productId;
	    private double price;
	    private String name;
	    private String description;
	    private int quantity;

	    private CategoryInfo category;

	    private List<CommentInfo> comments;

	    private List<String> imageUrls; // List các URL từ entity ImagesProduct

	    private List<ProductSummaryResponse> relatedProducts;
}
