package com.inditex.assets.integration;

import com.inditex.assets.domain.model.Asset;
import com.inditex.assets.domain.port.out.StorageClientPort;
import com.inditex.assets.infrastructure.storage.model.StorageMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIT {

    @MockitoSpyBean
    protected StorageClientPort storageClientPort;

    @BeforeEach
    void setupStorageSpy() {
        final StorageMetadata defaultMeta = StorageMetadata.builder()
                .url("mock://assets/test-file")
                .size(123L)
                .uploadedAt(Instant.now())
                .build();

        doReturn(Mono.just(defaultMeta))
                .when(storageClientPort)
                .upload(any(Asset.class), anyString());
    }

}
