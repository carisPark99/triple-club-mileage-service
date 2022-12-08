package com.triple.repository;

import com.triple.entity.UserPointDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPointDetailRepository extends JpaRepository<UserPointDetail, UUID> {
}
