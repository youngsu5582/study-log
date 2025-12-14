package youngsu.study.opensearchstudy.lesson;

import java.util.List;

public record FullTextDocument(
        String id,
        String title,
        String content,
        List<String> tags
) {
}
