package com.user.management.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest implements UserDetails {

    private String username;
    private String password;
    private String newPassword;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(PortalPermissions.USER);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PasswordChangeRequest [");
        Integer init = sb.length();
        if (this.username != null)
            sb.append("username=").append(this.username).append(", ");
        if (sb.length() > init)
            sb.delete(sb.length() - 2, sb.length());
        return sb.append("]").toString();
    }
}
