package com.example.opensearch.temp;

import com.example.opensearch.support.OpenSearchIntegrationSmokeTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.core.InfoResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class OpenSearchPingTests extends OpenSearchIntegrationSmokeTestSupport {

    @Test
    @DisplayName("외부 OpenSearch 클러스터에 핑/정보 조회가 가능하다")
    void pingExternalCluster() throws IOException {
        assumeTrue(EXTERNAL_URI != null,
                "EXTERNAL_OPENSEARCH_URI 환경 변수가 설정되지 않아 외부 클러스터 검증을 건너뜁니다.");

        final boolean pinged = client.ping().value();
        final InfoResponse info = client.info();

        assertThat(pinged).isTrue();
        assertThat(info.version()).isNotNull();
    }
}
