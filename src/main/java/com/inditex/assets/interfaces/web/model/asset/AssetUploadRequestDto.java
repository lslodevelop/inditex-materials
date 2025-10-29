package com.inditex.assets.interfaces.web.model.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO that models the asset upload request")
public class AssetUploadRequestDto {

    @Schema(description = "File name")
    @NotBlank
    private String filename;

    @Schema(description = "Encoded file in Base64")
    @NotBlank
    private String encodedFile; // base64

    @Schema(description = "Content type")
    @NotBlank
    private String contentType;

}
