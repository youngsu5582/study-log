package youngsu.study.opensearchstudy.lesson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.GetMappingRequest;
import org.opensearch.client.opensearch.indices.GetMappingResponse;
import org.opensearch.client.opensearch.indices.get_mapping.IndexMappingRecord;
import org.opensearch.client.transport.endpoints.BooleanResponse;
import org.springframework.stereotype.Service;

@Service
public class MappingAndTypesLesson {

    public static final String INDEX_NAME = "lesson-mapping-types";

    private final OpenSearchClient client;

    public MappingAndTypesLesson(final OpenSearchClient client) {
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
            if (!exists.value()) {
                return;
            }
            client.indices().delete(DeleteIndexRequest.of(builder -> builder.index(INDEX_NAME)));
        } catch (final IOException ioException) {
            throw new UncheckedIOException("Failed to delete index %s".formatted(INDEX_NAME), ioException);
        }
    }

    public TypeMapping fetchMappings() {
        try {
            final GetMappingResponse response = client.indices()
                    .getMapping(GetMappingRequest.of(builder -> builder.index(INDEX_NAME)));
            final IndexMappingRecord record = response.get(INDEX_NAME);
            return record != null ? record.mappings() : null;
        } catch (final IOException ioException) {
            throw new UncheckedIOException("Failed to read mappings for %s".formatted(INDEX_NAME), ioException);
        }
    }

    public List<MappingTypedDocument> searchActiveWithLowErrorRate(final double maxErrorRate) {
        final Query query = Query.of(q -> q.bool(bool -> bool
                .must(m -> m.term(t -> t.field("active").value(FieldValue.of(true))))
                .filter(f -> f.range(r -> r.field("errorRate").lte(JsonData.of(maxErrorRate))))));

        try {
            final SearchResponse<MappingTypedDocument> response = client.search(
                    new SearchRequest.Builder()
                            .index(INDEX_NAME)
                            .query(query)
                            .size(10)
                            .build(),
                    MappingTypedDocument.class
            );
            return response.hits().hits().stream()
                    .map(hit -> hit.source())
                    .filter(source -> source != null)
                    .toList();
        } catch (final IOException ioException) {
            throw new UncheckedIOException("Search failed on %s".formatted(INDEX_NAME), ioException);
        }
    }

    private void createIndex() {
        final TypeMapping mapping = new TypeMapping.Builder()
                .properties("id", p -> p.keyword(k -> k))
                .properties("serviceName", p -> p.keyword(k -> k))
                .properties("latencyMs", p -> p.long_(l -> l))
                .properties("errorRate", p -> p.scaledFloat(sf -> sf.scalingFactor(1_000d)))
                .properties("active", p -> p.boolean_(b -> b))
                .properties("releasedAt", p -> p.date(d -> d))
                .properties("description", p -> p.text(t -> t))
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
        final List<MappingTypedDocument> documents = List.of(
                new MappingTypedDocument(
                        "svc-latency",
                        "api-gateway",
                        120L,
                        0.002,
                        true,
                        Instant.parse("2024-01-10T00:00:00Z"),
                        "API gateway service focused on latency and throughput."
                ),
                new MappingTypedDocument(
                        "svc-error",
                        "analytics-worker",
                        340L,
                        0.075,
                        false,
                        Instant.parse("2023-06-01T00:00:00Z"),
                        "Legacy analytics worker service with frequent errors."
                ),
                new MappingTypedDocument(
                        "svc-green",
                        "search-router",
                        80L,
                        0.001,
                        true,
                        Instant.parse("2024-09-15T00:00:00Z"),
                        "Search router service for green/blue deployments and low latency."
                ),
                new MappingTypedDocument(
                        "svc-canary",
                        "canary-metrics",
                        180L,
                        0.015,
                        true,
                        Instant.parse("2023-12-15T00:00:00Z"),
                        "Canary metrics service for experiments and rollout safety."
                )
        );

        for (final MappingTypedDocument document : documents) {
            final IndexRequest<MappingTypedDocument> request = new IndexRequest.Builder<MappingTypedDocument>()
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
}
