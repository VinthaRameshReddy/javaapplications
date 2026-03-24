package com.medicard.integration.domain.entity;

import jakarta.persistence.Id;  // Updated import
import jakarta.persistence.MappedSuperclass;  // Updated import
import jakarta.persistence.Version;  // Updated import
import java.util.UUID;

@MappedSuperclass
public class AbstractBaseEntity {

    @Id
    private String id;

    @Version
    private int version;

    public AbstractBaseEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractBaseEntity)) {
            return false;
        }
        AbstractBaseEntity other = (AbstractBaseEntity) obj;
        return getId().equals(other.getId());
    }
}