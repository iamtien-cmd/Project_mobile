package vn.iostar.Project_Mobile.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
  

    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;

    private String fullName;

    private String otpCode;

    private LocalDateTime otpExpiration;
    private boolean active;


    // Quan hệ Nhiều-Nhiều với Address
    @ManyToMany
    @JoinTable(
        name = "user_address",  // Tên bảng trung gian
        joinColumns = @JoinColumn(name = "user_id"),  // Khóa ngoại tham chiếu đến User
        inverseJoinColumns = @JoinColumn(name = "address_id") // Khóa ngoại tham chiếu đến Address
    )
    private List<Address> addresses;
    
 // Quan hệ 1-N: Một User có nhiều Comment
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();
    
    @OneToOne(mappedBy = "user")
    private Favorite favorite;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    
}
