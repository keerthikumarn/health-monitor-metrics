package com.health.metrics.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthMetricsSummaryResponse {

    private String cpu;
    private String systemLoad;
    private String systemServices;
    private String k8sServices;

}
