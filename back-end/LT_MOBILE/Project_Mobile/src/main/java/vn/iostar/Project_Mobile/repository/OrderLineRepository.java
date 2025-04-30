package vn.iostar.Project_Mobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.iostar.Project_Mobile.entity.OrderLine;

public interface OrderLineRepository  extends JpaRepository<OrderLine, Long> {

}
