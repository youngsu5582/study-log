package com.example.opensearch.support;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import youngsu.study.opensearchstudy.OpensearchStudyApplication;
import youngsu.study.opensearchstudy.lesson.BoolRangeDocument;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(classes = OpensearchStudyApplication.class)
public class OpenSearchIntegrationSmokeTestSupport {
    protected static final String EXTERNAL_URI = "http://localhost:9200";

    @DynamicPropertySource
    static void overrideWithExternalCluster(final DynamicPropertyRegistry registry) {
        assumeTrue(EXTERNAL_URI != null && !EXTERNAL_URI.isBlank(),
                "EXTERNAL_OPENSEARCH_URI 환경 변수가 설정되지 않아 외부 클러스터 검증을 건너뜁니다.");
        registry.add("opensearch.client.uris[0]", () -> EXTERNAL_URI);
    }

    @Autowired
    protected OpenSearchClient client;

    protected <T> List<T> executeSearch(final SearchRequest request, final Class<T> clazz) {
        try {
            final SearchResponse<T> response = client.search(request, clazz);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (final IOException ioException) {
            throw new UncheckedIOException(ioException);
        }
    }
}
