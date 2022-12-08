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
@Table(name = "review_photo_bas")
public class PhotoEntity extends BaseEntity{

    @Id
    @Column(columnDefinition = "BINARY(16)", name = "id")
    private UUID id;

    @Column(columnDefinition = "BINARY(16)", name = "photo_id")
    private UUID photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private ReviewEntity review;

    @Column(columnDefinition = "VARCHAR(100)", name = "name")
    private String name;

    public void updatePhoto(UUID photoId) {
        this.photoId = photoId;
    }

    public void setReview(ReviewEntity review) {
        this.review = review;
    }

    @Builder
    public PhotoEntity(UUID id, UUID photoId, ReviewEntity review, String name) {
        this.id = id;
        this.photoId = photoId;
        this.review = review;
        this.name = name;
    }
}
