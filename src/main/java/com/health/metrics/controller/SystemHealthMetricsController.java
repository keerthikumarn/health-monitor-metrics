package com.health.metrics.controller;

import com.health.metrics.response.HealthMetricsSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class SystemHealthMetricsController {

    @GetMapping("/cpu")
    public String getCpuUtilization(@RequestParam String host, @RequestParam String user, @RequestParam String password) throws Exception {
        return executeCommand(host, user, password, "top -bn1 | grep 'Cpu(s)'");
    }

    @GetMapping("/system-load")
    public String getSystemLoad(@RequestParam String host, @RequestParam String user, @RequestParam String password) throws Exception {
        return executeCommand(host, user, password, "uptime");
    }

    @GetMapping("/services")
    public String getRunningServices(@RequestParam String host, @RequestParam String user, @RequestParam String password) throws Exception {
        return executeCommand(host, user, password, "systemctl list-units --type=service --state=running");
    }

    @GetMapping("/k8s-services")
    public String getKubernetesServices(@RequestParam String host, @RequestParam String user, @RequestParam String password) throws Exception {
        return executeCommand(host, user, password, "kubectl get services -n performance -o wide");
    }

    @GetMapping("/summary")
    public HealthMetricsSummaryResponse getHealthSummary(@RequestParam String host, @RequestParam String user, @RequestParam String password)
                throws Exception {
        HealthMetricsSummaryResponse summaryResponse = new HealthMetricsSummaryResponse();
        summaryResponse.setCpu(executeCommand(host, user, password, "top -bn1 | grep 'Cpu(s)'"));
        summaryResponse.setSystemLoad(executeCommand(host, user, password, "uptime"));
        summaryResponse.setSystemServices(executeCommand(host, user, password, "systemctl list-units --type=service --state=running"));
        summaryResponse.setK8sServices(executeCommand(host, user, password, "kubectl get services -n performance -o wide"));
        return summaryResponse;
    }

    private String executeCommand(String host, String user, String password, String command) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        var channel = session.openChannel("exec");
        ((com.jcraft.jsch.ChannelExec) channel).setCommand(command);
        java.io.InputStream in = channel.getInputStream();
        channel.connect();
        StringBuilder output = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            output.append((char) c);
        }
        channel.disconnect();
        session.disconnect();
        return output.toString();
    }

}
