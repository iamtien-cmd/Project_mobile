package vn.iostar.Project_Mobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.iostar.Project_Mobile.entity.Order;
public interface OrderRepository extends JpaRepository<Order, Long> {

}
