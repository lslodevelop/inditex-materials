package com.example.assets.interfaces.controller;

import com.example.assets.interfaces.adapter.AssetInputAdapter;
import com.example.assets.interfaces.model.asset.AssetDto;
import com.example.assets.interfaces.model.asset.AssetUploadRequestDto;
import com.example.assets.interfaces.model.asset.AssetUploadResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/mgmt/1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetInputAdapter assetInputAdapter;

    @PostMapping(value = "/actions/upload", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<AssetUploadResponseDto> upload(@Validated @RequestBody final AssetUploadRequestDto request) {
        return assetInputAdapter.upload(request);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Flux<AssetDto> search(@RequestParam(required = false) final String filename,
                                 @RequestParam(required = false) final String contentType,
                                 @RequestParam final String sortBy,
                                 @RequestParam(required = false) final String sortDirection) {
        return assetInputAdapter.search(filename, contentType, sortBy, sortDirection);
    }
}
