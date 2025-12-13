package youngsu.study.opensearchstudy.config;

import java.net.URI;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.opensearch.client.transport.OpenSearchTransport;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
@EnableConfigurationProperties(OpenSearchProperties.class)
public class OpenSearchConfig {

    @Bean(destroyMethod = "close")
    public OpenSearchTransport openSearchTransport(final OpenSearchProperties properties) {
        final HttpHost[] hosts = properties.getUris().stream()
                .map(this::toHttpHost)
                .toArray(HttpHost[]::new);

        final ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        return ApacheHttpClient5TransportBuilder.builder(hosts)
                .setMapper(new JacksonJsonpMapper(objectMapper))
                .setHttpClientConfigCallback(clientBuilder -> clientBuilder
                        .setDefaultRequestConfig(RequestConfig.custom()
                                .setConnectTimeout(Timeout.ofMilliseconds(properties.getConnectionTimeout().toMillis()))
                                .setResponseTimeout(Timeout.ofMilliseconds(properties.getSocketTimeout().toMillis()))
                                .build()))
                .build();
    }

    @Bean
    public OpenSearchClient openSearchClient(final OpenSearchTransport transport) {
        return new OpenSearchClient(transport);
    }

    private HttpHost toHttpHost(final String rawUri) {
        final URI uri = URI.create(rawUri);
        final String scheme = uri.getScheme() != null ? uri.getScheme() : "http";
        final String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("opensearch.client.uris must contain valid host information");
        }
        final int port = uri.getPort() >= 0 ? uri.getPort() : 9200;
        return new HttpHost(scheme, host, port);
    }
}
