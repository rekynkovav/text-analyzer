package com.example.textanalyzer.writer;

import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.model.WordCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Writes analysis results to the console in a human-readable format.
 *
 * <p>Outputs execution statistics, a formatted list of top words with
 * their frequencies, followed by any errors that occurred during processing.
 *
 * @author Text Analyzer Team
 * @version 2.0
 */
@Component
public class ConsoleWriter implements ResultWriter {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleWriter.class);

    /**
     * Writes the analysis result to the console.
     *
     * <p>Includes execution mode, number of processed files, execution time,
     * top words list, and any errors encountered.
     *
     * @param result The analysis result to write to the console
     */
    @Override
    public void write(AnalysisResult result) {
        logger.debug("Writing analysis result to console");

        if (result.getMode() != null && result.getProcessedFiles() != null && result.getExecutionTimeMs() != null) {
            if ("multi".equals(result.getMode())) {
                System.out.printf("%nMode: MULTI (%d workers)%n", result.getThreads());
            } else {
                System.out.printf("%nMode: SINGLE%n");
            }
            System.out.printf("Processed %d files in %d ms%n",
                    result.getProcessedFiles(), result.getExecutionTimeMs());
        }

        if (result.getWords().isEmpty()) {
            System.out.println("\nNo words found matching the criteria.");
        } else {
            Integer minLength = null;
            if (result.getAnalysisInfo() != null && result.getAnalysisInfo().containsKey("minWordLength")) {
                minLength = (Integer) result.getAnalysisInfo().get("minWordLength");
            }

            if (minLength != null) {
                System.out.printf("%nTop %d words (min length = %d):%n",
                        result.getWords().size(), minLength);
            } else {
                System.out.printf("%nTop %d words:%n", result.getWords().size());
            }
            System.out.println("=".repeat(40));

            int rank = 1;
            for (WordCount wc : result.getWords()) {
                logger.trace("Word #{}: {} - {}", rank, wc.getWord(), wc.getCount());
                System.out.printf("%d. %s — %d%n", rank++, wc.getWord(), wc.getCount());
            }
        }

        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
            logger.warn("Analysis completed with {} errors", result.getErrors().size());
            System.out.println("\nErrors encountered:");
            for (var error : result.getErrors()) {
                logger.debug("Error - File: {}, Message: {}", error.getFile(), error.getMessage());
                System.out.printf("  - %s: %s%n", error.getFile(), error.getMessage());
            }
        }

        logger.info("Console output completed successfully");
    }
}