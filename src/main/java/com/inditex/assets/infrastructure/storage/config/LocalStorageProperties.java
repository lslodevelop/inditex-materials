package com.inditex.assets.infrastructure.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.local")
public record LocalStorageProperties(String basePath) {}
