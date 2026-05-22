package com.hust.logistics.clean.presentation.rest;

import com.hust.logistics.clean.application.service.LogisticsService;
import com.hust.logistics.clean.domain.entity.AnalysisResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class LogisticsController {
    private final LogisticsService logisticsService;

    public LogisticsController(LogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    @GetMapping("/run/{taskId}")
    public AnalysisResult runAnalysis(
            @PathVariable int taskId,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return logisticsService.runTask(taskId, keywords, startTime, endTime);
    }
}
