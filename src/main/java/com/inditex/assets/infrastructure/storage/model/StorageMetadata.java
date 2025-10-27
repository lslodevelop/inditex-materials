package com.inditex.assets.infrastructure.storage.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class StorageMetadata {

    private final String url;
    private final long size;
    private final Instant uploadedAt;

}
