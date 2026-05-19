package com.hust.logistics.clean.domain.gateway;

import com.hust.logistics.clean.domain.entity.SocialPost;

import java.util.List;

public interface SocialMediaCrawler {
    String platform();

    List<SocialPost> crawl();

    List<SocialPost> crawlByKeyword(String keyword);

    List<SocialPost> crawlByHashtag(String hashtag);
}
