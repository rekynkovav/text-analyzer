package com.example.textanalyzer.writer;

import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.model.WordCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsoleWriter implements ResultWriter {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleWriter.class);

    @Override
    public void write(AnalysisResult result) {
        logger.debug("Writing analysis result to console");

        if (result.getWords().isEmpty()) {
            logger.info("No words found matching the criteria");
            System.out.println("No words found matching the criteria.");

            if (!result.getErrors().isEmpty()) {
                logger.warn("Found {} errors during analysis", result.getErrors().size());
                System.out.println("\nErrors encountered:");
                result.getErrors().forEach(error -> {
                    logger.debug("Error - File: {}, Message: {}", error.getFile(), error.getMessage());
                    System.out.printf("  - %s: %s%n", error.getFile(), error.getMessage());
                });
            }
            return;
        }

        logger.info("Writing top {} words to console", result.getWords().size());
        System.out.println("\nTop " + result.getWords().size() + " most frequent words:");
        System.out.println("=".repeat(40));

        int rank = 1;
        for (WordCount wc : result.getWords()) {
            logger.trace("Word #{}: {} - {}", rank, wc.getWord(), wc.getCount());
            System.out.printf("%d. %s — %d%n", rank++, wc.getWord(), wc.getCount());
        }

        if (!result.getErrors().isEmpty()) {
            logger.warn("Analysis completed with {} errors", result.getErrors().size());
            System.out.println("\nErrors encountered:");
            result.getErrors().forEach(error -> {
                logger.debug("Error - File: {}, Message: {}", error.getFile(), error.getMessage());
                System.out.printf("  - %s: %s%n", error.getFile(), error.getMessage());
            });
        }

        logger.info("Console output completed successfully");
    }
}