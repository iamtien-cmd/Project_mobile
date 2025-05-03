package vn.iostar.Project_Mobile.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import vn.iostar.Project_Mobile.entity.Cart;
import vn.iostar.Project_Mobile.entity.CartItem;
import vn.iostar.Project_Mobile.repository.CartItemRepository;
import vn.iostar.Project_Mobile.repository.CartRepository;
import vn.iostar.Project_Mobile.repository.IUserRepository;
import vn.iostar.Project_Mobile.repository.ProductRepository;

public interface ICartService {

    Cart getOrCreateCart(Long userId);

    Cart addToCart(Long userId, Long productId, int quantity);

    Cart updateCartItem(Long userId, Long productId, int newQuantity);

    void removeCartItem(Long userId, Long productId);

    List<CartItem> getCartItems(Long userId);

    public static final CartRepository cartRepo = null;
    public static final CartItemRepository itemRepo = null;
    public static final ProductRepository productRepo = null;
    public static final IUserRepository userRepo = null;

}

