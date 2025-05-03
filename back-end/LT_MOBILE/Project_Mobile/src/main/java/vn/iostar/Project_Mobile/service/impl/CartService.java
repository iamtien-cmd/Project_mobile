package vn.iostar.Project_Mobile.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private IUserRepository userRepo; // Keep IUserRepository

    private User getUserById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    @Override
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        User user = getUserById(userId);
        Cart cart = cartRepo.findByUser_UserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepo.save(cart);
        }
        return cart;
    }

    @Override
    @Transactional
    public Cart addToCart(Long userId, Long productId, int quantity) {
        Cart cart = getOrCreateCart(userId); // userId is now validated from token
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        // 🔒 Stock check logic remains the same
        int stock = product.getQuantity();
        Optional<CartItem> optional = itemRepo.findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId);
        int existingQty = optional.map(CartItem::getQuantity).orElse(0);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        if (existingQty + quantity > stock) {
            throw new RuntimeException("Số lượng yêu cầu (" + (existingQty + quantity) + ") vượt quá số lượng sản phẩm trong kho (" + stock + "). Bạn đã có " + existingQty + " trong giỏ.");
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
        return cartRepo.findById(cart.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found after adding item, ID: " + cart.getCartId())); // Should not happen normally
    }


    @Override
    @Transactional
    public Cart updateCartItem(Long userId, Long productId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive. To remove, use the remove endpoint.");
        }

        Cart cart = getOrCreateCart(userId); // userId validated from token
        CartItem item = itemRepo.findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new RuntimeException("Item with Product ID " + productId + " not found in cart for user ID " + userId));

        Product product = item.getProduct();
        if (newQuantity > product.getQuantity()) {
            throw new RuntimeException("Số lượng yêu cầu (" + newQuantity + ") vượt quá số lượng sản phẩm trong kho (" + product.getQuantity() + ").");
        }

        item.setQuantity(newQuantity);
        itemRepo.save(item);
        return cartRepo.findById(cart.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found after updating item, ID: " + cart.getCartId()));
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId); // userId validated from token
        CartItem item = itemRepo.findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new RuntimeException("Item with Product ID " + productId + " not found in cart for user ID " + userId + ". Cannot remove.")); // More specific error
        itemRepo.delete(item);
    }

    @Override
    @Transactional(readOnly = true) // Good practice for read operations
    public List<CartItem> getCartItems(Long userId) {
        Cart cart = cartRepo.findByUser_UserId(userId);
        return cart != null ? itemRepo.findByCart_CartId(cart.getCartId()) : new ArrayList<>();
    }}