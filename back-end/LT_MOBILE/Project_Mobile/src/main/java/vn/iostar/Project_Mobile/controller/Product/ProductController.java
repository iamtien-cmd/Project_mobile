package vn.iostar.Project_Mobile.controller.Product;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.iostar.Project_Mobile.DTO.CategoryInfo;
import vn.iostar.Project_Mobile.DTO.CommentInfo;
import vn.iostar.Project_Mobile.DTO.ProductDetailResponse;
import vn.iostar.Project_Mobile.DTO.ProductSearchRequest;
import vn.iostar.Project_Mobile.DTO.ProductSummaryResponse;
import vn.iostar.Project_Mobile.DTO.UserInfo;
import vn.iostar.Project_Mobile.entity.Category;
import vn.iostar.Project_Mobile.entity.Comment;
import vn.iostar.Project_Mobile.entity.ImagesProduct;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.repository.ProductRepository;
import vn.iostar.Project_Mobile.service.ICategoryService;
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
        ProductDetailResponse response = new ProductDetailResponse();

        response.setProductId(product.getProductId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setDescription(product.getDescription());
        response.setQuantity(product.getQuantity());

        // 3. Điền thông tin danh mục vào DTO (nếu có)
        if (product.getCategory() != null) {
            Category category = product.getCategory();
            response.setCategory(new CategoryInfo(
                    category.getCategoryId(),
                    category.getCategoryName()
            ));
        }

        // 4. Điền danh sách URL ảnh  vào DTO (nếu có)
        if (product.getImages() != null) {
            List<String> imageUrls = product.getImages().stream()
                    .map(ImagesProduct::getUrl) // Lấy URL từ mỗi đối tượng ImagesProduct
                    .collect(Collectors.toList()); // Thu thập thành danh sách
            response.setImageUrls(imageUrls);
        }

        // *** 5. Điền danh sách bình luận vào DTO (nếu có) ***
        if (product.getComments() != null) {
            List<CommentInfo> commentInfos = product.getComments().stream()
                    .map(comment -> {
                        CommentInfo commentInfo = new CommentInfo();
                        commentInfo.setCommentId(comment.getCommentId());
                        commentInfo.setContent(comment.getContent());
                        commentInfo.setImage(comment.getImage());
                        commentInfo.setRating(comment.getRating());
                        
                        if (comment.getUser() != null) {
                            User user = comment.getUser();
                            commentInfo.setUser(new UserInfo(
                                user.getUserId(),
                                user.getFullName(),
                                user.getAvatar()
                            ));
                        } else {
                             commentInfo.setUser(null); 
                        }
                        return commentInfo;
                    })
                    .collect(Collectors.toList());
             response.setComments(commentInfos);
        }

        // 6. Điền danh sách sản phẩm liên quan (cùng danh mục, nếu có danh mục)
        if (product.getCategory() != null) {
            Long categoryId = product.getCategory().getCategoryId();
            List<Product> allProductsInCategory = productService.getProductsByCategory(categoryId);

            List<ProductSummaryResponse> relatedProducts = allProductsInCategory.stream()
                    .filter(p -> p.getProductId() != id) 
                    .map(p -> new ProductSummaryResponse(
                            p.getProductId(),
                            p.getImage(),
                            p.getName(),
                            p.getPrice()
                    ))
                    .collect(Collectors.toList()); 
            response.setRelatedProducts(relatedProducts);
        }

        return ResponseEntity.ok(response);
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
   
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }


    @Autowired
    private ICategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

}
