package com.example.library.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "AUDIT_LOGS")
public class AuditLog {
    @Id
    @Column(name = "ID")
    private String id;
    //Thời gian hành động xảy ra
    @Column(name = "TIMESTAMP")
    private Instant timestamp;
    //Id của người thực hiện
    @Column(name = "ACTOR_ID")
    private String actorId;
    //Vai trò của actor tại thời điểm thực hiện
    @Column(name = "ACTOR_ROLE")
    private String actorRole;
    //Quyền của actor tại thời điểm thực hiện
    @Column(name = "ACTOR_PERMISSIONS_SNAPSHOT", columnDefinition = "JSON")
    private String actorPermissionsSnapshot;
    //Loại hành động
    @Column(name = "ACTION")
    private String action;
    //Loại đối tượng bị tác động
    @Column(name = "TARGET_TYPE")
    private String targetType;
    //ID cụ thể của đối tượng bị tác động
    @Column(name = "TARGET_ID")
    private String targetId;
    @Column(name = "OLD_VALUE", columnDefinition = "JSON")
    private String oldValue;
    @Column(name = "NEW_VALUE", columnDefinition = "JSON")
    private String newValue;
    @Column(name = "STATUS")
    private String status;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
