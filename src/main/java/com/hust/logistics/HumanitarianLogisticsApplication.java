package com.hust.logistics;

import com.hust.logistics.clean.presentation.gui.StageInitializer;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class HumanitarianLogisticsApplication {
    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }
}
