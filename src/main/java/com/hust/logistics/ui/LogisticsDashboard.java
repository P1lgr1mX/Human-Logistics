package com.hust.logistics.ui;

import com.hust.logistics.analyzer.PythonSentimentAnalyzer;
import com.hust.logistics.analyzer.SentimentAnalyzer;
import com.hust.logistics.crawler.DataCrawler;
import com.hust.logistics.crawler.FileDataCrawler;
import com.hust.logistics.model.AnalysisResult;
import com.hust.logistics.model.SocialPost;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Minimal Swing dashboard starter.
 */
public class LogisticsDashboard extends JFrame {
    private final JTextArea outputArea = new JTextArea();
    private final JButton selectFileButton = new JButton("Select CSV File");
    private final JButton refreshButton = new JButton("Refresh Data");
    private final JLabel selectedFileLabel = new JLabel("No CSV selected");
    private final JPanel chartContainer = new JPanel(new BorderLayout());
    private final JPanel barChartContainer = new JPanel(new BorderLayout());
    private final JSplitPane chartSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private String selectedCsvPath;
    private List<AnalyzedPost> lastAnalysisResults = new ArrayList<>();

    public LogisticsDashboard() {
        setTitle("Humanitarian Logistics Dashboard");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        outputArea.setEditable(false);
        setLayout(new BorderLayout());
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        chartSplitPane.setLeftComponent(chartContainer);
        chartSplitPane.setRightComponent(barChartContainer);
        chartSplitPane.setResizeWeight(0.5);
        chartSplitPane.setOneTouchExpandable(true);
        add(chartSplitPane, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(selectFileButton);
        buttonPanel.add(refreshButton);
        bottomPanel.add(selectedFileLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Keep both charts responsive when the window is resized.
        chartContainer.setMinimumSize(new java.awt.Dimension(250, 220));
        barChartContainer.setMinimumSize(new java.awt.Dimension(250, 220));
        chartSplitPane.setPreferredSize(new java.awt.Dimension(900, 320));
    }

    public JButton getSelectFileButton() {
        return selectFileButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public void setSelectedFilePath(String filePath) {
        selectedCsvPath = filePath;
        selectedFileLabel.setText(filePath);
    }

    public void appendLine(String line) {
        outputArea.append(line + System.lineSeparator());
    }

    public void clearOutput() {
        outputArea.setText("");
    }

    public void runAnalysis(String keyword, String sourceType) {
        DataCrawler crawler = resolveCrawler(sourceType);
        SentimentAnalyzer analyzer = new PythonSentimentAnalyzer();
        List<AnalyzedPost> analyzedPosts = new ArrayList<>();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        for (SocialPost post : crawler.crawl()) {
            String content = post.getContent() == null ? "" : post.getContent();
            if (!normalizedKeyword.isBlank()
                    && !content.toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                continue;
            }
            AnalysisResult result = analyzer.analyze(content);
            result.setTimestamp(post.getTimestamp());
            analyzedPosts.add(new AnalyzedPost(post, result));
        }

        lastAnalysisResults = analyzedPosts;
        updateChart1();
        updateChart2();
    }

    private DataCrawler resolveCrawler(String sourceType) {
        if (sourceType == null) {
            throw new IllegalArgumentException("sourceType is required");
        }
        String normalizedSource = sourceType.trim().toLowerCase(Locale.ROOT);
        if ("file".equals(normalizedSource) || "csv".equals(normalizedSource)) {
            if (selectedCsvPath == null || selectedCsvPath.isBlank()) {
                throw new IllegalStateException("CSV path is not selected");
            }
            return new FileDataCrawler(selectedCsvPath);
        }
        throw new IllegalArgumentException("Unsupported sourceType: " + sourceType);
    }

    private void updateChart1() {
        List<AnalysisResult> results = new ArrayList<>();
        for (AnalyzedPost item : lastAnalysisResults) {
            results.add(item.getResult());
        }
        updateLineChart(results);
    }

    private void updateChart2() {
        List<AnalysisResult> results = new ArrayList<>();
        for (AnalyzedPost item : lastAnalysisResults) {
            results.add(item.getResult());
        }
        updateBarChart(results);
    }

    public void updateLineChart(List<AnalysisResult> results) {
        Map<LocalDate, int[]> dailyCounts = new TreeMap<>();
        for (AnalysisResult result : results) {
            LocalDate date = parseToDate(result.getTimestamp());
            if (date == null) {
                continue;
            }
            int[] counters = dailyCounts.computeIfAbsent(date, key -> new int[]{0, 0});
            String sentiment = result.getSentiment() == null
                    ? ""
                    : result.getSentiment().trim().toLowerCase(Locale.ROOT);
            if ("tích cực".equals(sentiment)) {
                counters[0]++;
            } else if ("tiêu cực".equals(sentiment)) {
                counters[1]++;
            }
        }

        TimeSeries positiveSeries = new TimeSeries("Tích cực");
        TimeSeries negativeSeries = new TimeSeries("Tiêu cực");
        for (Map.Entry<LocalDate, int[]> entry : dailyCounts.entrySet()) {
            LocalDate date = entry.getKey();
            int[] counters = entry.getValue();
            Day day = new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
            positiveSeries.add(day, counters[0]);
            negativeSeries.add(day, counters[1]);
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(positiveSeries);
        dataset.addSeries(negativeSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Xu hướng cảm xúc theo ngày",
                "Thời gian",
                "Số lượng bài đăng",
                dataset,
                true,
                true,
                false
        );

        chartContainer.removeAll();
        chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    public void updateBarChart(List<AnalysisResult> results) {
        Map<String, Integer> damageCounts = new TreeMap<>();
        for (AnalysisResult result : results) {
            String damageType = result.getDamageType();
            if (damageType == null || damageType.isBlank()) {
                damageType = "Khác";
            }
            damageCounts.put(damageType, damageCounts.getOrDefault(damageType, 0) + 1);
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : damageCounts.entrySet()) {
            dataset.addValue(entry.getValue(), "Số lượng báo cáo", entry.getKey());
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Phân bố báo cáo theo loại thiệt hại",
                "Loại thiệt hại",
                "Số lượng báo cáo",
                dataset
        );

        barChartContainer.removeAll();
        barChartContainer.add(new ChartPanel(barChart), BorderLayout.CENTER);
        barChartContainer.revalidate();
        barChartContainer.repaint();
    }

    private LocalDate parseToDate(String rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp.isBlank()) {
            return null;
        }
        String value = rawTimestamp.trim();
        List<DateTimeFormatter> dateTimeFormats = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
        for (DateTimeFormatter formatter : dateTimeFormats) {
            try {
                return LocalDateTime.parse(value, formatter).toLocalDate();
            } catch (DateTimeParseException ignored) {
                // Try next supported format.
            }
        }
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException ignored) {
            // Try date-only format next.
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static class AnalyzedPost {
        private final SocialPost post;
        private final AnalysisResult result;

        private AnalyzedPost(SocialPost post, AnalysisResult result) {
            this.post = post;
            this.result = result;
        }

        public SocialPost getPost() {
            return post;
        }

        public AnalysisResult getResult() {
            return result;
        }
    }
}
