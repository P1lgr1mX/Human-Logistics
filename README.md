# Humanitarian Logistics Desktop (Skeleton)

This repository now includes a Clean Architecture skeleton for a Java Desktop application (JavaFX + Maven) focused on humanitarian logistics from social media data.

## Clean Architecture layout

```text
Human-Logistics )  tree 
.
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── hust
│   │   │           ├── logistics
│   │   │           │   ├── analyzer
│   │   │           │   │   ├── GptAnalysisProvider.java
│   │   │           │   │   ├── KeywordSentimentAnalyzer.java
│   │   │           │   │   ├── PythonSentimentAnalyzer.java
│   │   │           │   │   └── SentimentAnalyzer.java
│   │   │           │   ├── clean
│   │   │           │   │   ├── application
│   │   │           │   │   │   ├── task
│   │   │           │   │   │   │   ├── AnalyticsTask.java
│   │   │           │   │   │   │   ├── DamageAssessmentTask.java
│   │   │           │   │   │   │   ├── GenericAnalyticsTask.java
│   │   │           │   │   │   │   ├── ReliefAnalysisTask.java
│   │   │           │   │   │   │   └── SentimentTrendTask.java
│   │   │           │   │   │   └── usecase
│   │   │           │   │   │       └── RunAnalyticsUseCase.java
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
│   │   │           │   │   │   │   ├── AppConfig.java
│   │   │           │   │   │   │   └── ConfigLoader.java
│   │   │           │   │   │   ├── crawler
│   │   │           │   │   │   │   ├── CrawlerFactory.java
│   │   │           │   │   │   │   ├── CrawlerStrategyRegistry.java
│   │   │           │   │   │   │   ├── GenericSocialCrawler.java
│   │   │           │   │   │   │   ├── MockCrawler.java
│   │   │           │   │   │   │   ├── TwitterCrawler.java
│   │   │           │   │   │   │   └── YouTubeCrawler.java
│   │   │           │   │   │   └── preprocess
│   │   │           │   │   │       └── TextPreprocessor.java
│   │   │           │   │   └── presentation
│   │   │           │   │       └── MainAppFx.java
│   │   │           │   ├── crawler
│   │   │           │   │   ├── DataCrawler.java
│   │   │           │   │   └── FileDataCrawler.java
│   │   │           │   ├── MainApp.java
│   │   │           │   ├── model
│   │   │           │   │   ├── AnalysisResult.java
│   │   │           │   │   ├── LogisticsRecord.java
│   │   │           │   │   └── SocialPost.java
│   │   │           │   ├── preprocess
│   │   │           │   │   └── TextPreprocessor.java
│   │   │           │   └── ui
│   │   │           │       └── LogisticsDashboard.java
│   │   │           └── logistics-ai
│   │   │               ├── main.py
│   │   │               └── requiments.txt
│   │   └── resources
│   │       └── config.json
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
└── target
    ├── classes
    │   ├── com
    │   │   └── hust
    │   │       ├── logistics
    │   │       │   ├── analyzer
    │   │       │   │   ├── GptAnalysisProvider.class
    │   │       │   │   ├── KeywordSentimentAnalyzer.class
    │   │       │   │   ├── PythonSentimentAnalyzer.class
    │   │       │   │   └── SentimentAnalyzer.class
    │   │       │   ├── clean
    │   │       │   │   ├── application
    │   │       │   │   │   ├── task
    │   │       │   │   │   │   ├── AnalyticsTask.class
    │   │       │   │   │   │   ├── DamageAssessmentTask.class
    │   │       │   │   │   │   ├── GenericAnalyticsTask.class
    │   │       │   │   │   │   ├── ReliefAnalysisTask.class
    │   │       │   │   │   │   └── SentimentTrendTask.class
    │   │       │   │   │   └── usecase
    │   │       │   │   │       └── RunAnalyticsUseCase.class
    │   │       │   │   ├── domain
    │   │       │   │   │   ├── entity
    │   │       │   │   │   │   ├── AnalysisResult.class
    │   │       │   │   │   │   └── SocialPost.class
    │   │       │   │   │   └── gateway
    │   │       │   │   │       ├── AnalysisClient.class
    │   │       │   │   │       └── SocialMediaCrawler.class
    │   │       │   │   ├── infrastructure
    │   │       │   │   │   ├── client
    │   │       │   │   │   │   └── GptAnalysisClient.class
    │   │       │   │   │   ├── config
    │   │       │   │   │   │   ├── AppConfig$AnalysisConfig.class
    │   │       │   │   │   │   ├── AppConfig.class
    │   │       │   │   │   │   └── ConfigLoader.class
    │   │       │   │   │   ├── crawler
    │   │       │   │   │   │   ├── CrawlerFactory.class
    │   │       │   │   │   │   ├── CrawlerStrategyRegistry.class
    │   │       │   │   │   │   ├── GenericSocialCrawler.class
    │   │       │   │   │   │   ├── MockCrawler.class
    │   │       │   │   │   │   ├── TwitterCrawler.class
    │   │       │   │   │   │   └── YouTubeCrawler.class
    │   │       │   │   │   └── preprocess
    │   │       │   │   │       └── TextPreprocessor.class
    │   │       │   │   └── presentation
    │   │       │   │       └── MainAppFx.class
    │   │       │   ├── crawler
    │   │       │   │   ├── DataCrawler.class
    │   │       │   │   └── FileDataCrawler.class
    │   │       │   ├── MainApp.class
    │   │       │   ├── model
    │   │       │   │   ├── AnalysisResult.class
    │   │       │   │   ├── LogisticsRecord.class
    │   │       │   │   └── SocialPost.class
    │   │       │   ├── preprocess
    │   │       │   │   └── TextPreprocessor.class
    │   │       │   └── ui
    │   │       │       ├── LogisticsDashboard$AnalyzedPost.class
    │   │       │       └── LogisticsDashboard.class
    │   │       └── logistics-ai
    │   │           ├── main.py
    │   │           └── requiments.txt
    │   └── config.json
    ├── generated-sources
    │   └── annotations
    ├── maven-status
    │   └── maven-compiler-plugin
    │       └── compile
    │           └── default-compile
    │               ├── createdFiles.lst
    │               └── inputFiles.lst
    └── test-classes
        └── com
            └── hust
                └── logistics
                    └── clean
                        ├── application
                        │   └── usecase
                        │       ├── RunAnalyticsUseCaseTest$NamedTask.class
                        │       ├── RunAnalyticsUseCaseTest$StubCrawler.class
                        │       └── RunAnalyticsUseCaseTest.class
                        └── infrastructure
                            └── crawler
                                └── CrawlerModuleTest.class

76 directories, 84 files
Human-Logistics ) 
```

## Config file

`src/main/resources/config.json` stores:
- keywords
- apiKeys (including Groq and Gemini )
- damageCategories
- analysis endpoint + timeout settings

## Run

```bash
mvn clean test
```

```bash
mvn javafx:run 
```
Goodluck and have a nice day! 
For JavaFX execution, run `com.hust.logistics.clean.presentation.MainAppFx`.
