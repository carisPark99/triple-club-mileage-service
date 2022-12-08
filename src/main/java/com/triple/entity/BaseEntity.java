package com.triple.entity;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseEntity {

    @Column(updatable = false, name = "create_dt")
    @CreatedDate
    private LocalDateTime createDt;

    @Column(name = "update_dt")
    @LastModifiedDate
    private LocalDateTime updateDt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createDt = now;
        updateDt = now;
    }

    @PostUpdate
    public void postPersist() {
        updateDt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updateDt = LocalDateTime.now();
    }
}
