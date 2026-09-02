package ch.dboeckli.springframeworkguru.kbe.sfgrestgateway.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.WebHandler;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLoggingWebFilterTest {

    private static final String LOGGER_NAME = "ch.dboeckli.springframeworkguru.kbe.sfgrestgateway.config.RequestLoggingWebFilter";

    private Logger logger;

    private ListAppender<ILoggingEvent> appender;

    private RequestLoggingWebFilter filter;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
        logger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        filter = new RequestLoggingWebFilter();
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        logger.setLevel(Level.INFO);
    }

    @Test
    void logsRequestAndResponseWhenDebugEnabled() {
        WebTestClient client = client();

        client.get().uri("/api/v1/beer").header("X-Test", "abc").exchange().expectStatus().isOk();

        List<String> messages = messages();
        assertThat(messages).anyMatch(msg -> msg.contains("Before request [GET /api/v1/beer]"))
            .anyMatch(msg -> msg.contains("X-Test=abc"))
            .anyMatch(msg -> msg.contains("After request [GET /api/v1/beer] status="));
    }

    @Test
    void logsPayloadWhenDebugEnabled() {
        WebTestClient client = client();

        client.post()
            .uri("/api/v1/beer")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"name\":\"test\"}")
            .exchange()
            .expectStatus()
            .isOk();

        assertThat(messages()).anyMatch(msg -> msg.contains("Payload: {\"name\":\"test\"}"));
    }

    @Test
    void truncatesPayloadAboveLimit() {
        WebTestClient client = client();

        String largePayload = "a".repeat(12000);
        client.post()
            .uri("/api/v1/beer")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(largePayload)
            .exchange()
            .expectStatus()
            .isOk();

        String payloadLog = messages().stream().filter(msg -> msg.startsWith("Payload: ")).findFirst().orElseThrow();
        assertThat(payloadLog).hasSize("Payload: ".length() + 10000);
    }

    @Test
    void doesNotLogWhenDebugDisabled() {
        logger.setLevel(Level.INFO);
        WebTestClient client = client();

        client.get().uri("/api/v1/beer").exchange().expectStatus().isOk();

        assertThat(messages()).isEmpty();
    }

    private WebTestClient client() {
        WebHandler okHandler = exchange -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getRequest().getBody().doOnNext(DataBufferUtils::release).then();
        };
        return WebTestClient.bindToWebHandler(okHandler).webFilter(filter).build();
    }

    private List<String> messages() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

}
