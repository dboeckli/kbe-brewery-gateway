package ch.dboeckli.springframeworkguru.kbe.sfgrestgateway.config;

import io.micrometer.common.KeyValues;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.headers.observation.GatewayContext;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayTracingObservationConventionTest {

    private final GatewayTracingObservationConvention convention = new GatewayTracingObservationConvention();

    @Test
    void addsRouteServerTagsForUriWithPort() {
        GatewayContext context = contextWithRoute("http://beer-service:8080");

        KeyValues keyValues = convention.getHighCardinalityKeyValues(context);

        assertThat(keyValues.stream())
            .anyMatch(kv -> kv.getKey().equals("server.address") && kv.getValue().equals("beer-service"))
            .anyMatch(kv -> kv.getKey().equals("server.port") && kv.getValue().equals("8080"))
            .anyMatch(kv -> kv.getKey().equals("service.target.name") && kv.getValue().equals("beer-service:8080"));
    }

    @Test
    void defaultsPortTo80WhenRouteUriHasNoPort() {
        GatewayContext context = contextWithRoute("lb://beer-service");

        KeyValues keyValues = convention.getHighCardinalityKeyValues(context);

        assertThat(keyValues.stream())
            .anyMatch(kv -> kv.getKey().equals("server.address") && kv.getValue().equals("beer-service"))
            .anyMatch(kv -> kv.getKey().equals("server.port") && kv.getValue().equals("80"))
            .anyMatch(kv -> kv.getKey().equals("service.target.name") && kv.getValue().equals("beer-service:80"));
    }

    @Test
    void doesNotAddRouteTagsWhenRouteIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange
            .from(MockServerHttpRequest.get("http://localhost/api/v1/beer"));
        GatewayContext context = new GatewayContext(new HttpHeaders(), exchange.getRequest(), exchange);

        KeyValues keyValues = convention.getHighCardinalityKeyValues(context);

        assertThat(keyValues.stream()).noneMatch(kv -> kv.getKey().equals("server.address"))
            .noneMatch(kv -> kv.getKey().equals("server.port"))
            .noneMatch(kv -> kv.getKey().equals("service.target.name"));
    }

    private GatewayContext contextWithRoute(String routeUri) {
        MockServerWebExchange exchange = MockServerWebExchange
            .from(MockServerHttpRequest.get("http://localhost/api/v1/beer"));
        Route route = Route.async().id("beer-service").uri(URI.create(routeUri)).predicate(ex -> true).build();
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);
        return new GatewayContext(new HttpHeaders(), exchange.getRequest(), exchange);
    }

}
