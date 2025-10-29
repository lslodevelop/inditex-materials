package com.inditex.assets.interfaces.web.model.asset;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "DTO that models the asset")
public class AssetDto {

    @Schema(description = "Asset ID")
    private String id;

    @Schema(description = "Asset file name")
    private String filename;

    @Schema(description = "Asset encoded file")
    private String encodedFile;

    @Schema(description = "Asset content type")
    private String contentType;

    @Schema(description = "Asset URL (in case it was successfully uploaded)")
    private String url;

    @Schema(description = "Asset size")
    private Long size;

    @Schema(description = "Asset status")
    private String status;

    @Schema(description = "Asset upload date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime uploadDate;

}
