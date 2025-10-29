package com.inditex.assets.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Asset {
    private String id;
    private String filename;
    private String encodedFile;
    private String contentType;
    private String url;
    private Long size;
    private AssetStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant uploadedAt;
}
