package vn.iostar.Project_Mobile.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iostar.Project_Mobile.entity.Order;

public interface IOrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserUserId(Long userId);
}
