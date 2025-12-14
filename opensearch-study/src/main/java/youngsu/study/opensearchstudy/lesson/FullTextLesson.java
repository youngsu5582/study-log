package youngsu.study.opensearchstudy.lesson;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.FieldValue;
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
public class FullTextLesson {

    public static final String INDEX_NAME = "lesson-full-text";

    private final OpenSearchClient client;

    public FullTextLesson(final OpenSearchClient client) {
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

    public List<FullTextDocument> searchMatchContent(final String query, final int size) {
        final Query match = Query.of(q -> q.match(m -> m.field("content").query(FieldValue.of(query))));
        return executeSearch(match, size);
    }

    public List<FullTextDocument> searchPhraseInContent(final String phrase, final int size) {
        final Query phraseQuery = Query.of(q -> q.matchPhrase(mp -> mp.field("content").query(phrase)));
        return executeSearch(phraseQuery, size);
    }

    public List<FullTextDocument> searchMultiField(final String query, final int size) {
        final Query multiMatch = Query.of(q -> q.multiMatch(mm -> mm
                .fields("title", "content")
                .query(query)));
        return executeSearch(multiMatch, size);
    }

    private void createIndex() {
        final TypeMapping mapping = new TypeMapping.Builder()
                .properties("id", p -> p.keyword(k -> k))
                .properties("title", p -> p.text(t -> t
                        .analyzer("standard")
                        .fields("keyword", f -> f.keyword(k -> k.ignoreAbove(256)))))
                .properties("content", p -> p.text(t -> t.analyzer("standard")))
                .properties("tags", p -> p.keyword(k -> k))
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
        final List<FullTextDocument> samples = List.of(
                new FullTextDocument(
                        "article-analytics",
                        "실시간 로그 분석 파이프라인",
                        "OpenSearch 로그를 수집하고 실시간 검색을 구현하는 방법을 정리한다.",
                        List.of("fulltext", "log", "realtime")
                ),
                new FullTextDocument(
                        "article-draft",
                        "로그 초안",
                        "OpenSearch의 장점에 대한 초안",
                        List.of("opensearch", "draft", "text")
                ),
                new FullTextDocument(
                        "article-operations",
                        "클러스터 운영 가이드",
                        "운영자가 장애 대응과 롤링 업그레이드를 수행하는 실전 절차 모음",
                        List.of("fulltext", "operations", "upgrade")
                ),
                new FullTextDocument(
                        "article-observability",
                        "Observability with OpenSearch",
                        "traces와 metrics를 함께 검색해 장애 원인을 추적하는 전략",
                        List.of("observability", "distributed-tracing")
                ),
                new FullTextDocument(
                        "rolling-deployment",
                        "Rolling Deployment",
                        "롤링 배포를 통해 실시간 점진적 교체 방법",
                        List.of("deployment", "rolling")
                )
        );

        for (final FullTextDocument document : samples) {
            final IndexRequest<FullTextDocument> request = new IndexRequest.Builder<FullTextDocument>()
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

    private List<FullTextDocument> executeSearch(final Query query, final int size) {
        final SearchRequest request = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(query)
                .size(size)
                .build();

        try {
            final SearchResponse<FullTextDocument> response = client.search(request, FullTextDocument.class);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (final IOException ioException) {
            throw new UncheckedIOException("Search failed on %s".formatted(INDEX_NAME), ioException);
        }
    }
}
