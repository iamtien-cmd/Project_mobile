package vn.iostar.Project_Mobile.service;

import java.util.List;
import java.util.Optional;

import vn.iostar.Project_Mobile.entity.Product;

public interface IProductService {
	 Optional<Product> findById(Long id);
	public List<Product> getAllProducts();
	public Product getProductById(Long id);
	 public Product saveProduct(Product product) ;
	  public void deleteProduct(Long id) ;
	  List<Product> getProductsByCategory(Long categoryId);
}
