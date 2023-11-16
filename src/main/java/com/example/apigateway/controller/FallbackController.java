package com.example.apigateway.controller;


import com.example.apigateway.service.dto.FallbackResponseDTO;
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
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fallbackResponseDTO);
    }

    @GetMapping("adminServieAuthenticationFallback")
    public ResponseEntity<FallbackResponseDTO> adminServieAuthenticationFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Admin Service is taking longer than usual to reach the authentication page or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fallbackResponseDTO);
    }

    @GetMapping("adminServieReviewersFallback")
    public ResponseEntity<FallbackResponseDTO> adminServieReviewersFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Admin Service is taking longer than usual to reach the reviewer page or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fallbackResponseDTO);
    }

    @GetMapping("adminServieReviewerGradesFallback")
    public ResponseEntity<FallbackResponseDTO> adminServieReviewerGradesFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Admin Service is taking longer than usual to respond with grades or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fallbackResponseDTO);
    }

    @GetMapping("adminServieRubricsFallback")
    public ResponseEntity<FallbackResponseDTO> adminServieRubricsFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Admin Service is taking longer than usual to display rubrics or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fallbackResponseDTO);
    }

    @GetMapping("adminServieTeamsFallback")
    public ResponseEntity<FallbackResponseDTO> adminServieTeamsFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Admin Service is taking longer than usual to display teams or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fallbackResponseDTO);
    }

    @GetMapping("reviewerServiceFallback")
    public ResponseEntity<FallbackResponseDTO> reviewerServiceFallback() {
        FallbackResponseDTO fallbackResponseDTO = new FallbackResponseDTO(
                "Reviewer Service is taking too long to respond with evaluations or is down. Please try again later");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(fallbackResponseDTO);
    }
}

