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

    private final IngestionService ingestionService;
    private final ArticleCurationService curationService;
    private final TelegramDigestService telegramDigestService;

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Ho_Chi_Minh")
    public void runMorningPipeline() {
        log.info("Starting Morning Tech Digest Pipeline");
        ingestionService.ingestAllActiveSources();
        curationService.curateRawArticles();
        telegramDigestService.sendDailyDigest();
        log.info("Finished Morning Tech Digest Pipeline");
    }
}
