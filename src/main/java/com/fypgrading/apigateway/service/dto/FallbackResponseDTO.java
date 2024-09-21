package com.fypgrading.apigateway.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FallbackResponseDTO {

    private String fallbackMessage;

}

