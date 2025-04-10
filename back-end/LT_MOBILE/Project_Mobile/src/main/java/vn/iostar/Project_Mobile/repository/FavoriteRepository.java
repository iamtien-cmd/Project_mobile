package vn.iostar.Project_Mobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iostar.Project_Mobile.entity.Favorite;

public interface FavoriteRepository  extends JpaRepository<Favorite, Long> {

}
