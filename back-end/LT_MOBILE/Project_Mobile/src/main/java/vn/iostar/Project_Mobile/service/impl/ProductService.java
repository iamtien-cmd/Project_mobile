package vn.iostar.Project_Mobile.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.repository.ProductRepository;
import vn.iostar.Project_Mobile.service.IProductService;
// import vn.iostar.Project_Mobile.service.IUserService; // IUserService không được sử dụng trực tiếp trong class này

@Service
public class ProductService implements IProductService { // Đảm bảo implements đúng interface IProductService

    @Autowired
    private ProductRepository productRepository;

    @Override // Nếu IProductService có khai báo phương thức này
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Phương thức này có thể không cần nếu bạn đã có findById trả về Optional
    // Nếu giữ lại, đảm bảo nó là một phần của IProductService nếu bạn override
    // @Override // Chỉ thêm @Override nếu phương thức này được khai báo trong IProductService
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElse(null); // Trả về null nếu không tìm thấy (cân nhắc dùng Optional ở đây)
    }

    @Override // Nếu IProductService có khai báo phương thức này
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override // Nếu IProductService có khai báo phương thức này
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override // Nếu IProductService có khai báo phương thức này
    public List<Product> getProductsByCategory(Long categoryId) {
        // Đảm bảo rằng ProductRepository có phương thức findByCategory_CategoryId
        // Hoặc tên phương thức đúng theo quy ước của Spring Data JPA
        return productRepository.findByCategory_CategoryId(categoryId);
    }

    @Override
    public Optional<Product> findById(Long id) {
        // SỬA Ở ĐÂY: Gọi phương thức findById của repository
        if (id == null) { // Thêm kiểm tra null cho id
            return Optional.empty();
        }
        return productRepository.findById(id);
    }
}