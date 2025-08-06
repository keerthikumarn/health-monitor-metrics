package com.health.metrics.service;

import com.health.metrics.dto.SystemHealthMetricsDTO;
import com.health.metrics.response.HealthMetricsSummaryResponse;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Service
@Slf4j
public class SSHService {

    public HealthMetricsSummaryResponse fetchSystemHealthMetrics(String ipAddress, String username, String password) {
        HealthMetricsSummaryResponse response = new HealthMetricsSummaryResponse();
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, ipAddress, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            String cpuCmd = "top -bn1 | grep 'Cpu(s)' | awk '{print $2 + $4}'";
            String loadCmd = "uptime | awk -F'load average:' '{ print $2 }' | cut -d',' -f1";
            String systemServicesCmd = "ps -eo comm --sort=-%mem | head -n 10";
            String k8sServicesCmd = "kubectl get services -n performance -o wide";
            response.setCpuUsage(executeCommand(session, cpuCmd).trim());
            response.setSystemLoad(executeCommand(session, loadCmd).trim());
            response.setSystemServices(executeCommand(session, systemServicesCmd));
            response.setK8sServices(executeCommand(session, k8sServicesCmd));
            session.disconnect();
        } catch (Exception e) {
            log.error("Failed to fetch health for IP: " + ipAddress, e);
        }
        return response;
    }

    private String executeCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        InputStream in = channel.getInputStream();
        channel.connect();
        Scanner s = new Scanner(in).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        channel.disconnect();
        return result;
    }

    private List<String> parseServices(String output) {
        List<String> services = new ArrayList<>();
        Arrays.stream(output.split("\\n")).skip(1).forEach(services::add);
        return services;
    }

    public String executeCommand(String host, String user, String password, String command) throws Exception {
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

    // Scheduled polling every 5 minutes
    /*@Scheduled(fixedRate = 100000) // 1 minute
    public void pollSystemHealth() {
        log.info("Scheduled health check triggered for IP: " + IP_ADDRESS);
        SystemHealthMetricsDTO metricsDTO = fetchSystemHealthMetrics(IP_ADDRESS);
        log.info("CPU: {}%, Load: {}, Services: {}", metricsDTO.getCpuUsage(), metricsDTO.getSystemLoad(), metricsDTO.getRunningServices());
    }*/

}
