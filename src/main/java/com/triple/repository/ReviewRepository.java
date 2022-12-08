package com.triple.repository;

import com.triple.entity.PlaceEntity;
import com.triple.entity.ReviewEntity;
import com.triple.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    Optional<List<ReviewEntity>> findByPlace(UUID placeId);

    Optional<List<ReviewEntity>> findByUser(UserEntity user);

    boolean existsByPlaceAndUser(UUID place, UserEntity user);

    Optional<ReviewEntity> findByUserAndPlace(UserEntity user, UUID place);

    Optional<ReviewEntity> findByUserAndId(UserEntity user, UUID reviewId);
}
