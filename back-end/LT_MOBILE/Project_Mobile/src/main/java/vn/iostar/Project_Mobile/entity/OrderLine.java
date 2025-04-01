package vn.iostar.Project_Mobile.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class OrderLine {
	 @Id 
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private long orderLineId;
	    private int quantity;
	    
	    @ManyToOne
	    private Order order;
	    
	    @ManyToOne
	    private Product product;
}
