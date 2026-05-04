package com.ttjobs.backend.scheduler;

import com.ttjobs.backend.service.JobAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobAlertScheduler {

    @Autowired
    private JobAlertService jobAlertService;

    @Value("${ttjobs.job-alert.enabled:true}")
    private boolean jobAlertEnabled;

    @Scheduled(cron = "${ttjobs.job-alert.cron:0 0 8 * * *}")
    public void sendDailyJobAlerts() {
        if (!jobAlertEnabled) {
            return;
        }
        jobAlertService.processAllAlerts();
    }
}
