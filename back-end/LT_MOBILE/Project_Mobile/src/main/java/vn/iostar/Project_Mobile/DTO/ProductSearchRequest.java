package vn.iostar.Project_Mobile.DTO;

import lombok.Data;

@Data
public class ProductSearchRequest {
    private String keyword;
    private Long minPrice;
    private Long maxPrice;

    private int offset = 0; // vị trí bắt đầu
    private int limit = 20;
}
