package com.example.textanalyzer.processor;

import com.example.textanalyzer.config.ProcessorConfig;
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
 * Configuration parameters such as executor timeout are loaded from
 * {@link ProcessorConfig}.
 *
 * <p>Key features:
 * <ul>
 *   <li>Parallel processing with configurable thread pool size</li>
 *   <li>Sequential processing for comparison and single-threaded environments</li>
 *   <li>Proper resource management with guaranteed ExecutorService shutdown</li>
 *   <li>Automatic cancellation of remaining tasks on interruption</li>
 *   <li>Configurable timeout for executor termination</li>
 * </ul>
 *
 * @author Text Analyzer Team
 * @version 3.0
 */
@Component
public class ParallelFileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ParallelFileProcessor.class);
    private final TextAnalysisService analysisService;
    private final ProcessorConfig config;

    /**
     * Constructs a ParallelFileProcessor with required dependencies.
     *
     * @param analysisService Service for analyzing individual text files
     * @param config Configuration containing thread pool and timeout settings
     */
    public ParallelFileProcessor(TextAnalysisService analysisService, ProcessorConfig config) {
        this.analysisService = analysisService;
        this.config = config;
    }

    /**
     * Processes files in parallel using a fixed thread pool.
     *
     * <p>This method creates a thread pool of the specified size and processes
     * all files concurrently. Results are aggregated in a thread-safe manner
     * using {@link ConcurrentHashMap} and {@link CopyOnWriteArrayList}.
     *
     * <p>Resource management:
     * <ul>
     *   <li>ExecutorService is guaranteed to shut down via try-finally block</li>
     *   <li>Timeout for termination is configurable via {@link ProcessorConfig}</li>
     *   <li>On interruption, remaining tasks are cancelled automatically</li>
     *   <li>Forced shutdown occurs if graceful termination times out</li>
     * </ul>
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

        try {
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
                    cancelRemainingTasks(futures);
                    break;
                } catch (ExecutionException e) {
                    logger.error("Task execution failed", e);
                    errors.add(new ErrorInfo("unknown", "Execution error: " + e.getMessage()));
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("Parallel processing completed: {} files processed in {} ms with {} threads",
                    processedCount.get(), executionTime, threadPoolSize);

            return new ProcessingResult(totalWordCounts, errors, processedCount.get(), executionTime);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(config.getExecutorTimeoutSeconds(), TimeUnit.SECONDS)) {
                    logger.warn("Forcing shutdown of executor after timeout");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error("Shutdown interrupted", e);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Cancels all remaining futures that haven't completed yet.
     *
     * <p>This method is called when processing is interrupted to prevent
     * resource leaks and ensure clean shutdown. Only non-completed tasks
     * are cancelled with interruption flag set to true.
     *
     * @param futures List of futures to cancel
     */
    private void cancelRemainingTasks(List<Future<FileProcessingResult>> futures) {
        for (Future<FileProcessingResult> future : futures) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
    }

    /**
     * Processes files sequentially for comparison purposes.
     *
     * <p>This method processes files one at a time in a single thread.
     * It is useful for:
     * <ul>
     *   <li>Comparing performance with parallel processing</li>
     *   <li>Environments where multi-threading is not desired</li>
     *   <li>Debugging and testing purposes</li>
     * </ul>
     *
     * <p>Uses standard {@link HashMap} instead of concurrent collections
     * since no thread synchronization is needed.
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
     *
     * <p>Used internally by parallel processing to capture the outcome
     * of each file analysis task submitted to the thread pool.
     */
    private static class FileProcessingResult {
        String filePath;
        Map<String, Integer> wordCounts;
        ErrorInfo error;
        boolean success;
    }

    /**
     * Inner class containing the aggregated results of processing multiple files.
     *
     * <p>This immutable result object contains:
     * <ul>
     *   <li>Aggregated word counts from all successfully processed files</li>
     *   <li>List of errors encountered during processing</li>
     *   <li>Count of successfully processed files</li>
     *   <li>Total execution time in milliseconds</li>
     * </ul>
     */
    public static class ProcessingResult {
        public final Map<String, Integer> wordCounts;
        public final List<ErrorInfo> errors;
        public final int processedFiles;
        public final long executionTimeMs;

        /**
         * Constructs a ProcessingResult with the given data.
         *
         * @param wordCounts Aggregated word frequency map
         * @param errors List of errors that occurred during processing
         * @param processedFiles Number of successfully processed files
         * @param executionTimeMs Total execution time in milliseconds
         */
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