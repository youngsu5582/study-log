package youngsu.study.opensearchstudy.lesson;

import java.time.Instant;

public record BoolRangeDocument(
        String id,
        String category,
        double price,
        double rating,
        boolean availability,
        Instant releasedAt,
        String description
) {
}
