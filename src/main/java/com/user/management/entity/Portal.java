package com.user.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Portal {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) default 'CLOSED'")
    private PortalStatus portalStatus;
}
