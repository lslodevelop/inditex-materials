package com.example.assetservice.interfaces.dto;

import lombok.Data;

@Data
public class AssetResponseDto {
    private String id;
    private String filename;
    private String contentType;
    private String status;
}
