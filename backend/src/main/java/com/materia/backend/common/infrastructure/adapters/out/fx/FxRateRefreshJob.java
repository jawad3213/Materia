package com.materia.backend.common.infrastructure.adapters.out.fx;

import com.materia.backend.common.domain.fx.ReportingFx;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/**
 * Stores today's exchange rates and values committed documents still missing a copy: once at start-up, then every
 * day (app.fx.refresh.cron). It has its own scheduler, independent of the {@code @Scheduled} jobs that
 * SchedulingConfig runs. Disabled with app.fx.refresh.enabled=false (tests).
 */
@Component
@ConditionalOnProperty(name = "app.fx.refresh.enabled", havingValue = "true", matchIfMissing = true)
public class FxRateRefreshJob {

    private static final Logger log = LoggerFactory.getLogger(FxRateRefreshJob.class);

    private final ReportingFx fx;
    private final String cron;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    public FxRateRefreshJob(ReportingFx fx, @Value("${app.fx.refresh.cron:0 30 5 * * *}") String cron) {
        this.fx = fx;
        this.cron = cron;
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("fx-rates-");
        scheduler.initialize();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        scheduler.execute(this::run);
        scheduler.schedule(this::run, new CronTrigger(cron));
        log.info("[FX] Daily rate refresh scheduled ({}), reporting currency {}", cron, fx.reportingCurrency());
    }

    void run() {
        try {
            fx.refreshDailyRates();
            fx.backfill();
        } catch (RuntimeException e) {
            log.warn("[FX] Daily rate refresh failed: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdown();
    }
}
