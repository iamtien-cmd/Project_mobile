package vn.iostar.Project_Mobile.service;

import java.util.List;

import vn.iostar.Project_Mobile.entity.Product;

public interface IProductService {
	public List<Product> getAllProducts();
	public Product getProductById(Long id);
	 public Product saveProduct(Product product) ;
	  public void deleteProduct(Long id) ;
}
