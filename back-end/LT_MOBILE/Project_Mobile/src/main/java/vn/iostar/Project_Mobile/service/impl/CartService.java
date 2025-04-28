package vn.iostar.Project_Mobile.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vn.iostar.Project_Mobile.entity.Cart;
import vn.iostar.Project_Mobile.entity.CartItem;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.entity.User;
import vn.iostar.Project_Mobile.repository.*;
import vn.iostar.Project_Mobile.service.ICartService;

@Service
public class CartService implements ICartService{
	@Autowired
    private CartRepository cartRepo;

    @Autowired
    private CartItemRepository itemRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private IUserRepository userRepo;
	@Override
	public Cart getOrCreateCart(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepo.findByUser_UserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepo.save(cart);
        }
        return cart;
    }

    @Override
	public Cart addToCart(Long userId, Long productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // 🔒 Kiểm tra tồn kho
        int stock = product.getQuantity();
        Optional<CartItem> optional = itemRepo.findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId);
        int existingQty = optional.map(CartItem::getQuantity).orElse(0);

        if (existingQty + quantity > stock) {
            throw new RuntimeException("Số lượng vượt quá số lượng sản phẩm trong kho! Còn lại: " + (stock - existingQty));
        }

        CartItem item;
        if (optional.isPresent()) {
            item = optional.get();
            item.setQuantity(existingQty + quantity);
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
        }

        itemRepo.save(item);
        return cartRepo.findById(cart.getCartId()).orElse(cart);
    }


    @Override
	public Cart updateCartItem(Long userId, Long productId, int newQuantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = itemRepo.findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setQuantity(newQuantity);
        itemRepo.save(item);
        return cart;
    }

    @Override
	public void removeCartItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = itemRepo.findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        itemRepo.delete(item);
    }

    @Override
	public List<CartItem> getCartItems(Long userId) {
        Cart cart = cartRepo.findByUser_UserId(userId);
        return cart != null ? cart.getCartItems() : new ArrayList<>();
    }

}
