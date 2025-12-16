package com.example.opensearch.lesson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;

import com.example.opensearch.support.OpenSearchIntegrationSmokeTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;

import youngsu.study.opensearchstudy.lesson.MappingAndTypesLesson;
import youngsu.study.opensearchstudy.lesson.MappingTypedDocument;

class MappingAndTypesLessonTest extends OpenSearchIntegrationSmokeTestSupport {

    @Autowired
    private MappingAndTypesLesson mappingAndTypesLesson;

    @BeforeEach
    void setUp() {
        mappingAndTypesLesson.prepareSampleIndex();
    }

    @AfterEach
    void tearDown() {
        mappingAndTypesLesson.deleteIndexIfExists();
    }

    @Test
    @DisplayName("각 필드 타입을 명시적으로 정의한다")
    void verifyMappingTypes() throws IOException {
        final var response = client.indices().getMapping(
                get -> get.index(MappingAndTypesLesson.INDEX_NAME)
        );

        final var index = response.get(MappingAndTypesLesson.INDEX_NAME);
        final var properties = index.mappings().properties();
        // Property 는 다양한 속성들 제공

        assertThat(properties.get("latencyMs").isLong()).isTrue();
        assertThat(properties.get("errorRate").isScaledFloat()).isTrue();
        assertThat(properties.get("active").isBoolean()).isTrue();
        assertThat(properties.get("releasedAt").isDate()).isTrue();
        assertThat(properties.get("description").isText()).isTrue();
    }

    @Test
    @DisplayName("scaled_float 필드에 range 필터를 적용한다")
    void filterByScaledFloat() {
        // scaled_float
        // long 에 scale factor 를 곱해 저장하는 부동소수 표현
        // {"type":"scaled_float","scaling_factor":100}
        // -> 1.23, 을 123으로 저장 ( 1.23 * 100 = 123 )
        // 정밀도는 1 / scaling_factor 단위로 고정 ( 1 / 100 = 0.01 )
        // 집계, 정렬, 스코어 계산 시 float 보다 안정적 결과 + 공간, 성능 비용이 덜 듬
        final Query query = Query.of(
                q ->
                        q.bool(bool -> bool
                                .must(m -> m.term(t -> t.field("active").value(FieldValue.TRUE)))
                                .filter(filter -> filter.range(r -> r.field("errorRate").lte(JsonData.of(0.01))))
                        )
        );
        final var search = SearchRequest.of(s -> s.query(query).size(5));

        final var result = executeSearch(search, MappingTypedDocument.class);

        assertThat(result)
                .extracting(MappingTypedDocument::id)
                .containsExactlyInAnyOrder("svc-latency", "svc-green");
    }

    @Test
    @DisplayName("date 필드에 range 필터를 적용한다")
    void filterByDate() {
        final Query query = Query.of(
                q -> q.bool(
                        b -> b.filter(f -> f.range(r -> r.field("releasedAt").gte(JsonData.of("2024-01-10T00:00:00Z")
                        )))
                )
        );
        final var request = SearchRequest.of(s -> s.query(query).size(5));
        final var response = executeSearch(request, MappingTypedDocument.class);
        assertThat(response).hasSize(2);
    }

    @Test
    @DisplayName("should 절은 점수를 올리고 filter 는 점수 없이 결과만 거른다")
    void shouldBoostsRankingWhileFilterOnlyRestricts() throws IOException {
        final Query query = Query.of(
                q -> q.bool(bool -> bool
                        .must(m -> m.match(mm -> mm.field("description").query(FieldValue.of("service"))))
                        .filter(filter -> filter.range(r -> r.field("errorRate").lte(JsonData.of(0.02))))
                        .should(s -> s.match(m -> m.field("description").query(FieldValue.of("search")).boost(2.0f))))
        );

        final var response = client.search(
                SearchRequest.of(s -> s.index(MappingAndTypesLesson.INDEX_NAME).query(query).size(5)),
                MappingTypedDocument.class
        );

        final var hits = response.hits().hits();
        assertThat(hits).hasSize(3);
        assertThat(hits.getFirst().id()).isEqualTo("svc-green");

        final var scoreById = hits.stream().collect(toMap(Hit::id, Hit::score));
        assertThat(scoreById.get("svc-green"))
                .isGreaterThan(scoreById.get("svc-latency"))
                .isGreaterThan(scoreById.get("svc-canary"));
    }

    @Test
    @DisplayName("must 가 없으면 should 는 최소 하나를 만족해야 매칭된다")
    void shouldBecomesRequiredWhenNoMust() throws IOException {
        final Query query = Query.of(
                q -> q.bool(bool -> bool
                        .should(s -> s.match(m -> m.field("description").query(FieldValue.of("gateway"))))
                        .should(s -> s.match(m -> m.field("description").query(FieldValue.of("search")))))
        );

        final var response = client.search(
                SearchRequest.of(s -> s.index(MappingAndTypesLesson.INDEX_NAME).query(query).size(5)),
                MappingTypedDocument.class
        );

        assertThat(response.hits().hits())
                .extracting(Hit::id)
                .containsExactlyInAnyOrder("svc-latency", "svc-green");
    }

    @Test
    @DisplayName("filter 절을 추가해도 동일 문서의 스코어는 변하지 않는다")
    void filterDoesNotChangeScore() throws IOException {
        final var withoutFilter = client.search(
                SearchRequest.of(s -> s.index(MappingAndTypesLesson.INDEX_NAME)
                        .query(q -> q.bool(b -> b.must(m -> m.match(mm -> mm.field("description").query(FieldValue.of("service latency"))))))
                        .size(5)),
                MappingTypedDocument.class
        );

        final var withFilter = client.search(
                SearchRequest.of(s -> s.index(MappingAndTypesLesson.INDEX_NAME)
                        .query(q -> q.bool(b -> b
                                .must(m -> m.match(mm -> mm.field("description").query(FieldValue.of("service latency"))))
                                .filter(f -> f.term(t -> t.field("active").value(FieldValue.TRUE)))))
                        .size(5)),
                MappingTypedDocument.class
        );

        final double scoreWithoutFilter = scoreFor("svc-latency", withoutFilter);
        final double scoreWithFilter = scoreFor("svc-latency", withFilter);

        assertThat(scoreWithFilter).isCloseTo(scoreWithoutFilter, within(0.0001));
    }

    private double scoreFor(final String id, final SearchResponse<MappingTypedDocument> response) {
        return response.hits().hits().stream()
                .filter(hit -> id.equals(hit.id()))
                .findFirst()
                .map(hit -> hit.score() == null ? 0.0 : hit.score())
                .orElseThrow();
    }
}
