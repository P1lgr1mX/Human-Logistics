package com.hust.logistics.clean.infrastructure.crawler.impl;

import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.infrastructure.crawler.impl.YoutubeDTO.YoutubeVideoResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class YoutubeMapper {
    public List<SocialPost> toSocialPosts(YoutubeVideoResponse response) {
        if (response == null || response.items == null || response.items.isEmpty()) {
            return Collections.emptyList();
        } 
        
        return response.items.stream()
                .map(item -> new SocialPost(
                        item.id != null ? item.id.videoId : "unknown", 
                        "YOUTUBE", 
                        item.snippet.description, 
                        Instant.now(), 
                        item.snippet.title
                ))
                .collect(Collectors.toList());
    }
}
