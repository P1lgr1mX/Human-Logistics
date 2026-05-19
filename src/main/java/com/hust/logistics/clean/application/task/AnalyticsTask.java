package com.hust.logistics.clean.application.task;

import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;

import java.util.List;

public interface AnalyticsTask {
    String name();

    AnalysisResult execute(List<SocialPost> posts);
}
