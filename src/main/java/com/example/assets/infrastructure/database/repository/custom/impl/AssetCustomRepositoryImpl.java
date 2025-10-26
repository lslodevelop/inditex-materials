package com.example.assets.infrastructure.database.repository.custom.impl;

import com.example.assets.infrastructure.database.entity.AssetEntity;
import com.example.assets.infrastructure.database.repository.custom.AssetCustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class AssetCustomRepositoryImpl implements AssetCustomRepository {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("filename", "content_type", "created_at", "updated_at");

    @Override
    public Flux<AssetEntity> findByFilter(final String filename, final String contentType,
                                          final String sortBy, final String sortDirection) {
        Criteria criteria = Criteria.empty();

        if (StringUtils.hasText(filename)) {
            criteria = criteria.and("filename").like("%" + filename + "%");
        }
        if (StringUtils.hasText(contentType)){
            criteria = criteria.and("content_type").is(contentType);
        }

        Query query = Query.query(criteria);

        // Sort field validation
        String sortField = (sortBy != null && ALLOWED_SORT_FIELDS.contains(sortBy))
                ? sortBy
                : "created_at";

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        query = query.sort(Sort.by(direction, sortField));

        return r2dbcEntityTemplate.select(query, AssetEntity.class);
    }
}
