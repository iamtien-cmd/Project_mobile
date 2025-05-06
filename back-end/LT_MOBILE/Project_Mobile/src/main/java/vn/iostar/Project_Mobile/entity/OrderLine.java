package vn.iostar.Project_Mobile.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "order_line") // rõ ràng, tránh lỗi ngầm
public class OrderLine {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderLineId;

    private int quantity;
    private double price;

    @ManyToOne
    @JoinColumn(name = "order_id") // KHÔNG ĐƯỢC DÙNG "order"
    @JsonIgnore
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
