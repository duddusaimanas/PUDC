package com.user.management.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import com.user.management.entity.LocalDocument;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebPageLoader {

    protected LocalDocument load(String url, String pageContent) {
        Document document = Jsoup.parse(pageContent);
        return getContent(url, document);
    }

    public LocalDocument visit(String url) {
        log.info("Visiting url: {}", url);
        Document document = null;

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.warn("Page loader failed. {}", e.getMessage(), e);
            return null;
        }

        return getContent(url, document);
    }

    private LocalDocument getContent(String url, Document document) {
        String title = document.title();
        String content = document.text().replaceAll("[^\\x00-\\x7F]", "").replaceAll("\\s+", " ");

        log.info("Fetching page {} with url: {}", title, url);
        return LocalDocument.builder().title(title).content(content).url(url).build();
    }

    public LocalDocument webSearch(String prompt) {
        String url = "https://www.google.co.in/search?q=" + UriUtils.encodeQueryParam(prompt, StandardCharsets.UTF_8);
        return visit(url);
    }
}
