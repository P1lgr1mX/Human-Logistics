# Humanitarian Logistics Desktop (Skeleton)

This repository now includes a Clean Architecture skeleton for a Java Desktop application (JavaFX + Maven) focused on humanitarian logistics from social media data.

## Clean Architecture layout

```text
src/main/java/com/hust/logistics/clean
├── presentation
│   └── MainAppFx.java
├── application
│   ├── task
│   │   ├── AnalyticsTask.java
│   │   ├── DamageCategoryClassificationTask.java
│   │   ├── ResourcePriorityTask.java
│   │   ├── SeverityAssessmentTask.java
│   │   └── UrgentNeedsDetectionTask.java
│   └── usecase
│       └── RunAnalyticsUseCase.java
├── domain
│   ├── entity
│   │   ├── AnalysisResult.java
│   │   └── SocialPost.java
│   └── gateway
│       ├── AnalysisClient.java
│       └── SocialMediaCrawler.java
└── infrastructure
    ├── client
    │   └── DeepSeekAnalysisClient.java
    ├── config
    │   ├── AppConfig.java
    │   └── ConfigLoader.java
    └── crawler
        ├── CrawlerStrategyRegistry.java
        ├── TwitterCrawler.java
        └── YouTubeCrawler.java
```

## Config file

`src/main/resources/config.json` stores:
- keywords
- apiKeys (including DeepSeek key placeholder)
- damageCategories
- analysis endpoint + timeout settings

## Run

```bash
mvn clean test
```

For JavaFX execution, run `com.hust.logistics.clean.presentation.MainAppFx`.
