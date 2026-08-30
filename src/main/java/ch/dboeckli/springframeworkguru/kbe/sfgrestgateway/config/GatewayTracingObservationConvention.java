package ch.dboeckli.springframeworkguru.kbe.sfgrestgateway.config;

import io.micrometer.common.KeyValues;

import java.net.URI;

import org.springframework.cloud.gateway.filter.headers.observation.DefaultGatewayObservationConvention;
import org.springframework.cloud.gateway.filter.headers.observation.GatewayContext;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;

public class GatewayTracingObservationConvention extends DefaultGatewayObservationConvention {

    @Override
    public KeyValues getHighCardinalityKeyValues(GatewayContext context) {
        KeyValues keyValues = super.getHighCardinalityKeyValues(context);
        Route route = context.getServerWebExchange().getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route != null && route.getUri() != null && route.getUri().getHost() != null) {
            URI uri = route.getUri();
            int port = uri.getPort() != -1 ? uri.getPort() : 80;
            keyValues = keyValues.and("server.address", uri.getHost())
                .and("server.port", String.valueOf(port))
                .and("service.target.name", uri.getHost() + ":" + port);
        }
        return keyValues;
    }

}
