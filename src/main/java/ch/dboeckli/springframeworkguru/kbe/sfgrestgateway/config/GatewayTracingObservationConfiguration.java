package ch.dboeckli.springframeworkguru.kbe.sfgrestgateway.config;

import org.springframework.cloud.gateway.filter.headers.observation.GatewayObservationConvention;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GatewayTracingObservationConfiguration {

    @Bean
    public GatewayObservationConvention gatewayObservationConvention() {
        return new GatewayTracingObservationConvention();
    }

}
