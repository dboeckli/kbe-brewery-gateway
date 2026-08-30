package ch.dboeckli.springframeworkguru.kbe.sfgrestgateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RequestLoggingWebFilter implements WebFilter {

    private static final int MAX_PAYLOAD_LENGTH = 10000;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!log.isDebugEnabled()) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        log.debug("Before request [{} {}] headers=[{}]", request.getMethod(), request.getURI(),
                formatHeaders(request.getHeaders()));
        return chain.filter(exchange.mutate().request(createRequestDecorator(exchange)).build()).doFinally(signal -> {
            ServerHttpResponse response = exchange.getResponse();
            log.debug("After request [{} {}] status=[{}]", request.getMethod(), request.getURI(),
                    response.getStatusCode());
        });
    }

    private ServerHttpRequest createRequestDecorator(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        return new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return DataBufferUtils.join(super.getBody()).flux().map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    log.debug("Payload: {}", truncate(new String(bytes, StandardCharsets.UTF_8)));
                    return exchange.getResponse().bufferFactory().wrap(bytes);
                });
            }
        };
    }

    private String formatHeaders(HttpHeaders headers) {
        return headers.toSingleValueMap()
            .entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", "));
    }

    private String truncate(String value) {
        return value.length() > MAX_PAYLOAD_LENGTH ? value.substring(0, MAX_PAYLOAD_LENGTH) : value;
    }

}
