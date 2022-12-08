package com.triple.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "place_bas")
public class PlaceEntity extends BaseEntity{

    @Id
    @Column(columnDefinition = "BINARY(16)", name = "id")
    private UUID id;

    @Column(columnDefinition = "VARCHAR(100)", name = "name")
    private String name;

    @Builder
    public PlaceEntity(UUID id, String name) {
        this.id = id;;
        this.name = name;
    }
}
