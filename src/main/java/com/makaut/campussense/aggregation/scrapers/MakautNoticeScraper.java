package com.makaut.campussense.aggregation.scrapers;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MakautNoticeScraper {

    public static final String DEFAULT_NOTICE_URL = "https://makautexam.net/announcement.html";

    @Getter
    @Builder
    public static class ScrapedNotice {
        private final String title;
        private final String date;
        private final String link;
        private final boolean isMarNotice;
        private final boolean isMoocsNotice;
    }

    /**
     * Scrapes examination & academic notices from MAKAUT website.
     */
    public List<ScrapedNotice> scrapeNotices(String targetUrl) throws IOException {
        String url = (targetUrl != null && !targetUrl.isBlank()) ? targetUrl : DEFAULT_NOTICE_URL;
        log.info("Scraping MAKAUT notices from: {}", url);

        List<ScrapedNotice> notices = new ArrayList<>();
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();

        return parseNoticeDocument(doc);
    }

    public List<ScrapedNotice> parseNoticeDocument(Document doc) {
        List<ScrapedNotice> notices = new ArrayList<>();
        Elements items = doc.select("ul.notice-list li, div.notice-item, table.notice-table tr");

        for (Element el : items) {
            Element linkEl = el.selectFirst("a");
            if (linkEl != null) {
                String title = linkEl.text().trim();
                String link = linkEl.absUrl("href");
                String date = el.select(".date, span.badge").text().trim();
                String upper = title.toUpperCase();

                notices.add(ScrapedNotice.builder()
                        .title(title)
                        .date(date)
                        .link(link)
                        .isMarNotice(upper.contains("MAR") || upper.contains("MANDATORY ADDITIONAL"))
                        .isMoocsNotice(upper.contains("MOOC") || upper.contains("SWAYAM") || upper.contains("NPTEL"))
                        .build());
            }
        }
        return notices;
    }
}
