package com.hust.logistics.clean.application.usecase;

import com.hust.logistics.clean.application.task.AnalyticsTask;
import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class RunAnalyticsUseCaseTest {

    @Test
    void executesAllTasks() {
        SocialMediaCrawler crawler = new StubCrawler();

        RunAnalyticsUseCase useCase = new RunAnalyticsUseCase(
                crawler,
                List.of(
                        new NamedTask("a", 0.1),
                        new NamedTask("b", 0.2)
                )
        );

        List<AnalysisResult> results = useCase.execute();
        Assertions.assertEquals(2, results.size());
    }

    private static class StubCrawler implements SocialMediaCrawler {
        @Override
        public String platform() {
            return "stub";
        }

        @Override
        public List<SocialPost> crawl() {
            return List.of(new SocialPost("1", "stub", "need water", Instant.now(), "tester"));
        }

        @Override
        public List<SocialPost> crawlByKeyword(String keyword) {
            return crawl();
        }

        @Override
        public List<SocialPost> crawlByHashtag(String hashtag) {
            return crawl();
        }
    }

    private static class NamedTask implements AnalyticsTask {
        private final String name;
        private final double score;

        private NamedTask(String name, double score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public AnalysisResult execute(List<SocialPost> posts) {
            return new AnalysisResult(name, "ok", score);
        }
    }
}
