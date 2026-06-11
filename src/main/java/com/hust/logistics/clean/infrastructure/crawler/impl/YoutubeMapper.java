package com.hust.logistics.clean.infrastructure.crawler.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.infrastructure.crawler.impl.YoutubeDTO.YoutubeVideoResponse;

@Component
public class YoutubeMapper {
    public List<SocialPost> toSocialPosts(YoutubeVideoResponse response) {
        if (response == null || response.items == null || response.items.isEmpty()) {
            return Collections.emptyList();
        } 
        
        return response.items.stream()
                .map(item -> new SocialPost(
                        item != null && item.id != null 
                                ? item.id.videoId 
                                : "unknown",
                        
                        "YOUTUBE",

                        item != null
                                && item.snippet != null
                                && item.snippet.description != null
                                ? item.snippet.description
                                : "",

                        Instant.now(),

                        item != null
                                && item.snippet != null
                                && item.snippet.title != null
                                ? item.snippet.title
                                : "Unknown Title"
                ))
                .collect(Collectors.toList());
    }
}
