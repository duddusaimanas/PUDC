package com.user.management.config;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import com.user.management.entity.LocalSearchRequest;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import io.jsonwebtoken.lang.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

        private final OnDemandWebCrawler onDemandWebCrawler;

        @Value("${local.crawl.directory}")
        private String localCrawlDirectory;

        public Set<String> crawl(LocalSearchRequest search) throws Exception {
                CrawlConfig crawlConfig = new CrawlConfig();
                crawlConfig.setCrawlStorageFolder(localCrawlDirectory);
                crawlConfig.setCleanupDelaySeconds(5);
                crawlConfig.setMaxDepthOfCrawling(3);
                crawlConfig.setMaxPagesToFetch(50);

                PageFetcher pageFetcher = new PageFetcher(crawlConfig);

                RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
                robotstxtConfig.setEnabled(false);
                RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

                CrawlController controller = new CrawlController(crawlConfig, pageFetcher, robotstxtServer);
                search.getPromptVariations().forEach(prompt -> controller.addSeed(
                                "https://www.google.co.in/search?q="
                                                + UriUtils.encodeQueryParam(prompt, StandardCharsets.UTF_8)));

                final String consolidatedPrompt = Arrays
                                .asList(search.getPromptVariations().stream().collect(Collectors.joining(" "))
                                                .split("\\s+"))
                                .stream()
                                .collect(Collectors.toSet()).stream().collect(Collectors.joining(" "));

                onDemandWebCrawler.setPrompt(StringUtils.hasText(search.getDomainName()) ? search.getDomainName()
                                : consolidatedPrompt);
                controller.start(() -> onDemandWebCrawler, 5);

                return controller.getCrawlersLocalData().stream()
                                .map(localDocument -> localDocument.toString())
                                .filter(StringUtils::hasText)
                                .collect(Collectors.toSet());
        }
}
