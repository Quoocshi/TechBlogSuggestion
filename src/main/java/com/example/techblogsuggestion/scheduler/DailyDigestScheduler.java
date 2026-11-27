package com.example.techblogsuggestion.scheduler;

import com.example.techblogsuggestion.service.ArticleCurationService;
import com.example.techblogsuggestion.service.IngestionService;
import com.example.techblogsuggestion.service.TelegramDigestService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyDigestScheduler {
    private static final Logger log = LoggerFactory.getLogger(DailyDigestScheduler.class);
    private static final String TIMEZONE = "Asia/Ho_Chi_Minh";

    private final IngestionService ingestionService;
    private final ArticleCurationService curationService;
    private final TelegramDigestService telegramDigestService;

    @Scheduled(cron = "0 0 8,12,18,22,2,6 * * *", zone = TIMEZONE)
    public void runIngestionJob() {
        log.info("Starting scheduled RSS/HTML ingestion job");
        ingestionService.ingestAllActiveSources();
        log.info("Finished scheduled RSS/HTML ingestion job");
    }

    @Scheduled(cron = "0 30 7 * * *", zone = TIMEZONE)
    public void runMorningCurationAndDigestJob() {
        log.info("Starting Morning Curation & Telegram Digest Job");
        curationService.curateRawArticles();
        telegramDigestService.sendDailyDigest();
        log.info("Finished Morning Curation & Telegram Digest Job");
    }
}