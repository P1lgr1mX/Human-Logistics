package com.hust.logistics.clean.infrastructure.preprocess;

import java.util.regex.Pattern;

/**
 * Preprocessor to clean and normalize text before analysis.
 */
public class TextPreprocessor {

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+\\s?");
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#\\S+");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@\\S+");

    public String preprocess(String text) {
        if (text == null) return "";
        
        String processed = text.toLowerCase().trim();
        
        // Remove URLs
        processed = URL_PATTERN.matcher(processed).replaceAll("");
        
        // Remove Mentions
        processed = MENTION_PATTERN.matcher(processed).replaceAll("");
        
        // Normalizing spaces
        processed = processed.replaceAll("\\s+", " ");
        
        return processed;
    }
}
