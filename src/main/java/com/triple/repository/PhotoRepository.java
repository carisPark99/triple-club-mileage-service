package com.triple.repository;

import com.triple.entity.PhotoEntity;
import com.triple.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<PhotoEntity, UUID> {
    List<PhotoEntity> findByReview(ReviewEntity review);
}
