package vn.iostar.Project_Mobile.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iostar.Project_Mobile.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCart_CartIdAndProduct_ProductId(Long cartId, Long productId);
}