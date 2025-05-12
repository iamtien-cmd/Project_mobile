package vn.iostar.Project_Mobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.Project_Mobile.entity.OrderItem;
import vn.iostar.Project_Mobile.entity.PurchaseReview;
import java.util.Optional;

@Repository
public interface IPurchaseReviewRepository extends JpaRepository<PurchaseReview, Long> {
    boolean existsByOrderItem(OrderLine orderItem);
    Optional<PurchaseReview> findByOrderItem(OrderLine orderItem);
    // Bạn cũng có thể viết:
    // boolean existsByOrderItem_OrderItemId(Long orderItemId);
}