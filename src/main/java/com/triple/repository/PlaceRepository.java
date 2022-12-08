package com.triple.repository;

import com.triple.entity.PlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlaceRepository extends JpaRepository<PlaceEntity, UUID> {
}
