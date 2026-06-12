package com.hust.logistics.clean.presentation.gui;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import com.hust.logistics.clean.application.service.LogisticsService;
import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.infrastructure.config.AppConfig;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

@Component
public class MainView {

    private final LogisticsService logisticsService;
    private final AppConfig config;

    @FXML private TextField keywordsField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private StackPane displayArea;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    @FXML private Label placeholderLabel;
    @FXML private Button btnTask1;
    @FXML private Button btnTask2;
    @FXML private Button btnTask3;
    @FXML private Button btnTask4;

    private int selectedTaskId = 1;

    public MainView(LogisticsService logisticsService, AppConfig config) {
        this.logisticsService = logisticsService;
        this.config = config;
    }

    public void show(Stage stage) {
        System.out.println("DEBUG: DANG KHOI CHAY GIAO DIEN FXML MOI...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_view.fxml"));
            loader.setControllerFactory(aClass -> this);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1100, 750);
            
            // Load CSS
            String css = getClass().getResource("/style.css") != null ? getClass().getResource("/style.css").toExternalForm() : "";
            if (!css.isEmpty()) scene.getStylesheets().add(css);

            stage.setTitle("NEW FXML INTERFACE - AI Dashboard");
            stage.setScene(scene);
            stage.show();

            // Set default dates
            startDatePicker.setValue(LocalDate.now().minusDays(14));
            endDatePicker.setValue(LocalDate.now());
            
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể tải giao diện: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onTaskSelected(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String text = btn.getText();
        
        // Reset style cho tất cả các nút (đơn giản hóa)
        System.out.println("USER_ACTION: CLICKED SIDEBAR BUTTON: " + text);

        if (text.contains("1")) selectedTaskId = 1;
        else if (text.contains("2")) selectedTaskId = 2;
        else if (text.contains("3")) selectedTaskId = 3;
        else if (text.contains("4")) selectedTaskId = 4;

        // Bỏ trạng thái selected của tất cả các nút
        btnTask1.getStyleClass().remove("selected-menu");
        btnTask2.getStyleClass().remove("selected-menu");
        btnTask3.getStyleClass().remove("selected-menu");
        btnTask4.getStyleClass().remove("selected-menu");

        // Nút vừa click được chọn
        Button clickedButton = (Button) event.getSource();

        if (!clickedButton.getStyleClass().contains("selected-menu")) {
            clickedButton.getStyleClass().add("selected-menu");
        }
        
        updateStatus("Đã chọn Nhiệm vụ " + selectedTaskId + ": " + getTaskName(selectedTaskId));
    }

    @FXML
    private void handleRunAnalysis() {
        System.out.println("USER_ACTION: CLICKED RUN ANALYSIS - TASK ID: " + selectedTaskId);
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            updateStatus("Vui lòng chọn ngày bắt đầu và ngày kết thúc.");
            return;
        }

        String keywords = keywordsField.getText();
        System.out.println("UI RAW KEYWORD = [" + keywords + "]"); // check keyword

        String start = startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant().toString();
        String end = endDatePicker.getValue().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toString();

        progressIndicator.setVisible(true);
        placeholderLabel.setVisible(false);

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
            placeholderLabel.setVisible(true);
            Throwable ex = task.getException();
            updateStatus("Lỗi: " + (ex != null ? ex.getMessage() : "Không xác định"));
            if (ex != null) ex.printStackTrace();
        });

        new Thread(task).start();
    }

    @FXML
    private void clearKeyword() {
        keywordsField.clear();
    }

    private void showResult(AnalysisResult result) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setAlignment(Pos.TOP_CENTER);

        // Tiêu đề kết quả
        Label title = new Label("BÁO CÁO PHÂN TÍCH HỆ THỐNG");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2c3e50"));

        Label subTitle = new Label("Nhiệm vụ: " + getTaskName(selectedTaskId));
        subTitle.setFont(Font.font("System", FontPosture.ITALIC, 14));
        subTitle.setTextFill(Color.GRAY);

        container.getChildren().addAll(title, subTitle, new Separator());

        if (selectedTaskId == 1) {
            // Hiển thị BIỂU ĐỒ CỘT cho Task 1 (Sentiment)
            container.getChildren().add(createSentimentChart(result));
        } else if (selectedTaskId == 2) {
            // Hiển thị BIỂU ĐỒ CỘT cho Task 2 (Damage Assessment)
            container.getChildren().add(createDamageChart(result));
        } else if (selectedTaskId == 3) {
            // Hiển thị BIỂU ĐỒ TRÒN cho Task 3 (Satisfaction)
            container.getChildren().add(createSatisfactionChart(result));
        } else if (selectedTaskId == 4) {
            // Hiển thị BIỂU ĐỒ ĐƯỜNG cho Task 4 (Trend)
            container.getChildren().add(createTrendChart(result));
        } else {
            // Hiển thị DẠNG THẺ (Card) cho các Task khác
            VBox card = new VBox(15);
            card.setStyle("-fx-background-color: #fcfcfc; -fx-border-color: #eee; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 20;");
            
            Label summaryLabel = new Label("Tóm tắt phân tích:");
            summaryLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            
            // Làm sạch văn bản: Xóa dòng mã dữ liệu
            String cleanSummary = result.getSummary()
                    .replaceAll("DATA_POINTS:.*", "")
                    .replaceAll("SATISFACTION_DATA:.*", "")
                    .replaceAll("TREND_DATA:.*", "")
                    .trim();
            TextArea summaryText = new TextArea(cleanSummary);
            summaryText.setEditable(false);
            summaryText.setWrapText(true);
            summaryText.setPrefRowCount(10);
            summaryText.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 10;");
            
            card.getChildren().addAll(summaryLabel, summaryText);
            VBox.setVgrow(card, Priority.ALWAYS);
            container.getChildren().add(card);
        }

        // Footer với độ tin cậy
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Label scoreLabel = new Label(String.format("Độ tin cậy AI: %.2f%%", result.getScore() * 100));
        scoreLabel.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5 15; -fx-background-radius: 15;");
        footer.getChildren().add(scoreLabel);

        container.getChildren().add(footer);

        displayArea.getChildren().clear();
        displayArea.getChildren().addAll(container, progressIndicator);
        progressIndicator.toFront();
    }

    private String getTaskName(int id) {
        return switch (id) {
            case 1 -> "Xu hướng tâm lý cộng đồng";
            case 2 -> "Đánh giá mức độ thiệt hại";
            case 3 -> "Mức độ hài lòng cứu trợ";
            case 4 -> "Theo dõi xu hướng khẩn cấp";
            default -> "Phân tích tổng quát";
        };
    }

    private javafx.scene.chart.LineChart<String, Number> createTrendChart(AnalysisResult result) {
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        xAxis.setLabel("Ngày/Tháng");
        
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis(0, 100, 10);
        yAxis.setLabel("Mức độ quan tâm");

        javafx.scene.chart.LineChart<String, Number> lineChart = new javafx.scene.chart.LineChart<>(xAxis, yAxis);
        lineChart.setTitle("XU HƯỚNG CỨU TRỢ THỰC TẾ");
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        
        // Bóc tách dữ liệu động: dd/MM=val
        String summary = result.getSummary();
        System.out.println("DEBUG TREND - SUMMARY: " + summary); // debug
        
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d{2}/\\d{2})=(\\d+)");
        java.util.regex.Matcher m = p.matcher(summary);
        
        int count = 0;
        while (m.find()) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(m.group(1), Integer.parseInt(m.group(2))));
            count++;
        }
        System.out.println("DEBUG TREND - FOUND POINTS: " + count); // debug

        lineChart.getData().clear();
        lineChart.getData().add(series);
        return lineChart;
    }

    private javafx.scene.chart.PieChart createSatisfactionChart(AnalysisResult result) {
        System.out.println("DEBUG SATISFACTION - SUMMARY: " + result.getSummary());
        javafx.scene.chart.PieChart pieChart = new javafx.scene.chart.PieChart();
        pieChart.setTitle("MỨC ĐỘ HÀI LÒNG CỨU TRỢ TỔNG THỂ");
        pieChart.setLabelsVisible(true);
        pieChart.setAnimated(false);

        int happy = extractValue(result.getSummary(), "HAPPY=(\\d+)");
        int neutral = extractValue(result.getSummary(), "NEUTRAL=(\\d+)");
        int unhappy = extractValue(result.getSummary(), "UNHAPPY=(\\d+)");

        pieChart.getData().clear();
        pieChart.getData().add(new javafx.scene.chart.PieChart.Data("Hài lòng (" + happy + "%)", happy));
        pieChart.getData().add(new javafx.scene.chart.PieChart.Data("Trung lập (" + neutral + "%)", neutral));
        pieChart.getData().add(new javafx.scene.chart.PieChart.Data("Không hài lòng (" + unhappy + "%)", unhappy));

        // Màu sắc: Xanh dương (Hài lòng), Xám (Trung lập), Đỏ (Không hài lòng)
        Platform.runLater(() -> {
            pieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #3498db;");
            pieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #95a5a6;");
            pieChart.getData().get(2).getNode().setStyle("-fx-pie-color: #e74c3c;");
        });

        return pieChart;
    }

    private javafx.scene.chart.BarChart<String, Number> createDamageChart(AnalysisResult result) {
        System.out.println("DEBUG DAMAGE - SUMMARY: " + result.getSummary());
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        yAxis.setLabel("Số vụ ghi nhận");

        javafx.scene.chart.BarChart<String, Number> barChart = new javafx.scene.chart.BarChart<>(xAxis, yAxis);
        barChart.setTitle("THỐNG KÊ CHI TIẾT THIỆT HẠI");
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        
        // Danh sách các loại thiệt hại cần bóc tách
        String[] categories = {
            "Nhà cửa bị hư hỏng", 
            "Gián đoạn kinh tế sản xuất", 
            "Tài sản cá nhân bị mất", 
            "Cơ sở hạ tầng bị hư hỏng", 
            "Người bị ảnh hưởng", 
            "Khác"
        };

        for (String cat : categories) {
            int val = extractValue(result.getSummary(), cat + ": (\\d+)");
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(cat, val));
        }

        barChart.getData().clear();
        barChart.getData().add(series);
        
        // Tùy chỉnh màu sắc (màu cam chuyên nghiệp cho cứu hộ)
        Platform.runLater(() -> {
            for (javafx.scene.chart.XYChart.Data<String, Number> data : series.getData()) {
                data.getNode().setStyle("-fx-bar-fill: #e67e22;"); 
            }
        });

        return barChart;
    }

    private javafx.scene.chart.BarChart<String, Number> createSentimentChart(AnalysisResult result) {
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();

        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis(0, 100, 10);
        yAxis.setLabel("Phần trăm (%)");

        javafx.scene.chart.BarChart<String, Number> barChart = new javafx.scene.chart.BarChart<>(xAxis, yAxis);
        barChart.setTitle("PHÂN TÍCH XU HƯỚNG TÂM LÝ");

        barChart.setLegendVisible(false);
        barChart.setAnimated(false);

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        
        // Trích xuất dữ liệu từ chuỗi Summary bằng Regex mới (DATA_POINTS: POS=x, NEU=y, NEG=z)
        System.out.println("DEBUG - SUMMARY RECEIVED: " + result.getSummary());
        
        int pos = extractValue(result.getSummary(), "POS=(\\d+)");
        int neu = extractValue(result.getSummary(), "NEU=(\\d+)");
        int neg = extractValue(result.getSummary(), "NEG=(\\d+)");
        
        System.out.println(String.format("DEBUG - PARSED VALUES: POS=%d, NEU=%d, NEG=%d", pos, neu, neg));

        series.getData().add(new javafx.scene.chart.XYChart.Data<>("Tích cực", pos));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("Trung lập", neu));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("Tiêu cực", neg));

        barChart.getData().clear();
        barChart.getData().add(series);
        
        Platform.runLater(() -> {
            for (javafx.scene.chart.XYChart.Data<String, Number> data : series.getData()) {
                if (data.getXValue().equals("Tích cực")) data.getNode().setStyle("-fx-bar-fill: #2ecc71;");
                if (data.getXValue().equals("Trung lập")) data.getNode().setStyle("-fx-bar-fill: #f1c40f;");
                if (data.getXValue().equals("Tiêu cực")) data.getNode().setStyle("-fx-bar-fill: #e74c3c;");
            }
        });

        return barChart;
    }

    private int extractValue(String text, String patternStr) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 33; // Mặc định nếu không tìm thấy
    }

    private void updateStatus(String text) {
        Platform.runLater(() -> {
            statusLabel.setText(text);
            System.out.println(text);
        });
    }
}
