package vn.iostar.Project_Mobile.DTO;

import lombok.Data;
import vn.iostar.Project_Mobile.util.PaymentMethod; // Import enum PaymentMethod
import java.util.List;

@Data
public class CreateOrderRequest {
	private List<Long> cartItemIds; 
    private Long addressId;         
    private PaymentMethod paymentMethod;
}
