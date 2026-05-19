package com.hust.logistics.clean.domain.entity;

public class AnalysisResult {
    private final String taskName;
    private final String summary;
    private final double score;

    public AnalysisResult(String taskName, String summary, double score) {
        this.taskName = taskName;
        this.summary = summary;
        this.score = score;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getSummary() {
        return summary;
    }

    public double getScore() {
        return score;
    }
}
