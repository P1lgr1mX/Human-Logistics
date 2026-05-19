package com.hust.logistics.clean.domain.gateway;

import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;

import java.util.List;

public interface AnalysisClient {
    AnalysisResult analyze(String taskName, List<SocialPost> posts);
}
