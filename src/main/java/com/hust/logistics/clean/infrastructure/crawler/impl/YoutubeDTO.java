package com.hust.logistics.clean.infrastructure.crawler.impl;

import java.util.List;

public class YoutubeDTO {
    public static class YoutubeVideoResponse {
        public List<YoutubeItem> items;
    }

    public static class YoutubeItem {
        public String id;
        public Snippet snippet;
    }

    public static class Snippet {
        public String title;
        public String description;
    }
}
