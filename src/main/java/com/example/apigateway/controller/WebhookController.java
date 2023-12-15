package com.example.apigateway.controller;

import com.example.apigateway.config.ServiceAddrConfig;
import com.example.apigateway.service.dto.WebhookDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ServiceAddrConfig addressesConfig;

    public WebhookController(ServiceAddrConfig addressesConfig) {
        this.addressesConfig = addressesConfig;
    }

    @PostMapping("/{service}")
    public ResponseEntity<Void> forwardWebhook(@PathVariable String service, @RequestBody WebhookDTO webhookDTO) {
        String serviceAddress = addressesConfig.getAddressesMap().get(service);
        if (webhookDTO.getRepository().get("repo_name") == null) {
            logger.warn("Webhook Body is invalid!");
            return ResponseEntity.badRequest().build();
        }

        restTemplate.postForObject(serviceAddress + ":5000/webhook", webhookDTO, Void.class);
        return ResponseEntity.ok().build();
    }

}
