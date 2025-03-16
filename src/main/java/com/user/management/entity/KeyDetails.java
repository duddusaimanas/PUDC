package com.user.management.entity;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.crypto.SecretKey;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class KeyDetails {

    @Id
    private UUID keyId;

    private SecretKey secretKey;

    @Enumerated(EnumType.STRING)
    private KeyStatus keyStatus;

    private ZonedDateTime expiry;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("KeyDetails [");
        Integer init = sb.length();
        if (this.keyId != null)
            sb.append("keyId=").append(this.keyId).append(", ");
        if (this.keyStatus != null)
            sb.append("keyStatus=").append(this.keyStatus).append(", ");
        if (this.expiry != null)
            sb.append("expiry=").append(this.expiry).append(", ");
        if (sb.length() > init)
            sb.delete(sb.length() - 2, sb.length());
        return sb.append("]").toString();
    }
}
