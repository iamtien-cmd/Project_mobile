package vn.iostar.Project_Mobile.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Comment")
public class Comment {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long commentId;
	private String image;
	private String content;
	private int rating;
	
	   // Quan hệ N-1: Một Comment thuộc về 1 User
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // Quan hệ N-1: Một Comment dành cho 1 Product
    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private Product product;

	
}
