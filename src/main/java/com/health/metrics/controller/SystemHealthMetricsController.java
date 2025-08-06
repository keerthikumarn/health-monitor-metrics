package com.health.metrics.controller;

import com.health.metrics.response.HealthMetricsSummaryResponse;
import com.health.metrics.service.SSHService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/health")
public class SystemHealthMetricsController {

    @Autowired
    private SSHService sshService;

    @GetMapping("/cpu")
    public String getCpuUtilization(@RequestParam String host, @RequestParam String user, @RequestParam String password) throws Exception {
        return sshService.executeCommand(host, user, password, "top -bn1 | grep 'Cpu(s)'");
    }

    @GetMapping("/system-load")
    public String getSystemLoad(@RequestParam String host, @RequestParam String user, @RequestParam String password) throws Exception {
        return sshService.executeCommand(host, user, password, "uptime");
    }

    @GetMapping("/services")
    public String getRunningServices(@RequestParam String host, @RequestParam String user, @RequestParam String password) throws Exception {
        return sshService.executeCommand(host, user, password, "systemctl list-units --type=service --state=running");
    }

    @GetMapping("/k8s-services")
    public String getKubernetesServices(@RequestParam String host, @RequestParam String user, @RequestParam String password) throws Exception {
        return sshService.executeCommand(host, user, password, "kubectl get services -n performance -o wide");
    }

    @GetMapping("/summary")
    public HealthMetricsSummaryResponse getHealthSummary(
            @RequestParam String host,
            @RequestParam String username,
            @RequestParam String password) throws Exception {
        log.info("Inside getHealthSummary method with host: {}, username: {}", host, username);
        return sshService.fetchSystemHealthMetrics(host, username, password);
    }
}
