package com.example.techblogsuggestion.service.impl;

import com.example.techblogsuggestion.service.ArticleContentExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JsoupArticleContentExtractor implements ArticleContentExtractor {

    private static final Logger log = LoggerFactory.getLogger(JsoupArticleContentExtractor.class);
    private static final int MIN_VALID_CONTENT_LENGTH = 300;
    private static final int MAX_CONTENT_LENGTH = 15000;

    @Override
    public String extractCleanText(String url, String existingHtmlSnippet) {
        if (existingHtmlSnippet != null && !existingHtmlSnippet.isBlank()) {
            String cleanedSnippet = cleanHtml(existingHtmlSnippet);
            if (cleanedSnippet.length() >= MIN_VALID_CONTENT_LENGTH) {
                return truncate(cleanedSnippet);
            }
        }

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                    .timeout(15000)
                    .get();

            doc.select("script, style, nav, header, footer, aside, form, iframe, .ad, .advertisement").remove();

            Element mainContent = doc.selectFirst("article");
            if (mainContent == null) {
                mainContent = doc.selectFirst("main");
            }
            if (mainContent == null) {
                mainContent = doc.body();
            }

            String text = mainContent != null ? mainContent.text() : "";
            return truncate(normalizeWhitespace(text));
        } catch (Exception e) {
            log.warn("Could not extract full content from URL [{}]: {}", url, e.getMessage());
            return "";
        }
    }

    private String cleanHtml(String rawHtml) {
        String text = Jsoup.parse(rawHtml).text();
        return normalizeWhitespace(text);
    }

    private String normalizeWhitespace(String input) {
        return input.replaceAll("\\s+", " ").trim();
    }

    private String truncate(String text) {
        if (text.length() <= MAX_CONTENT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_CONTENT_LENGTH);
    }
}