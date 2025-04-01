package vn.iostar.Project_Mobile.entity;

import java.sql.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.iotstar.enums.OrderStatus;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "orders")
public class Order {
	  @Id 
	  @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private long orderId;
	  
	    private double totalPrice;
	    private Date orderDate;
	    private Date predictReceiveDate;
	    
	    @Enumerated(EnumType.STRING)
	    private OrderStatus status;
	    
	    @ManyToOne
	    @JoinColumn(name = "user_id")
	    private User user;
	    
	    @OneToMany(mappedBy = "order")
	    private List<OrderLine> orderLines;
}
