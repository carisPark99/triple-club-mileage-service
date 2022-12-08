package com.triple.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_point_bas")
public class UserPointEntity extends BaseEntity{

    @Id
    @Column(columnDefinition = "BINARY(16)", name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(columnDefinition = "INT", name = "review_tot_point")
    private int reviewPoint;

    @OneToMany(mappedBy = "userPoint", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<UserPointDetail> pointDetailList = new ArrayList<>();

    public void updateReviewTotPoint(int reviewPoint) {
        this.reviewPoint += reviewPoint;
    }

    @Builder
    public UserPointEntity(UUID id, UserEntity user, int reviewPoint) {
        this.id = id;
        this.user = user;
        this.reviewPoint = reviewPoint;
    }
}
