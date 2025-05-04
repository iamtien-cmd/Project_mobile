package vn.iostar.Project_Mobile.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.entity.OrderLine;

public interface OrderLineRepository  extends JpaRepository<OrderLine, Long> {
	@Query("SELECT o FROM Order o WHERE o.user.userId = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);
}
