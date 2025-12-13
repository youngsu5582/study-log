package youngsu.study.opensearchstudy.lesson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.endpoints.BooleanResponse;
import org.springframework.stereotype.Service;

@Service
public class BoolRangeLesson {

    public static final String INDEX_NAME = "lesson-bool-range";

    private final OpenSearchClient client;

    public BoolRangeLesson(final OpenSearchClient client) {
        this.client = client;
    }

    public void prepareSampleIndex() {
        deleteIndexIfExists();
        createIndex();
        indexSamples();
    }

    public void deleteIndexIfExists() {
        try {
            final BooleanResponse exists = client.indices()
                    .exists(ExistsRequest.of(builder -> builder.index(INDEX_NAME)));
            if (!Boolean.TRUE.equals(exists.value())) {
                return;
            }
            client.indices().delete(DeleteIndexRequest.of(builder -> builder.index(INDEX_NAME)));
        } catch (final IOException ioException) {
            throw new UncheckedIOException("Failed to delete index %s".formatted(INDEX_NAME), ioException);
        }
    }

    public List<BoolRangeDocument> searchAnalyticsWithinBudget(final double minPrice,
                                                               final double maxPrice,
                                                               final Instant releasedAfter,
                                                               final double minRating) {
        final Query query = Query.of(q -> q.bool(bool -> bool
                .must(must -> must.term(t -> t.field("category").value(FieldValue.of("analytics"))))
                .filter(filter -> filter.range(r -> r.field("price")
                        .gte(JsonData.of(minPrice))
                        .lte(JsonData.of(maxPrice))))
                .filter(filter -> filter.range(r -> r.field("releasedAt").gte(JsonData.of(releasedAfter.toString()))))
                .must(must -> must.range(r -> r.field("rating").gte(JsonData.of(minRating))))
        ));

        return executeSearch(query);
    }

    public List<BoolRangeDocument> searchRecentHighAvailability(final Instant releasedAfter) {
        final Query query = Query.of(q -> q.bool(bool -> bool
                .must(m -> m.term(t -> t.field("category").value(FieldValue.of("search"))))
                .filter(f -> f.range(r -> r.field("releasedAt").gte(JsonData.of(releasedAfter.toString()))))
                .mustNot(m -> m.term(t -> t.field("availability").value(FieldValue.of(false))))));

        return executeSearch(query);
    }

    private void createIndex() {
        final TypeMapping mapping = new TypeMapping.Builder()
                .properties("id", p -> p.keyword(k -> k))
                .properties("category", p -> p.keyword(k -> k))
                .properties("price", p -> p.double_(d -> d))
                .properties("rating", p -> p.double_(d -> d))
                .properties("availability", p -> p.boolean_(b -> b))
                .properties("releasedAt", p -> p.date(d -> d))
                .properties("description", p -> p.text(t -> t.analyzer("standard")))
                .build();

        try {
            final CreateIndexRequest request = new CreateIndexRequest.Builder()
                    .index(INDEX_NAME)
                    .settings(setting -> setting.numberOfShards(1).numberOfReplicas(0))
                    .mappings(mapping)
                    .build();
            client.indices().create(request);
        } catch (final IOException ioException) {
            throw new UncheckedIOException("Failed to create index %s".formatted(INDEX_NAME), ioException);
        }
    }

    private void indexSamples() {
        final List<BoolRangeDocument> documents = List.of(
                new BoolRangeDocument(
                        "svc-analytics",
                        "analytics",
                        180.0,
                        4.8,
                        true,
                        Instant.parse("2024-06-10T00:00:00Z"),
                        "실시간 대시보드와 이상 탐지 기능을 제공"
                ),
                new BoolRangeDocument(
                        "svc-legacy",
                        "analytics",
                        90.0,
                        3.8,
                        true,
                        Instant.parse("2023-02-15T00:00:00Z"),
                        "구버전 파이프라인, 유지보수 전용"
                ),
                new BoolRangeDocument(
                        "svc-search-premium",
                        "search",
                        260.0,
                        4.9,
                        true,
                        Instant.parse("2024-03-01T00:00:00Z"),
                        "고가용성 서치 클러스터와 멀티 AZ 지원"
                ),
                new BoolRangeDocument(
                        "svc-search-archived",
                        "search",
                        120.0,
                        4.2,
                        false,
                        Instant.parse("2022-11-01T00:00:00Z"),
                        "테스트 환경 전용, 장애 발생 가능"
                )
        );

        for (final BoolRangeDocument document : documents) {
            final IndexRequest<BoolRangeDocument> request = new IndexRequest.Builder<BoolRangeDocument>()
                    .index(INDEX_NAME)
                    .id(document.id())
                    .document(document)
                    .refresh(Refresh.True)
                    .build();
            try {
                client.index(request);
            } catch (final IOException ioException) {
                throw new UncheckedIOException("Failed to index %s".formatted(document.id()), ioException);
            }
        }
    }

    private List<BoolRangeDocument> executeSearch(final Query query) {
        final SearchRequest request = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(query)
                .size(10)
                .build();

        try {
            final SearchResponse<BoolRangeDocument> response = client.search(request, BoolRangeDocument.class);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (final IOException ioException) {
            throw new UncheckedIOException("Search failed on %s".formatted(INDEX_NAME), ioException);
        }
    }
}
