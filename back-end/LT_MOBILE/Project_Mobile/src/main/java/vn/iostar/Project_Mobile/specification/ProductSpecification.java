package vn.iostar.Project_Mobile.specification;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import vn.iostar.Project_Mobile.DTO.ProductSearchRequest;
import vn.iostar.Project_Mobile.entity.Product;

import java.util.*;
public class ProductSpecification {
	public static Specification<Product> withFilters(ProductSearchRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + req.getKeyword().toLowerCase() + "%"));
            }

            if (req.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), req.getMinPrice()));
            }

            if (req.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), req.getMaxPrice()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
