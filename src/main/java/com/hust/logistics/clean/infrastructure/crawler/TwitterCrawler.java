package com.hust.logistics.clean.infrastructure.crawler;

import com.hust.logistics.clean.infrastructure.config.AppConfig;

public class TwitterCrawler extends GenericSocialCrawler {
    public TwitterCrawler(AppConfig config) {
        super("twitter", config);
    }
}
