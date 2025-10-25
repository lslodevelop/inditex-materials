package com.example.assets.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Asset {
    private String id;
    private String filename;
    private String contentType;
    private AssetStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
