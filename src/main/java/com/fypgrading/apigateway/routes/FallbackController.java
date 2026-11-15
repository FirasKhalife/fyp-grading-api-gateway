package com.fypgrading.apigateway.routes;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Where an open circuit or a timeout lands, for every method: a 503 problem the SPA can show and retry. */
@RestController
public class FallbackController {

    @RequestMapping("/fallback/{service}")
    public ResponseEntity<ProblemDetail> fallback(@PathVariable String service) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "The demo is starting up, try again in a minute.");
        problem.setType(URI.create("https://assay.dev/problems/service-unavailable"));
        problem.setProperty("code", "service-unavailable");
        problem.setProperty("service", service);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .header("Retry-After", "30").body(problem);
    }
}
