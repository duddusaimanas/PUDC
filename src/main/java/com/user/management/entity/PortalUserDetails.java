package com.user.management.entity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortalUserDetails implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid default uuid_generate_v4()")
    private UUID id;

    private String username;
    private String password;
    private String name;

    private List<UUID> conversationIds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'USER'")
    private PortalPermissions permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(permissions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PortalUserDetails [");
        Integer init = sb.length();
        if (this.id != null)
            sb.append("id=").append(this.id).append(", ");
        if (this.username != null)
            sb.append("username=").append(this.username).append(", ");
        if (this.name != null)
            sb.append("name=").append(this.name).append(", ");
        if (this.permissions != null)
            sb.append("permissions=").append(this.permissions).append(", ");
        if (sb.length() > init)
            sb.delete(sb.length() - 2, sb.length());
        return sb.append("]").toString();
    }
}
