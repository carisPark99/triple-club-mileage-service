package com.triple.repository;

import com.triple.entity.UserEntity;
import com.triple.entity.UserPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserPointRepository extends JpaRepository<UserPointEntity, UUID> {
    Optional<UserPointEntity> findByUser(UserEntity userId);
}
