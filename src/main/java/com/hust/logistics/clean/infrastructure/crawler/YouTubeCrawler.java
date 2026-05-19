package com.hust.logistics.clean.infrastructure.crawler;

import com.hust.logistics.clean.infrastructure.config.AppConfig;

public class YouTubeCrawler extends GenericSocialCrawler {
    public YouTubeCrawler(AppConfig config) {
        super("youtube", config);
    }
}
