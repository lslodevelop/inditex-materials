package com.example.assetservice.interfaces.controller;

import com.example.assetservice.interfaces.adapter.AssetInputAdapter;
import com.example.assetservice.interfaces.dto.AssetRequestDto;
import com.example.assetservice.interfaces.dto.AssetResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public Mono<ResponseEntity<AssetResponseDto>> upload(@Validated @RequestBody AssetRequestDto request) {
        return assetInputAdapter.upload(request)
                .map(dto -> ResponseEntity.accepted().body(dto));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<AssetResponseDto> search(@RequestParam(required = false) String filename,
                                         @RequestParam(required = false) String contentType) {
        return assetInputAdapter.search(filename, contentType);
    }
}
