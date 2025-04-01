package vn.iostar.Project_Mobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.iostar.Project_Mobile.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
