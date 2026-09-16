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
public class MakautSyllabusScraper {

    public static final String DEFAULT_SYLLABUS_URL = "https://makautexam.net/syllabus.html";

    @Getter
    @Builder
    public static class ScrapedResourceLink {
        private final String title;
        private final String stream;
        private final Integer semester;
        private final String paperCode;
        private final String downloadUrl;
    }

    /**
     * Scrapes MAKAUT syllabus portal page for course links.
     */
    public List<ScrapedResourceLink> scrapeSyllabusLinks(String targetUrl) throws IOException {
        String url = (targetUrl != null && !targetUrl.isBlank()) ? targetUrl : DEFAULT_SYLLABUS_URL;
        log.info("Scraping MAKAUT syllabus links from: {}", url);

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();

        return parseDocument(doc);
    }

    /**
     * Parses an HTML document string (useful for offline testing and static DOM parsing).
     */
    public List<ScrapedResourceLink> parseDocument(Document doc) {
        List<ScrapedResourceLink> links = new ArrayList<>();
        Elements rows = doc.select("table tr, div.syllabus-row, a[href$=.pdf]");

        for (Element el : rows) {
            if (el.tagName().equals("a")) {
                String href = el.absUrl("href");
                String title = el.text().trim();
                if (href.endsWith(".pdf")) {
                    links.add(ScrapedResourceLink.builder()
                            .title(title)
                            .downloadUrl(href)
                            .stream(inferStream(title))
                            .semester(inferSemester(title))
                            .paperCode(inferPaperCode(title))
                            .build());
                }
            } else {
                Elements anchor = el.select("a[href$=.pdf]");
                if (!anchor.isEmpty()) {
                    String href = anchor.first().absUrl("href");
                    String title = el.text().trim();
                    links.add(ScrapedResourceLink.builder()
                            .title(title)
                            .downloadUrl(href)
                            .stream(inferStream(title))
                            .semester(inferSemester(title))
                            .paperCode(inferPaperCode(title))
                            .build());
                }
            }
        }
        log.info("Found {} syllabus resource links", links.size());
        return links;
    }

    private String inferStream(String text) {
        String upper = text.toUpperCase();
        if (upper.contains("CSE") || upper.contains("COMPUTER SCIENCE")) return "CSE";
        if (upper.contains("IT") || upper.contains("INFORMATION TECH")) return "IT";
        if (upper.contains("ECE") || upper.contains("ELECTRONICS")) return "ECE";
        if (upper.contains("EE") || upper.contains("ELECTRICAL")) return "EE";
        if (upper.contains("ME") || upper.contains("MECHANICAL")) return "ME";
        if (upper.contains("CE") || upper.contains("CIVIL")) return "CE";
        return "GENERAL";
    }

    private Integer inferSemester(String text) {
        String upper = text.toUpperCase();
        for (int i = 1; i <= 8; i++) {
            if (upper.contains(i + "TH SEM") || upper.contains("SEM " + i) || upper.contains("SEMESTER " + i)) {
                return i;
            }
        }
        return null;
    }

    private String inferPaperCode(String text) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("([A-Z]{2,4}-[A-Z]{2,4}\\d{3}[A-Z]?)").matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}
