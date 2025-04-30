package vn.iostar.Project_Mobile.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.repository.ProductRepository;
import vn.iostar.Project_Mobile.service.IProductService;
import vn.iostar.Project_Mobile.service.IUserService;

@Service
public class ProductService implements IProductService{
    
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
    	return productRepository.findById(id)
                .orElse(null);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_CategoryId(categoryId);
    }
}