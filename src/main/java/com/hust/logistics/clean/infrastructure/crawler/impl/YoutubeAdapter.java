package com.hust.logistics.clean.infrastructure.crawler.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.config.AppConfig;
import com.hust.logistics.clean.infrastructure.config.YoutubeConfig;
import com.hust.logistics.clean.infrastructure.crawler.impl.YoutubeDTO.YoutubeVideoResponse;

@Service
public class YoutubeAdapter implements SocialMediaCrawler {

    private final RestTemplate restTemplate;
    private final YoutubeConfig config; 
    private final AppConfig appConfig;
    private final YoutubeMapper mapper; 
    
    public YoutubeAdapter(RestTemplate restTemplate, YoutubeConfig config, AppConfig appConfig, YoutubeMapper mapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.appConfig = appConfig;
        this.mapper = mapper;
    }

    @Override
    public String platform() {
        return "YOUTUBE";
    }

    @Override
    public List<SocialPost> crawl() {
        return crawlByKeyword(null);
    }

    @Override
    public List<SocialPost> crawlByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            keyword = appConfig.getDefaultKeyword();
            System.out.println("USED DEFAULT KEYWORD: " + keyword);
        } else {
            System.out.println("USED UI KEYWORD: " + keyword);
        }
        
        String startTime = appConfig.getStartTime().toString(); // ISO-8601 format
        String endTime = appConfig.getEndTime().toString();
        
        String url = String.format("%s/search?part=snippet&q=%s&key=%s&type=video&publishedAfter=%s&publishedBefore=%s&maxResults=50",
                config.getBaseUrl(), keyword, config.getApiKey(), startTime, endTime);
        try {
            YoutubeVideoResponse response =
                        restTemplate.getForObject(url, YoutubeVideoResponse.class);

            return mapper.toSocialPosts(response);

        } catch (Exception e) {
            System.err.println("Failed to crawl Youtube posts: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<SocialPost> crawlByHashtag(String hashtag) {
        return crawlByKeyword(hashtag);
    }
}
