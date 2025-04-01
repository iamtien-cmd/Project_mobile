package vn.iostar.Project_Mobile.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Address")
public class Address {
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private long addressId;
	 private String houseNumber;
	 private String street;
	 private String city;
	 private String country;
	 
	 // Quan hệ Nhiều-Nhiều với User (hai chiều)
	    @ManyToMany(mappedBy = "addresses")
	    private List<User> users = new ArrayList<>();

}
