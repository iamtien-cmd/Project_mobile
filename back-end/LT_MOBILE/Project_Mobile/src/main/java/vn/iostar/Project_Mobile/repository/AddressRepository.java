package vn.iostar.Project_Mobile.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iostar.Project_Mobile.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}