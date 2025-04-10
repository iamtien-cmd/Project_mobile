package vn.iostar.Project_Mobile.entity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Favorite")
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long favoriteId;

    @JsonProperty("name") // Ánh xạ "name" từ JSON vào favoriteName
    private String favoriteName;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @OneToMany(mappedBy = "favorite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;
}
