package com.hust.logistics.crawler;

import com.hust.logistics.model.SocialPost;

import java.util.List;

/**
 * Contract for collecting raw logistics data
 * from external sources.
 */
public interface DataCrawler {
    List<SocialPost> crawl();
}
