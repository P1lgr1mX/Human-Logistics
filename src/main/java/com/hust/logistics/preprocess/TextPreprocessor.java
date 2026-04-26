package com.hust.logistics.preprocess;

/**
 * Performs simple text normalization.
 */
public class TextPreprocessor {

    public String normalize(String input) {
        return input.toLowerCase().trim();
    }
}
