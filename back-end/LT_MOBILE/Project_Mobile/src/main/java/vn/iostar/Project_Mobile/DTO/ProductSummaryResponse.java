package vn.iostar.Project_Mobile.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {
	private long productId;
    private String image; // URL ảnh chính
    private String name;
    private double price;
}
