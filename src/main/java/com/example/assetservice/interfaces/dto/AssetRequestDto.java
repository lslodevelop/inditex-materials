package com.example.assetservice.interfaces.dto;

import lombok.Data;

@Data
public class AssetRequestDto {

    private String filename;
    private String encodedFile; // base64
    private String contentType;

}
