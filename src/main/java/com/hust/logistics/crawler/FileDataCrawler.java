package com.hust.logistics.crawler;

import com.hust.logistics.model.SocialPost;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads social posts from CSV file with columns:
 * author, content, timestamp, source
 */
public class FileDataCrawler implements DataCrawler {
    private final Path csvPath;

    public FileDataCrawler(String filePath) {
        this.csvPath = Path.of(filePath);
    }

    @Override
    public List<SocialPost> crawl() {
        List<SocialPost> posts = new ArrayList<>();
        if (!Files.exists(csvPath)) {
            throw new IllegalArgumentException("CSV file not found: " + csvPath);
        }

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                List<String> fields = parseCsvLine(line);
                if (fields.size() < 4) {
                    continue;
                }
                posts.add(new SocialPost(
                        fields.get(0).trim(),
                        fields.get(1).trim(),
                        fields.get(2).trim(),
                        fields.get(3).trim()
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV: " + csvPath, e);
        }
        return posts;
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
