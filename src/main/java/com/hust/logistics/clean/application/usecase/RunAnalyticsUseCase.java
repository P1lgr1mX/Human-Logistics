package com.hust.logistics.clean.application.usecase;

import com.hust.logistics.clean.application.task.AnalyticsTask;
import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;

import java.util.ArrayList;
import java.util.List;

public class RunAnalyticsUseCase {
    private final SocialMediaCrawler crawler;
    private final List<AnalyticsTask> analyticsTasks;

    public RunAnalyticsUseCase(SocialMediaCrawler crawler, List<AnalyticsTask> analyticsTasks) {
        this.crawler = crawler;
        this.analyticsTasks = analyticsTasks;
    }

    public List<AnalysisResult> execute() {
        List<SocialPost> posts = crawler.crawl();
        List<AnalysisResult> results = new ArrayList<>();
        for (AnalyticsTask task : analyticsTasks) {
            results.add(task.execute(posts));
        }
        return results;
    }
}
