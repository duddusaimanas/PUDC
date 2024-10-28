package com.user.management.config;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.user.management.entity.LocalDocument;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import io.jsonwebtoken.lang.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class OnDemandWebCrawler extends WebCrawler {

    private final WebPageLoader pageLoader;
    private LocalDocument localDocument;

    @Setter
    private String prompt;

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        Pattern filter = Pattern
                .compile(".*(\\.(maps|images|css|js|xml|jpg|png|gif|jpeg|mp3|mp4|gz|bmp|svg|pdf|doc|ppt|zip|rar))$");
        return !filter.matcher(url.getURL()).matches()
                && Arrays.asList(prompt.split("\\s+")).stream().anyMatch(promptBit -> {
                    promptBit = promptBit.replaceAll("[\\W]+", " ").replace("_", " ").replaceAll("\\s+", " ");
                    return url.getURL().toLowerCase().contains(promptBit.toLowerCase());
                });
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();

        logger.info("Visiting url: {}", url);
        localDocument = pageLoader.load(url, new String(page.getContentData()));
    }

    @Override
    public LocalDocument getMyLocalData() {
        return localDocument;
    }
}
