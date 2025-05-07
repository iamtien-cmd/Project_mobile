package vn.iostar.doan.model;

// import lombok.Data;
// @Data
public class OrderLine {

    // Các trường đã có
    private long orderLineId;
    private int quantity;
    // private Order order; // Thường không cần back reference ở Android Model
    private Product2 product;

    // === THÊM TRƯỜNG CÒN THIẾU ===
    private double price; // Giá sản phẩm tại thời điểm đặt hàng
    // =============================

    // === TẠO GETTER/SETTER CHO TRƯỜNG MỚI ===
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // --- Getters/Setters cho các trường đã có ---
    public long getOrderLineId() { return orderLineId; }
    public void setOrderLineId(long orderLineId) { this.orderLineId = orderLineId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Product2 getProduct() { return product; }
    public void setProduct(Product2 product) { this.product = product; }
}