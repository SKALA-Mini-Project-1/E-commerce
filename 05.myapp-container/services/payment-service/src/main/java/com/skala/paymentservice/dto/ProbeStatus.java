package com.skala.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProbeStatus {
    private String liveness;   // "CORRECT", "BROKEN"
    private String readiness;  // "ACCEPTING_TRAFFIC", "REFUSING_TRAFFIC"
}
