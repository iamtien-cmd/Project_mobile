package vn.iostar.Project_Mobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.iostar.Project_Mobile.entity.Comment;
import vn.iostar.Project_Mobile.entity.Order;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.entity.User;

import java.util.List;
import java.util.Set;

@Repository
public interface ICommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByProduct(Product product);

    // Đếm số lượng sản phẩm ĐỘC ĐÁO mà người dùng đã đánh giá (comment.reviewed = true)
    // CHO MỘT ĐƠN HÀNG CỤ THỂ và nằm trong một tập hợp các ID sản phẩm nhất định.
    @Query("SELECT COUNT(DISTINCT c.product.id) FROM Comment c " +
           "WHERE c.user = :user " +
           "AND c.order = :order " +
           "AND c.product.id IN :productIds " + // Giả sử ID của Product là 'id'
           "AND c.reviewed = true")
    long countDistinctProductsReviewedByUserForOrder(
            @Param("user") User user,
            @Param("order") Order order,
            @Param("productIds") Set<Long> productIds
    );

    // Kiểm tra xem user đã review product này TRONG order này chưa
    boolean existsByUserAndProductAndOrderAndReviewedIsTrue(User user, Product product, Order order);

    // Query cũ của bạn (nếu vẫn cần với ý nghĩa khác)
    boolean existsByUserAndProductAndReviewedIsTrue(User user, Product product);

    List<Comment> findByUserAndProduct(User user, Product product);

	List<Comment> findByUserAndProductAndReviewedIsTrue(User user, Product productInOrder);
	
}