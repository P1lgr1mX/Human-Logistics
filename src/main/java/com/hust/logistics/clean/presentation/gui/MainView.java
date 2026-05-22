package com.hust.logistics.clean.presentation.gui;

import com.hust.logistics.clean.application.service.LogisticsService;
import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.infrastructure.config.AppConfig;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class MainView {

    private final LogisticsService logisticsService;
    private final AppConfig config;

    private StackPane displayArea;
    private TextField keywordsField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ProgressIndicator progressIndicator;
    private Label statusLabel;
    private int selectedTaskId = 1;

    public MainView(LogisticsService logisticsService, AppConfig config) {
        this.logisticsService = logisticsService;
        this.config = config;
    }

    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // 1. Sidebar
        root.setLeft(createSidebar());

        // 2. Main Content
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));
        
        mainContent.getChildren().addAll(createConfigBar(), createResultContainer(), createStatusBar());
        root.setCenter(mainContent);

        Scene scene = new Scene(root, 1100, 750);
        // Load CSS for modern styling
        String css = getClass().getResource("/style.css") != null ? getClass().getResource("/style.css").toExternalForm() : "";
        if (!css.isEmpty()) scene.getStylesheets().add(css);

        stage.setTitle("Hệ thống Phân tích Logistics Nhân đạo - AI Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(30, 15, 30, 15));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #2c3e50;");

        Label title = new Label("LOGISTICS AI");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        
        VBox menuItems = new VBox(10);
        String[] tasks = {
            "1. Xu hướng tâm lý",
            "2. Đánh giá thiệt hại",
            "3. Hài lòng cứu trợ",
            "4. Theo dõi xu hướng"
        };

        for (int i = 0; i < tasks.length; i++) {
            int id = i + 1;
            Button btn = new Button(tasks[i]);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.getStyleClass().add("sidebar-btn");
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 14; -fx-padding: 10; -fx-cursor: hand;");
            
            btn.setOnAction(e -> {
                selectedTaskId = id;
                updateStatus("Đã chọn: " + tasks[id-1]);
            });
            
            menuItems.getChildren().add(btn);
        }

        sidebar.getChildren().addAll(title, new Separator(), menuItems);
        return sidebar;
    }

    private HBox createConfigBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(15));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Keywords
        VBox kwBox = new VBox(5);
        kwBox.getChildren().addAll(new Label("Từ khóa:"), keywordsField = new TextField());
        keywordsField.setPromptText("bão Yagi, lũ lụt...");
        keywordsField.setPrefWidth(200);

        // Dates
        VBox startBox = new VBox(5);
        startBox.getChildren().addAll(new Label("Từ ngày:"), startDatePicker = new DatePicker(LocalDate.now().minusDays(14)));
        
        VBox endBox = new VBox(5);
        endBox.getChildren().addAll(new Label("Đến ngày:"), endDatePicker = new DatePicker(LocalDate.now()));

        // Run Button
        Button runBtn = new Button("CHẠY PHÂN TÍCH");
        runBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        runBtn.setOnAction(e -> handleRunAnalysis());

        bar.getChildren().addAll(kwBox, startBox, endBox, runBtn);
        return bar;
    }

    private StackPane createResultContainer() {
        displayArea = new StackPane();
        displayArea.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        VBox.setVgrow(displayArea, Priority.ALWAYS);

        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(50, 50);

        Label placeholder = new Label("Kết quả sẽ hiển thị tại đây");
        placeholder.setTextFill(Color.GRAY);
        placeholder.setFont(Font.font(16));

        displayArea.getChildren().addAll(placeholder, progressIndicator);
        return displayArea;
    }

    private void handleRunAnalysis() {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            updateStatus("Vui lòng chọn ngày bắt đầu và ngày kết thúc.");
            return;
        }

        String keywords = keywordsField.getText();
        String start = startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant().toString();
        String end = endDatePicker.getValue().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toString();

        progressIndicator.setVisible(true);
        if (!displayArea.getChildren().isEmpty()) {
            displayArea.getChildren().get(0).setVisible(false);
        }

        Task<AnalysisResult> task = new Task<>() {
            @Override
            protected AnalysisResult call() {
                return logisticsService.runTask(selectedTaskId, keywords, start, end);
            }
        };

        task.setOnSucceeded(e -> {
            progressIndicator.setVisible(false);
            showResult(task.getValue());
        });

        task.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            Throwable ex = task.getException();
            updateStatus("Lỗi: " + (ex != null ? ex.getMessage() : "Không xác định"));
            if (ex != null) ex.printStackTrace();
        });

        new Thread(task).start();
    }

    private void showResult(AnalysisResult result) {
        VBox resultBox = new VBox(15);
        resultBox.setPadding(new Insets(25));
        
        Label title = new Label("KẾT QUẢ PHÂN TÍCH: TASK " + selectedTaskId);
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#2c3e50"));

        TextArea summaryText = new TextArea(result.getSummary());
        summaryText.setEditable(false);
        summaryText.setWrapText(true);
        summaryText.setFont(Font.font("Segoe UI", 15));
        summaryText.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-radius: 5;");
        VBox.setVgrow(summaryText, Priority.ALWAYS);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Label scoreLabel = new Label(String.format("Độ tin cậy AI: %.2f%%", result.getScore() * 100));
        scoreLabel.setFont(Font.font("System", FontPosture.ITALIC, 13));
        footer.getChildren().add(scoreLabel);

        resultBox.getChildren().addAll(title, new Separator(), summaryText, footer);
        
        displayArea.getChildren().clear();
        displayArea.getChildren().addAll(resultBox, progressIndicator);
    }

    private HBox createStatusBar() {
        statusLabel = new Label("Sẵn sàng");
        statusLabel.setTextFill(Color.GRAY);
        statusLabel.setFont(Font.font(12));
        HBox bar = new HBox(statusLabel);
        bar.setPadding(new Insets(5, 0, 0, 0));
        return bar;
    }

    private void updateStatus(String text) {
        Platform.runLater(() -> {
            statusLabel.setText(text);
            System.out.println(text);
        });
    }
}
