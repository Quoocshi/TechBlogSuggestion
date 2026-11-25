package com.example.techblogsuggestion.service.impl;

import com.example.techblogsuggestion.domain.model.Article;
import com.example.techblogsuggestion.domain.model.ArticleStatus;
import com.example.techblogsuggestion.domain.repository.ArticleRepository;
import com.example.techblogsuggestion.infrastructure.TelegramClient;
import com.example.techblogsuggestion.service.TelegramDigestService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramDigestServiceImpl implements TelegramDigestService {
    private static final Logger log = LoggerFactory.getLogger(TelegramDigestService.class);
    private static final int TELEGRAM_MAX_LENGTH = 4000;

    private final ArticleRepository articleRepository;
    private final TelegramClient telegramClient;

    @Override
    public void sendDailyDigest() {
        List<Article> articles = articleRepository.findAllByStatusOrderByRelevanceScoreDesc(ArticleStatus.SUMMARIZED);

        if (articles.isEmpty()) {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String fallbackMessage = "<b>DAILY TECH DIGEST — " + today + "</b>\n\n"
                    + "No new high-signal engineering articles were published across your sources today. Enjoy a quiet morning!";
            sendMessage(fallbackMessage);
            log.info("No summarized articles available to send today.");
            return;
        }
        List<Article> top5 =  articles.stream()
                .filter(a -> a.getSummary() != null && !a.getSummary().isBlank() )
                .limit(5)
                .toList();
        List<Article> quickLinks = articles.stream()
                .filter(a -> !top5.contains(a))
                .toList();
        String fullMessage = buildMessage(top5, quickLinks);
        sendMessage(fullMessage);

        articles.forEach(a -> a.setStatus(ArticleStatus.SENT));
        articleRepository.saveAll(articles);
        log.info("Successfully sent daily digest with {} top articles and {} quick links.", top5.size(), quickLinks.size());
    }

    private String buildMessage(List<Article> top5, List<Article> quickLinks) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        StringBuilder sb = new StringBuilder();

        sb.append("<b>TECH DIGEST — ").append(today).append("</b>\n\n");
        sb.append("<b>TOP ").append(top5.size()).append(" FEATURED ARTICLES</b>\n\n");

        for (int i = 0; i < top5.size(); i++) {
            Article a = top5.get(i);
            sb.append(i + 1).append(". <a href=\"").append(a.getUrl()).append("\">")
                    .append(escapeHtml(a.getTitle())).append("</a>\n");
            sb.append("Score: <b>").append(String.format("%.1f", a.getRelevanceScore())).append("/10</b>");
            if (a.getTags() != null && !a.getTags().isBlank()) {
                sb.append(" | Tags: <code>").append(escapeHtml(a.getTags())).append("</code>");
            }
            sb.append("\n");

            if (a.getScoreReason() != null && !a.getScoreReason().isBlank()) {
                sb.append("💡 <i>").append(escapeHtml(a.getScoreReason())).append("</i>\n");
            }
            sb.append("\n").append(escapeHtml(a.getSummary())).append("\n\n");
        }

        if (!quickLinks.isEmpty()) {
            sb.append("---------------\n");
            sb.append("<b>MORE ARTICLES TODAY (QUICK LINKS)</b>\n");
            for (Article a : quickLinks) {
                sb.append("- <a href=\"").append(a.getUrl()).append("\">")
                        .append(escapeHtml(a.getTitle())).append("</a>\n");
            }
        }

        return sb.toString();
    }

    private void sendMessage(String fullMessage) {
        if(fullMessage.length() < TELEGRAM_MAX_LENGTH){
            telegramClient.sendMessage(fullMessage);
            return;
        }
        String[] lines = fullMessage.split("\n");
        StringBuilder chunk = new StringBuilder();
        for(String line : lines){
            if(chunk.length() + line.length() + 1 > TELEGRAM_MAX_LENGTH){
                telegramClient.sendMessage(chunk.toString());
                chunk.setLength(0);
            }
            chunk.append(line).append("\n");
        }
        if(!chunk.isEmpty()){
            telegramClient.sendMessage(chunk.toString());
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
