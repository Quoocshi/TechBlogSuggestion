package com.example.techblogsuggestion.service.impl;

import com.example.techblogsuggestion.api.dto.RawArticleItem;
import com.example.techblogsuggestion.domain.model.FeedSource;
import com.example.techblogsuggestion.domain.model.SourceType;
import com.example.techblogsuggestion.service.FeedFetcherStrategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class HtmlFeedFetcherStrategy implements FeedFetcherStrategy {

    private static final DateTimeFormatter UBER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    @Override
    public SourceType supportedType() {
        return SourceType.UBER_HTML;
    }

    @Override
    public List<RawArticleItem> fetch(FeedSource source) throws Exception {
        Document doc = Jsoup.connect(source.getRssUrl())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9")
                .timeout(15000)
                .get();

        Elements cards = doc.select("div[data-testid=newsroom-article-feed-card]");
        List<RawArticleItem> items = new ArrayList<>();

        for (Element card : cards) {
            Element titleLink = card.selectFirst("a[href*=/blog/]:not([aria-hidden=true])");
            if (titleLink == null) {
                continue;
            }

            String url = titleLink.absUrl("href");
            String title = titleLink.text().trim();
            Instant publishedAt = parseUberDate(card);

            items.add(new RawArticleItem(url, title, "", publishedAt));
        }

        return items;
    }

    private Instant parseUberDate(Element card) {
        try {
            for (Element div : card.select("div")) {
                String text = div.ownText().trim();
                if (text.matches("^[A-Z][a-z]+ \\d{1,2}, \\d{4}$")) {
                    LocalDate date = LocalDate.parse(text, UBER_DATE_FORMAT);
                    return date.atStartOfDay(ZoneOffset.UTC).toInstant();
                }
            }
        } catch (Exception ignored) {
        }
        return Instant.now();
    }
}