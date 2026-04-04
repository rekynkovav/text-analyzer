package com.example.textanalyzer.processor;

import com.example.textanalyzer.model.ErrorInfo;
import com.example.textanalyzer.service.TextAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Component responsible for parallel file processing using a thread pool.
 *
 * <p>This processor handles both sequential and parallel processing modes,
 * providing thread-safe aggregation of results using concurrent collections.
 *
 * @author Text Analyzer Team
 * @version 2.0
 */
@Component
public class ParallelFileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ParallelFileProcessor.class);
    private final TextAnalysisService analysisService;

    public ParallelFileProcessor(TextAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Processes files in parallel using a fixed thread pool.
     *
     * @param files List of file paths to process
     * @param minLength Minimum word length to include
     * @param stopWords Set of stop words to exclude
     * @param threadPoolSize Number of threads in the pool
     * @return ProcessingResult containing aggregated counts, errors, and statistics
     */
    public ProcessingResult processFilesParallel(
            List<Path> files,
            int minLength,
            Set<String> stopWords,
            int threadPoolSize) {

        if (files.isEmpty()) {
            return new ProcessingResult(new ConcurrentHashMap<>(), new ArrayList<>(), 0, 0);
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<FileProcessingResult>> futures = new ArrayList<>();
        AtomicInteger processedCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (Path filePath : files) {
            futures.add(executor.submit(() -> {
                FileProcessingResult result = new FileProcessingResult();
                result.filePath = filePath.toString();
                try {
                    Map<String, Integer> wordCounts = analysisService.processFile(
                            filePath.toFile(), minLength, stopWords);
                    result.wordCounts = wordCounts;
                    result.success = true;
                    processedCount.incrementAndGet();
                    logger.debug("Successfully processed file: {}", filePath.getFileName());
                } catch (IOException e) {
                    logger.error("Error processing file: {}", filePath, e);
                    result.error = new ErrorInfo(filePath.toString(), e.getMessage());
                    result.success = false;
                }
                return result;
            }));
        }

        Map<String, Integer> totalWordCounts = new ConcurrentHashMap<>();
        List<ErrorInfo> errors = new CopyOnWriteArrayList<>();

        for (Future<FileProcessingResult> future : futures) {
            try {
                FileProcessingResult result = future.get();
                if (result.success && result.wordCounts != null) {
                    result.wordCounts.forEach((word, count) ->
                            totalWordCounts.merge(word, count, Integer::sum)
                    );
                } else if (result.error != null) {
                    errors.add(result.error);
                }
            } catch (InterruptedException e) {
                logger.error("Task interrupted", e);
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException e) {
                logger.error("Task execution failed", e);
                errors.add(new ErrorInfo("unknown", "Execution error: " + e.getMessage()));
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Forcing shutdown of executor after timeout");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Shutdown interrupted", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("Parallel processing completed: {} files processed in {} ms with {} threads",
                processedCount.get(), executionTime, threadPoolSize);

        return new ProcessingResult(totalWordCounts, errors, processedCount.get(), executionTime);
    }

    /**
     * Processes files sequentially for comparison purposes.
     *
     * @param files List of file paths to process
     * @param minLength Minimum word length to include
     * @param stopWords Set of stop words to exclude
     * @return ProcessingResult containing aggregated counts, errors, and statistics
     */
    public ProcessingResult processFilesSequential(
            List<Path> files,
            int minLength,
            Set<String> stopWords) {

        if (files.isEmpty()) {
            return new ProcessingResult(new HashMap<>(), new ArrayList<>(), 0, 0);
        }

        Map<String, Integer> totalWordCounts = new HashMap<>();
        List<ErrorInfo> errors = new ArrayList<>();
        int processedCount = 0;
        long startTime = System.currentTimeMillis();

        for (Path filePath : files) {
            try {
                Map<String, Integer> fileWordCounts = analysisService.processFile(
                        filePath.toFile(), minLength, stopWords);
                fileWordCounts.forEach((word, count) ->
                        totalWordCounts.merge(word, count, Integer::sum)
                );
                processedCount++;
                logger.debug("Successfully processed file: {}", filePath.getFileName());
            } catch (IOException e) {
                logger.error("Error processing file: {}", filePath, e);
                errors.add(new ErrorInfo(filePath.toString(), e.getMessage()));
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        logger.info("Sequential processing completed: {} files processed in {} ms",
                processedCount, executionTime);

        return new ProcessingResult(totalWordCounts, errors, processedCount, executionTime);
    }

    /**
     * Inner class representing the result of a single file processing task.
     */
    private static class FileProcessingResult {
        String filePath;
        Map<String, Integer> wordCounts;
        ErrorInfo error;
        boolean success;
    }

    /**
     * Inner class containing the aggregated results of processing multiple files.
     */
    public static class ProcessingResult {
        public final Map<String, Integer> wordCounts;
        public final List<ErrorInfo> errors;
        public final int processedFiles;
        public final long executionTimeMs;

        public ProcessingResult(Map<String, Integer> wordCounts,
                                List<ErrorInfo> errors,
                                int processedFiles,
                                long executionTimeMs) {
            this.wordCounts = wordCounts;
            this.errors = errors;
            this.processedFiles = processedFiles;
            this.executionTimeMs = executionTimeMs;
        }
    }
}