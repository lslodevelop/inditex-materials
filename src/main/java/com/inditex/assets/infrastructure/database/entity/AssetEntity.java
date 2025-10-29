package com.inditex.assets.infrastructure.database.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Table("assets")
public class AssetEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    private String filename;
    private String contentType;
    private String url;
    private Long size;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant uploadedAt;

    @Transient
    private boolean newAsset;

    @Override
    public boolean isNew() {
        return this.newAsset || id == null;
    }

}
