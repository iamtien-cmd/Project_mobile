package vn.iostar.Project_Mobile.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty; // Import thư viện này

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   
    private long productId;


    private String image;
    private String name;
    
    @Min(value = 1000, message = "Giá sản phẩm phải lớn hơn hoặc bằng 1.000đ")
    @Column(name = "quantity")
    private double price;
    private String description;
    
    @Min(value = 0, message = "Số lượng sản phẩm trong kho phải lớn hơn hoặc bằng 0")
    @Column(name = "quantity")
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "favoriteId")
    private Favorite favorite;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImagesProduct> images = new ArrayList<>();
}
