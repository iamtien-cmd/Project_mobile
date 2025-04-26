package vn.iostar.Project_Mobile.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iostar.Project_Mobile.entity.Comment;
import vn.iostar.Project_Mobile.entity.Product;

public interface ICommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByProduct(Product product);
}
