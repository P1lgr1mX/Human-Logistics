package com.hust.logistics.clean.presentation;

import com.hust.logistics.clean.application.task.*;
import com.hust.logistics.clean.application.usecase.RunAnalyticsUseCase;
import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.gateway.AnalysisClient;
import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.client.GptAnalysisClient;
import com.hust.logistics.clean.infrastructure.config.AppConfig;
import com.hust.logistics.clean.infrastructure.config.ConfigLoader;
import com.hust.logistics.clean.infrastructure.crawler.CrawlerFactory;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * JavaFX Dashboard for Humanitarian Logistics Analysis.
 * Implemented using code only (no FXML) for maximum flexibility.
 */
public class MainAppFx extends Application {

    private AppConfig config;
    private BorderPane root;
    private VBox sidebar;
    private VBox configArea;
    private StackPane displayArea;
    private int selectedTask = 1;

    // UI Components for configuration
    private TextField keywordsField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private CheckBox fbCheck, tiktokCheck, xCheck;

    @Override
    public void start(Stage stage) {
        // Load Configuration
        ConfigLoader configLoader = new ConfigLoader();
        this.config = configLoader.loadFromResource("config.json");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f4f4;");

        // 1. Sidebar
        sidebar = createSidebar();
        root.setLeft(sidebar);

        // 2. Main Content (Top: Config, Center: Display)
        VBox mainContent = new VBox(10);
        mainContent.setPadding(new Insets(15));
        
        configArea = createConfigArea();
        displayArea = new StackPane();
        displayArea.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        VBox.setVgrow(displayArea, Priority.ALWAYS);

        mainContent.getChildren().addAll(configArea, displayArea);
        root.setCenter(mainContent);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Hệ thống Phân tích Cứu trợ Nhân đạo - " + config.getAnalysis().getProvider().toUpperCase());
        stage.setScene(scene);
        stage.show();

        // Initial view
        updateDisplayArea("Chào mừng! Chọn một bài toán bên trái và nhấn 'Run Analysis'.");
    }

    private VBox createSidebar() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setPrefWidth(250);
        vbox.setStyle("-fx-background-color: #2c3e50;");

        Label title = new Label("BÀI TOÁN");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        vbox.getChildren().add(title);

        String[] tasks = {
            "1. Xu hướng tâm lý",
            "2. Đánh giá thiệt hại",
            "3. Nhu cầu khẩn cấp",
            "4. Ưu tiên nguồn lực"
        };

        for (int i = 0; i < tasks.length; i++) {
            int taskId = i + 1;
            Button btn = new Button(tasks[i]);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14;");
            
            btn.setOnAction(e -> {
                selectedTask = taskId;
                // Reset style for all buttons if needed
                updateDisplayArea("Đã chọn: " + tasks[taskId-1]);
            });
            
            vbox.getChildren().add(btn);
        }

        return vbox;
    }

    private VBox createConfigArea() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label label = new Label("Cấu hình tham số");
        label.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox inputs = new HBox(20);
        inputs.setAlignment(Pos.CENTER_LEFT);

        // Keywords
        VBox kwBox = new VBox(5);
        kwBox.getChildren().addAll(new Label("Keywords:"), keywordsField = new TextField());
        keywordsField.setPromptText("Lũ lụt, cứu trợ...");

        // Dates
        VBox startBox = new VBox(5);
        startBox.getChildren().addAll(new Label("Từ ngày:"), startDatePicker = new DatePicker(LocalDate.now().minusDays(7)));
        
        VBox endBox = new VBox(5);
        endBox.getChildren().addAll(new Label("Đến ngày:"), endDatePicker = new DatePicker(LocalDate.now()));

        // Sources
        VBox sourceBox = new VBox(5);
        HBox checks = new HBox(10);
        checks.getChildren().addAll(
            fbCheck = new CheckBox("Facebook"),
            tiktokCheck = new CheckBox("TikTok"),
            xCheck = new CheckBox("X (Twitter)")
        );
        xCheck.setSelected(true);
        sourceBox.getChildren().addAll(new Label("Nguồn dữ liệu:"), checks);

        // Run Button
        Button runBtn = new Button("Run Analysis");
        runBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        runBtn.setPrefHeight(40);
        runBtn.setPrefWidth(120);
        runBtn.setOnAction(e -> handleRunAnalysis());

        inputs.getChildren().addAll(kwBox, startBox, endBox, sourceBox, runBtn);
        vbox.getChildren().addAll(label, inputs);

        return vbox;
    }

    private void handleRunAnalysis() {
        updateDisplayArea("Đang xử lý: Thu thập -> Tiền xử lý -> " + config.getAnalysis().getProvider().toUpperCase() + " API -> Hiển thị...");
        
        // Actually instantiate the client and tasks
        AnalysisClient client = new GptAnalysisClient(config);
        SocialMediaCrawler crawler = new CrawlerFactory().create("twitter", config);
        
        // For demonstration, we'll run the analysis in the UI thread for now
        // In a production app, this should be in a background Task
        try {
            switch (selectedTask) {
                case 1: 
                    // SentimentTrendTask requires a list of posts
                    SentimentTrendTask trendTask = new SentimentTrendTask(client);
                    com.hust.logistics.clean.domain.entity.AnalysisResult result1 = trendTask.execute(crawler.crawl());
                    showSentimentTrend(); 
                    break;
                case 2: 
                    DamageAssessmentTask damageTask = new DamageAssessmentTask(client, config);
                    com.hust.logistics.clean.domain.entity.AnalysisResult result2 = damageTask.execute(crawler.crawl());
                    showDamageAssessment(); 
                    break;
                default: 
                    updateDisplayArea("Kết quả phân tích cho Bài toán " + selectedTask + " (Đang phát triển)"); 
                    break;
            }
        } catch (Exception e) {
            updateDisplayArea("Lỗi khi chạy phân tích: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSentimentTrend() {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Ngày");
        yAxis.setLabel("Tỷ lệ (%)");

        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Xu hướng tâm lý xã hội");

        XYChart.Series<String, Number> positive = new XYChart.Series<>();
        positive.setName("Tích cực");
        XYChart.Series<String, Number> negative = new XYChart.Series<>();
        negative.setName("Tiêu cực");

        // Mock data based on range
        LocalDate start = startDatePicker.getValue();
        for (int i = 0; i < 7; i++) {
            String date = start.plusDays(i).toString();
            positive.getData().add(new XYChart.Data<>(date, 40 + new Random().nextInt(40)));
            negative.getData().add(new XYChart.Data<>(date, 10 + new Random().nextInt(30)));
        }

        lineChart.getData().addAll(positive, negative);
        displayArea.getChildren().clear();
        displayArea.getChildren().add(lineChart);
    }

    private void showDamageAssessment() {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Loại thiệt hại");
        yAxis.setLabel("Tần suất (lượt đề cập)");

        final BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Thống kê thiệt hại thực tế");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tần suất");

        List<String> categories = config.getDamageCategories();
        for (String cat : categories) {
            series.getData().add(new XYChart.Data<>(cat, new Random().nextInt(100)));
        }

        barChart.getData().add(series);
        displayArea.getChildren().clear();
        displayArea.getChildren().add(barChart);
    }

    private void updateDisplayArea(String text) {
        displayArea.getChildren().clear();
        Label label = new Label(text);
        label.setFont(Font.font(16));
        displayArea.getChildren().add(label);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
