package vn.iostar.Project_Mobile.service;

import java.util.List;

import vn.iostar.Project_Mobile.entity.Comment;
import vn.iostar.Project_Mobile.entity.Product;

public interface ICommentService {
    Comment save(Comment comment);
    List<Comment> getCommentsByProduct(Product product);
    void deleteComment(Long id);
}
