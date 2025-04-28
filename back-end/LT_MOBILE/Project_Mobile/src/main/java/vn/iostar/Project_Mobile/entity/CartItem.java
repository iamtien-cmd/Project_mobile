package vn.iostar.Project_Mobile.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class CartItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long cartItemId;
	 
	private int quantity;
	
	@ManyToOne
	@JoinColumn(name = "cartId", nullable = false)
	@JsonIgnore
	private Cart cart;

	@ManyToOne
	@JoinColumn(name = "productId", nullable = false)
	private Product product;
}
