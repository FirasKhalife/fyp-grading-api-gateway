package com.fypgrading.apigateway.controller;

import com.fypgrading.apigateway.service.dto.FallbackResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/adminServiceGradesFallback")
    public ResponseEntity<FallbackResponseDTO> adminServiceGradesFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Admin Service is taking longer than usual to respond with grades or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackResponseDTO);
    }

    @GetMapping("adminServiceAuthenticationFallback")
    public ResponseEntity<FallbackResponseDTO> adminServiceAuthenticationFallback() {

        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Admin Service is taking longer than usual to reach the authentication page or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackResponseDTO);
    }

    @GetMapping("adminServiceReviewersFallback")
    public ResponseEntity<FallbackResponseDTO> adminServieReviewersFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Admin Service is taking longer than usual to reach the reviewer page or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackResponseDTO);
    }

    @GetMapping("adminServiceReviewerGradesFallback")
    public ResponseEntity<FallbackResponseDTO> adminServieReviewerGradesFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Admin Service is taking longer than usual to respond with grades or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackResponseDTO);
    }

    @GetMapping("adminServiceTeamsFallback")
    public ResponseEntity<FallbackResponseDTO> adminServieTeamsFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Admin Service is taking longer than usual to display teams or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackResponseDTO);
    }

    @GetMapping("evaluationServiceCircuitBreaker")
    public ResponseEntity<FallbackResponseDTO> reviewerServiceFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Evaluation Service is taking too long to respond with evaluations or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackResponseDTO);
    }

    @GetMapping("notificationServiceCircuitBreaker")
    public ResponseEntity<FallbackResponseDTO> notificationServiceFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Notification Service is taking too long to respond with notifications or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackResponseDTO);
    }

    @GetMapping("rubricServiceCircuitBreaker")
    public ResponseEntity<FallbackResponseDTO> rubricServiceFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Rubric Service is taking too long to respond with rubrics or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackResponseDTO);
    }

    @GetMapping("defaultCircuitBreaker")
    public ResponseEntity<FallbackResponseDTO> defaultFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Service is taking longer than usual to respond or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackResponseDTO);
    }
}

