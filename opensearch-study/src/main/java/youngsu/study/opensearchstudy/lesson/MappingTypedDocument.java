package youngsu.study.opensearchstudy.lesson;

import java.time.Instant;

public record MappingTypedDocument(
        String id,
        String serviceName,
        long latencyMs,
        double errorRate,
        boolean active,
        Instant releasedAt,
        String description
) {
}
