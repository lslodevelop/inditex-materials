package com.example.assetservice.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Asset {
    private String id;
    private String filename;
    private String contentType;
    private Long size;
    private String url;
    private Instant createdAt;
    private AssetStatus status;
    private String errorMessage;
}
