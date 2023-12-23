package com.example.apigateway.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class WebhookDTO {

    @NotNull
    private String callback_url;

    @NotNull
    private Map<String, String> push_data;

    @NotNull
    private Map<String, Object> repository;

}
