package com.inditex.assets.interfaces.web.model.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Schema(description = "DTO that models the asset upload response")
public class AssetUploadResponseDto {

    @Schema(description = "Asset ID")
    private String id;

}
