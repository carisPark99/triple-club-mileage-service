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
@Table(name = "user_point_detail")
public class UserPointDetail extends BaseEntity{

    @Id
    @Column(columnDefinition = "BINARY(16)", name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_point_id")
    private UserPointEntity userPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private ReviewEntity review;

    @Column(columnDefinition = "INT", name = "review_point")
    private int reviewPoint;

    @Column(columnDefinition = "INT", name = "mod_review_point")
    private int modReviewPoint;

    public void updateDdtReviewPoint(int point) {
        this.modReviewPoint = point;
    }

    @Builder
    public UserPointDetail(UUID id, UserPointEntity userPoint, ReviewEntity review, int reviewPoint, int ddtReviewPoint) {
        this.id = id;
        this.userPoint = userPoint;
        this.review = review;
        this.reviewPoint = reviewPoint;
        this.modReviewPoint = ddtReviewPoint;
    }
}
