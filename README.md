# Humanitarian Logistics Desktop (Skeleton)

This repository now includes a Clean Architecture skeleton for a Java Desktop application (JavaFX (frontend) + Maven + Spring-boot (backend) focused on humanitarian logistics from social media data.

## Clean Architecture layout

```text
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── hust
│   │   │           ├── logistics
│   │   │           │   ├── clean
│   │   │           │   │   ├── application
│   │   │           │   │   │   ├── service
│   │   │           │   │   │   │   └── LogisticsService.java
│   │   │           │   │   │   ├── task
│   │   │           │   │   │   │   ├── AnalyticsTask.java
│   │   │           │   │   │   │   ├── DamageAssessmentTask.java
│   │   │           │   │   │   │   ├── GenericAnalyticsTask.java
│   │   │           │   │   │   │   ├── ReliefAnalysisTask.java
│   │   │           │   │   │   │   └── SentimentTrendTask.java
│   │   │           │   │   │   └── usecase
│   │   │           │   │   ├── domain
│   │   │           │   │   │   ├── entity
│   │   │           │   │   │   │   ├── AnalysisResult.java
│   │   │           │   │   │   │   └── SocialPost.java
│   │   │           │   │   │   └── gateway
│   │   │           │   │   │       ├── AnalysisClient.java
│   │   │           │   │   │       └── SocialMediaCrawler.java
│   │   │           │   │   ├── infrastructure
│   │   │           │   │   │   ├── client
│   │   │           │   │   │   │   └── GptAnalysisClient.java
│   │   │           │   │   │   ├── config
│   │   │           │   │   │   │   └── AppConfig.java
│   │   │           │   │   │   ├── crawler
│   │   │           │   │   │   │   ├── CrawlerFactory.java
│   │   │           │   │   │   │   ├── GenericSocialCrawler.java
│   │   │           │   │   │   │   └── MockCrawler.java
│   │   │           │   │   │   └── preprocess
│   │   │           │   │   │       └── TextPreprocessor.java
│   │   │           │   │   └── presentation
│   │   │           │   │       ├── gui
│   │   │           │   │       │   ├── MainView.java
│   │   │           │   │       │   └── StageInitializer.java
│   │   │           │   │       └── rest
│   │   │           │   │           └── LogisticsController.java
│   │   │           │   ├── HumanitarianLogisticsApplication.java
│   │   │           │   └── JavaFxApplication.java
│   │   │           └── logistics-ai
│   │   │               ├── main.py
│   │   │               └── requiments.txt
│   │   └── resources
│   │       ├── application.yml
│   │       └── style.css
│   └── test
│       └── java
│           └── com
│               └── hust
│                   └── logistics
│                       └── clean
│                           ├── application
│                           │   └── usecase
│                           │       └── RunAnalyticsUseCaseTest.java
│                           └── infrastructure
│                               └── crawler
│                                   └── CrawlerModuleTest.java

```
## Run

```bash
mvn clean test
```

```bash
mvn spring-boot:run 
```
Goodluck and have a nice day! 
For JavaFX execution, run `com.hust.logistics.clean.presentation.MainAppFx`.
