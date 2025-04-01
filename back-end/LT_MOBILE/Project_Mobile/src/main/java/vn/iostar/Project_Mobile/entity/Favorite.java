package vn.iostar.Project_Mobile.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
	 @OneToOne
	    @JoinColumn(name = "user_id")
	    private User user;
	    
	    @ManyToMany
	    @JoinTable(
	        name = "favorite_product",
	        joinColumns = @JoinColumn(name = "favorite_id"),
	        inverseJoinColumns = @JoinColumn(name = "product_id")
	    )
	    private List<Product> products;
}
