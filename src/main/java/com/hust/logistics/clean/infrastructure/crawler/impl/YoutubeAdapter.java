package com.hust.logistics.clean.infrastructure.crawler.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.config.YoutubeConfig;
import com.hust.logistics.clean.infrastructure.crawler.impl.YoutubeDTO.YoutubeVideoResponse;

@Service
@Profile("prod")
public class YoutubeAdapter implements SocialMediaCrawler {

    private final RestTemplate restTemplate;
    private final YoutubeConfig config; 
    private final YoutubeMapper mapper; 
    
    public YoutubeAdapter(RestTemplate restTemplate, YoutubeConfig config, YoutubeMapper mapper) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.mapper = mapper;
    }

    @Override
    public String platform() {
        return "YOUTUBE";
    }

    @Override
    public List<SocialPost> crawl() {
        return crawlByKeyword("humanitarian logistics");
    }

    @Override
    public List<SocialPost> crawlByKeyword(String keyword) {
        String url = String.format("%s/search?part=snippet&q=%s&key=%s",
                config.getBaseUrl(), keyword, config.getApiKey());
        try {
            YoutubeVideoResponse response = restTemplate.getForObject(url, YoutubeVideoResponse.class);
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
