package com.example.apigateway.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
public class ServiceAddrConfig {

    public final Map<String, String> addressesMap;

    public ServiceAddrConfig(
            @Value("${app.services.address.admin}") String adminAddr,
            @Value("${app.services.address.evaluation}") String evaluationAddr,
            @Value("${app.services.address.rubrics}") String rubricsAddr,
            @Value("${app.services.address.notification}") String notificationAddr
    ) {
        addressesMap = Map.of(
                "admin", adminAddr,
                "evaluation", evaluationAddr,
                "rubricsAddr", rubricsAddr,
                "notification", notificationAddr
        );
    }
}

