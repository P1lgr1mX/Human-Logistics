package com.hust.logistics.clean.presentation.gui;

import com.hust.logistics.JavaFxApplication.StageReadyEvent;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final MainView mainView;

    public StageInitializer(MainView mainView) {
        this.mainView = mainView;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
        mainView.show(stage);
    }
}
