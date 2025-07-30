package com.health.metrics.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SystemHealthMetricsDTO {

    private double cpuUsage;
    private double systemLoad;
    private List<String> runningServices;

}
