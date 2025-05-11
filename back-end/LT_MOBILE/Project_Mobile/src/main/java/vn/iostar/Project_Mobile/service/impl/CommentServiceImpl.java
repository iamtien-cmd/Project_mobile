package vn.iostar.Project_Mobile.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Quan trọng

import vn.iostar.Project_Mobile.DTO.CommentResponse;
import vn.iostar.Project_Mobile.entity.Comment;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.entity.User; // Thêm User
import vn.iostar.Project_Mobile.entity.OrderLine; // Thêm OrderLine

import vn.iostar.Project_Mobile.repository.*;
import vn.iostar.Project_Mobile.repository.IUserRepository; // Thêm repository cho User
import vn.iostar.Project_Mobile.service.ICommentService;
import vn.iostar.Project_Mobile.util.OrderStatus; // Giả sử bạn có enum này và OrderLine có liên kết đến Order

@Service
public class CommentServiceImpl implements ICommentService {

	@Autowired
	private ICommentRepository commentRepository; // Giả sử đây là tên đúng của repo

	// Thêm các repository cần thiết
	@Autowired
	private IUserRepository userRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private OrderLineRepository orderLineRepository; // Cần tạo file này

	@Override
	public Comment save(Comment comment) {
		// Bạn có thể cần logic kiểm tra trước khi lưu ở đây nếu dùng trực tiếp
		return commentRepository.save(comment);
	}

	@Override
	public List<CommentResponse> getCommentsByProduct(Product product) {
		return commentRepository.findByProduct(product).stream().map(comment -> {
			CommentResponse response = new CommentResponse();
			response.setCommentId(comment.getCommentId());
			response.setImage(comment.getImage());
			response.setContent(comment.getContent());
			response.setRating(comment.getRating());
			response.setCreatedAt(comment.getCreatedAt());
			if (comment.getUser() != null) {
				response.setFullname(comment.getUser().getFullName());
				response.setAvatar(comment.getUser().getAvatar());
			} else {
				response.setFullname("Anonymous User"); // Hoặc giá trị mặc định khác
				// response.setAvatar(null); // hoặc avatar mặc định
			}
			return response;
		}).collect(Collectors.toList());
	}

	@Override
	public void deleteComment(Long id) {
		commentRepository.deleteById(id);
	}

	@Override
	public boolean hasUserReviewedProduct(User user, Product product) {
		return commentRepository.existsByUserAndProductAndReviewedIsTrue(user, product);
	}

}