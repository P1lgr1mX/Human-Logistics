package com.hust.logistics.clean.application.usecase;

import com.hust.logistics.clean.application.service.LogisticsService;
import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.AnalysisClient;
import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.config.AppConfig;
import com.hust.logistics.clean.infrastructure.crawler.CrawlerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class RunAnalyticsUseCaseTest {

    @Test
    void executesLogisticsServiceTask() {
        // Use manual stubs instead of Mockito to avoid JDK 25 issues
        AnalysisClient analysisClient = new StubAnalysisClient();
        CrawlerFactory crawlerFactory = new StubCrawlerFactory();
        AppConfig config = new AppConfig();

        LogisticsService service = new LogisticsService(analysisClient, crawlerFactory, config);

        // Execute task 1
        AnalysisResult result = service.runTask(1, "test", null, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1.0, result.getScore());
    }

    private static class StubAnalysisClient implements AnalysisClient {
        @Override
        public AnalysisResult analyze(String taskName, List<SocialPost> posts) {
            return new AnalysisResult("task", "summary", 1.0);
        }
    }

    private static class StubCrawlerFactory extends CrawlerFactory {
        @Override
        public SocialMediaCrawler create(String platform, AppConfig config) {
            return new StubCrawler();
        }
    }

    private static class StubCrawler implements SocialMediaCrawler {
        @Override
        public String platform() { return "stub"; }
        @Override
        public List<SocialPost> crawl() {
            return List.of(new SocialPost("1", "stub", "content", Instant.now(), "author"));
        }
        @Override
        public List<SocialPost> crawlByKeyword(String keyword) { return crawl(); }
        @Override
        public List<SocialPost> crawlByHashtag(String hashtag) { return crawl(); }
    }
}
