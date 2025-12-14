package com.example.opensearch.lesson;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.example.opensearch.support.OpenSearchIntegrationSmokeTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;

import youngsu.study.opensearchstudy.lesson.FullTextDocument;
import youngsu.study.opensearchstudy.lesson.FullTextLesson;

class FullTextLessonTest extends OpenSearchIntegrationSmokeTestSupport {

    @Autowired
    private FullTextLesson fullTextLesson;
    @Autowired
    private OpenSearchClient openSearchClient;

    @BeforeEach
    void setUp() {
        fullTextLesson.prepareSampleIndex();
    }

    @AfterEach
    void tearDown() {
        fullTextLesson.deleteIndexIfExists();
    }

    @Test
    @DisplayName("match 질의로 콘텐츠 본문을 검색한다")
    void matchQueryReturnsRelevantArticle() {
//        final List<FullTextDocument> results = fullTextLesson.searchMatchContent("실시간 검색", 5);
        final Query query = Query.of(q -> q.match(m -> m.field("content")
                .query(FieldValue.of("실시간 검색"))));

        final var request = SearchRequest.of(s -> s.index(FullTextLesson.INDEX_NAME)
                .query(query)
                .size(5));

        final var results = executeSearch(request, FullTextDocument.class);

        // article-observability 의 '함께 검색해' 부분은 '검색해' 로 분리된다.
        // nori 를 쓰고 정밀 작업을 해야 한다.
        assertThat(results)
                .extracting(FullTextDocument::id)
                .contains("article-analytics", "rolling-deployment")
                .doesNotContain("article-observability");
    }

    @Test
    @DisplayName("match_phrase 질의로 문장을 정확히 검색한다")
    void phraseQueryFindsRollingUpgradePlaybook() throws IOException {
//        final List<FullTextDocument> results = fullTextLesson.searchPhraseInContent("롤링 업그레이드를", 5);
        final Query query = Query.of(q -> q.
                matchPhrase(mp -> mp.field("content")
                        .query("롤링 업그레이드를")));

        final SearchRequest request = SearchRequest.of(s -> s
                .index(FullTextLesson.INDEX_NAME)
                .query(query)
                .size(5));

        final var results = executeSearch(request, FullTextDocument.class);

        assertThat(results)
                .extracting(FullTextDocument::id)
                .containsExactly("article-operations")
                .doesNotContain("rolling-deployment");
    }

    @Test
    @DisplayName("multi_match로 제목과 본문을 동시에 탐색한다")
    void multiMatchAcrossFields() {
//        final List<FullTextDocument> results = fullTextLesson.searchMultiField("Observability OpenSearch", 5);
        final Query query = Query.of(q -> q.multiMatch(mm -> mm.fields("title", "content")
                .query("Observability OpenSearch")));

        final SearchRequest request = SearchRequest.of(
                s -> s.index(FullTextLesson.INDEX_NAME)
                        .query(query)
                        .size(5)
        );
        final var results = executeSearch(request, FullTextDocument.class);

        // article-draft 의 요소는 'OpenSearch의' 로 분리된다.
        assertThat(results)
                .extracting(FullTextDocument::id)
                .contains("article-analytics", "article-observability")
                .doesNotContain("article-draft");
    }

    @Test
    @DisplayName("keyword 로 검색한다")
    void searchKeywords() {
        // keyword 타입은 토큰화를 하지 않고 전체 문자열을 그대로 비교한다.
        final Query query = Query.of(q -> q.term(t -> t.field("tags").value(FieldValue.of("fulltext"))));

        final var request = SearchRequest.of(s -> s
                .index(FullTextLesson.INDEX_NAME)
                .query(query)
                .size(5));

        final var results = executeSearch(request, FullTextDocument.class);

        assertThat(results)
                .extracting(FullTextDocument::id)
                .containsExactly("article-analytics", "article-operations")
                .doesNotContain("article-draft");
    }

    @Test
    @DisplayName("highlights 를 통해 적용된 토큰을 식별할 수 있다.")
    void highlights() throws IOException {
        final Query query = Query.of(q -> q.match(m -> m.field("content")
                .query(FieldValue.of("실시간"))));

        final var request = SearchRequest.of(s -> s
                .index(FullTextLesson.INDEX_NAME)
                .query(query)
                .highlight(h -> h
                        .fields("content", f -> f.preTags("<em>").postTags("</em>")))
                .size(5));

        final var responses = openSearchClient.search(request, FullTextDocument.class);
        final var highlights = responses.hits().hits()
                .stream().map(Hit::highlight).map(Map::values).flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .toList();

        highlights.forEach(
                line -> assertThat(line).contains("<em>실시간</em>")
        );
    }
}
