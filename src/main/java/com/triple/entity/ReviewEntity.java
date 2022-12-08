package com.triple.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review_bas")
public class ReviewEntity extends BaseEntity{

    @Id
    @Column(columnDefinition = "BINARY(16)", name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(columnDefinition = "BINARY(16)", name = "place_id")
    private UUID place;

    @Column(columnDefinition = "VARCHAR(1000)", name = "content")
    private String content;

    @OneToMany(mappedBy = "review", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final Set<PhotoEntity> attachedPhotos = new HashSet<>();

    public void updateContent(String content) {
        this.content = content;
    }

    public void addPhoto(PhotoEntity photo) {
        attachedPhotos.add(photo);
        photo.setReview(this);
    }

    public void deletePhoto(PhotoEntity photo) {
        attachedPhotos.remove(photo);
        photo.setReview(null);
    }

    @Builder
    public ReviewEntity(UUID id, UserEntity user, UUID place, String content) {
        this.id = id;
        this.user = user;
        this.place = place;
        this.content = content;
    }
}
