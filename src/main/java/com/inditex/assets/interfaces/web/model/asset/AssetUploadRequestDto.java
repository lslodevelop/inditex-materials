package com.inditex.assets.interfaces.web.model.asset;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetUploadRequestDto {

    @NotBlank
    private String filename;

    @NotBlank
    private String encodedFile; // base64

    @NotBlank
    private String contentType;

}
