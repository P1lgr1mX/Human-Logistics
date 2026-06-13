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

        try {
            return response.items.stream()
                    .map(item -> {
                        String id = (item != null && item.id != null) ? item.id.videoId : "unknown";
                        String content = "";
                        String author = "Unknown Title";
                        
                        if (item != null && item.snippet != null) {
                            content = item.snippet.description != null ? item.snippet.description : "";
                            author = item.snippet.title != null ? item.snippet.title : "Unknown Title";
                        }
                        
                        return new SocialPost(id, "YOUTUBE", content, Instant.now(), author);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error mapping YouTube response: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
