package com.inditex.assets.interfaces.web.model.asset;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssetDto {

    private String id;
    private String filename;
    private String encodedFile;
    private String contentType;
    private String url;
    private Long size;
    private String status;
    private LocalDateTime uploadDate;

}
