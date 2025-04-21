package vn.iostar.Project_Mobile.controller.Product;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.iostar.Project_Mobile.DTO.ProductSearchRequest;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.repository.ProductRepository;
import vn.iostar.Project_Mobile.service.IProductService;
import vn.iostar.Project_Mobile.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

	@Autowired
    private IProductService productService;
    private final ProductRepository productRepository;


    @GetMapping("/getListProducts")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    //Lấy thông tin 1 sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sản phẩm không tồn tại");
        }
        
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
    
    //Tìm kiếm sản phẩm với từ khóa( lọc với giá min, max)
    @GetMapping("/search")
    public List<Product> searchProducts(ProductSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getOffset() / request.getLimit(), request.getLimit());

        Specification<Product> spec = ProductSpecification.withFilters(request);
        return productRepository.findAll(spec, pageable).getContent();
    }

}
