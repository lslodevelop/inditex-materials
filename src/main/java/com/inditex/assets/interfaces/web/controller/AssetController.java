package com.inditex.assets.interfaces.web.controller;

import com.inditex.assets.interfaces.web.adapter.AssetAdapter;
import com.inditex.assets.interfaces.web.model.asset.AssetDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadRequestDto;
import com.inditex.assets.interfaces.web.model.asset.AssetUploadResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/mgmt/1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetAdapter assetAdapter;

    @Operation(
            summary = "Uploads a new asset",
            description = "Allows to upload a Base64 encoded file with it's metadata to the configured storage (MinIO or local)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Asset upload request accepted"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal error when performing upload")
    })
    @PostMapping(value = "/actions/upload", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<AssetUploadResponseDto> upload(@Validated @RequestBody final AssetUploadRequestDto request) {
        return assetAdapter.upload(request);
    }

    @Operation(
            summary = "Asset's metadata search from DB along with the file content in case it was successfully uploaded",
            description = "Allows to retrieve the assets based on the provided criteria"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset successfully retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal error when performing search")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Flux<AssetDto> search(@RequestParam(required = false) final String filename,
                                 @RequestParam(required = false) final String contentType,
                                 @RequestParam final String sortBy,
                                 @RequestParam(required = false) final String sortDirection) {
        return assetAdapter.search(filename, contentType, sortBy, sortDirection);
    }
}
