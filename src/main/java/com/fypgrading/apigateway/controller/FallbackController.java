package com.fypgrading.apigateway.controller;

import com.fypgrading.apigateway.service.dto.MessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

import static com.fypgrading.apigateway.constant.Constant.FALLBACK_MESSAGE;

@RestController
@RequestMapping("/fallbacks")
public class FallbackController {

    private static final Function<String, MessageDTO> fallbackMessage = serviceName ->
        new MessageDTO(FALLBACK_MESSAGE.formatted(serviceName));

    @GetMapping("/admin-service")
    public ResponseEntity<MessageDTO> adminServiceFallback() {
        MessageDTO messageDTO = fallbackMessage.apply("Admin Service");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(messageDTO);
    }

    @GetMapping("/evaluation-service")
    public ResponseEntity<MessageDTO> reviewerServiceFallback() {
        MessageDTO messageDTO = fallbackMessage.apply("Evaluation Service");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(messageDTO);
    }

    @GetMapping("notification-service")
    public ResponseEntity<MessageDTO> notificationServiceFallback() {
        MessageDTO messageDTO = fallbackMessage.apply("Notification Service");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(messageDTO);
    }

    @GetMapping("/rubric-service")
    public ResponseEntity<MessageDTO> rubricServiceFallback() {
        MessageDTO messageDTO = fallbackMessage.apply("Rubric Service");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(messageDTO);
    }

    @GetMapping("/registry-server")
    public ResponseEntity<MessageDTO> registryServerFallback() {
        MessageDTO messageDTO = fallbackMessage.apply("Registry Server");
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(messageDTO);
    }

    @GetMapping("/config-server")
    public ResponseEntity<MessageDTO> configServerFallback() {
        MessageDTO messageDTO = fallbackMessage.apply("Configuration Server");
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(messageDTO);
    }

    @GetMapping("/default")
    public ResponseEntity<MessageDTO> defaultFallback() {
        MessageDTO messageDTO = fallbackMessage.apply("Service");
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(messageDTO);
    }
}

