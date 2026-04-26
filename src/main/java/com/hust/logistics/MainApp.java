package com.hust.logistics;

import com.hust.logistics.analyzer.KeywordSentimentAnalyzer;
import com.hust.logistics.analyzer.SentimentAnalyzer;
import com.hust.logistics.crawler.DataCrawler;
import com.hust.logistics.crawler.FileDataCrawler;
import com.hust.logistics.model.AnalysisResult;
import com.hust.logistics.model.LogisticsRecord;
import com.hust.logistics.model.SocialPost;
import com.hust.logistics.preprocess.TextPreprocessor;
import com.hust.logistics.ui.LogisticsDashboard;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.List;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LogisticsDashboard dashboard = new LogisticsDashboard();
            TextPreprocessor preprocessor = new TextPreprocessor();
            SentimentAnalyzer analyzer = new KeywordSentimentAnalyzer();
            final String[] selectedCsvPath = new String[1];

            dashboard.getSelectFileButton().addActionListener(event -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Choose Social Posts CSV");
                chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
                int result = chooser.showOpenDialog(dashboard);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    selectedCsvPath[0] = file.getAbsolutePath();
                    dashboard.setSelectedFilePath(selectedCsvPath[0]);
                    dashboard.appendLine("Selected CSV: " + selectedCsvPath[0]);
                }
            });

            dashboard.getRefreshButton().addActionListener(event -> {
                if (selectedCsvPath[0] == null || selectedCsvPath[0].isBlank()) {
                    dashboard.appendLine("Please select a CSV file first.");
                    return;
                }

                dashboard.clearOutput();
                dashboard.appendLine("=== Data refresh ===");
                DataCrawler crawler = new FileDataCrawler(selectedCsvPath[0]);
                List<SocialPost> posts = crawler.crawl();

                for (SocialPost post : posts) {
                    String cleanedContent = preprocessor.normalize(post.getContent());
                    AnalysisResult result = analyzer.analyze(cleanedContent);
                    LogisticsRecord record = new LogisticsRecord(cleanedContent, result.getConfidence());
                    dashboard.appendLine("Author: " + post.getAuthor());
                    dashboard.appendLine("Time: " + post.getTimestamp());
                    dashboard.appendLine("Source: " + post.getSource());
                    dashboard.appendLine(record.toString());
                    dashboard.appendLine("Sentiment: " + result.getSentiment());
                    dashboard.appendLine("Damage Type: " + result.getDamageType());
                    dashboard.appendLine("------------------------------");
                }
            });

            dashboard.setVisible(true);
        });
    }
}
