package vn.iostar.doan.model;

public class OrderLine {
    private long orderLineId;
    private int quantity;
    private Product product;

    public long getOrderLineId() { return orderLineId; }
    public int getQuantity() { return quantity; }
    public Product getProduct() { return product; }
}
