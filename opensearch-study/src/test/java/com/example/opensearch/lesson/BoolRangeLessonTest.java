package com.example.opensearch.lesson;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import com.example.opensearch.support.OpenSearchIntegrationSmokeTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;

import youngsu.study.opensearchstudy.lesson.BoolRangeDocument;
import youngsu.study.opensearchstudy.lesson.BoolRangeLesson;

class BoolRangeLessonTest extends OpenSearchIntegrationSmokeTestSupport {

    @Autowired
    private BoolRangeLesson boolRangeLesson;

    @BeforeEach
    void setUp() {
        boolRangeLesson.prepareSampleIndex();
    }

    @AfterEach
    void tearDown() {
        boolRangeLesson.deleteIndexIfExists();
    }

    @Test
    @DisplayName("price 범위를 range 필터로 조회한다 (100~200 포함)")
    void priceRangeFilter_case1() {
        final Query query = Query.of(q -> q.bool(bool -> bool
                .filter(filter -> filter.range(r -> r.field("price")
                        .gte(JsonData.of(100.0))
                        .lte(JsonData.of(200.0))))
        ));

        final SearchRequest request = new SearchRequest.Builder()
                .index(BoolRangeLesson.INDEX_NAME)
                .query(query)
                .size(10)
                .build();

        final var result = executeSearch(request, BoolRangeDocument.class);

        assertThat(result)
                .extracting(BoolRangeDocument::price)
                .allMatch(price -> price >= 100.0 && price <= 200.0);
    }

    @Test
    @DisplayName("price 범위를 range 필터로 조회한다 (200초과)")
    void priceRangeFilter_case2() {
        final Query query = Query.of(q -> q.bool(bool -> bool
                .filter(filter -> filter.range(r -> r.field("price")
                        .gt(JsonData.of(200.0))))
        ));

        final SearchRequest request = new SearchRequest.Builder()
                .index(BoolRangeLesson.INDEX_NAME)
                .query(query)
                .build();

        final var result = executeSearch(request, BoolRangeDocument.class);

        assertThat(result)
                .hasSize(1)
                .extracting(BoolRangeDocument::price)
                .allMatch(price -> price > 200);
    }


    @Test
    @DisplayName("2-3-2 Bool Range: must_not 절로 비활성 인스턴스를 제외한다")
    void excludesUnavailableSearchClusters() {
        // false 인거는 가져오지 않는다.
        final var query = Query.of(q -> q.bool(bool -> bool
                .mustNot(m -> m.term(t -> t.field("availability").value(FieldValue.FALSE))))
        );
        final var request = SearchRequest.of(s -> s
                .index(BoolRangeLesson.INDEX_NAME)
                .query(query));

        final List<BoolRangeDocument> results = executeSearch(request, BoolRangeDocument.class);

        assertThat(results)
                .extracting(BoolRangeDocument::availability)
                .allMatch(availability -> availability == true);
    }

    @Test
    @DisplayName("특정 시간 이후만 조회한다")
    void excludesExpiredReleaseAt() {
        final var time = Instant.parse("2024-01-01T00:00:00Z");
        final var query = Query.of(q -> q.bool(bool -> bool
                .filter(filter -> filter.range(
                        range -> range.field("releasedAt")
                                .gte(JsonData.of(time))))
        ));
        final var request = SearchRequest.of(s -> s
                .index(BoolRangeLesson.INDEX_NAME)
                .query(query));

        final List<BoolRangeDocument> results = executeSearch(request, BoolRangeDocument.class);

        assertThat(results)
                .extracting(BoolRangeDocument::releasedAt)
                .allMatch(releasedAt -> releasedAt.isAfter(time));
    }
}
