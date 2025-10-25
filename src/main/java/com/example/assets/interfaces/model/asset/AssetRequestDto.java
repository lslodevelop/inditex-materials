package com.example.assets.interfaces.model.asset;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssetRequestDto {

    @NotBlank
    private String filename;

    @NotBlank
    private String encodedFile; // base64

    @NotBlank
    private String contentType;

}
