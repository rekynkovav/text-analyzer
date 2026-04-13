package com.example.textanalyzer.runner;

import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.model.ErrorInfo;
import com.example.textanalyzer.model.ProcessingMode;
import com.example.textanalyzer.model.WordCount;
import com.example.textanalyzer.processor.ParallelFileProcessor;
import com.example.textanalyzer.service.StopWordsService;
import com.example.textanalyzer.writer.ConsoleWriter;
import com.example.textanalyzer.writer.JsonFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application runner that orchestrates the text analysis process.
 *
 * <p>This component is automatically executed after the Spring Boot
 * application starts. It parses command-line arguments, validates
 * required parameters, loads stop words, performs the analysis
 * (either single-threaded or multi-threaded), and outputs results.
 *
 * @author Text Analyzer Team
 * @version 2.0
 */
@Component
public class TextAnalysisRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(TextAnalysisRunner.class);
    private static final int DEFAULT_THREADS = 2;

    private final StopWordsService stopWordsService;
    private final ConsoleWriter consoleWriter;
    private final JsonFileWriter jsonFileWriter;
    private final ParallelFileProcessor parallelFileProcessor;

    /**
     * Constructs the TextAnalysisRunner with required dependencies.
     *
     * @param stopWordsService Service for loading stop words
     * @param consoleWriter Writer for console output
     * @param jsonFileWriter Writer for JSON file output
     * @param parallelFileProcessor Processor for parallel file handling
     */
    public TextAnalysisRunner(StopWordsService stopWordsService,
                              ConsoleWriter consoleWriter,
                              JsonFileWriter jsonFileWriter,
                              ParallelFileProcessor parallelFileProcessor) {
        this.stopWordsService = stopWordsService;
        this.consoleWriter = consoleWriter;
        this.jsonFileWriter = jsonFileWriter;
        this.parallelFileProcessor = parallelFileProcessor;
    }

    /**
     * Entry point for the analysis process after application startup.
     *
     * @param args Command-line arguments passed to the application
     * @throws Exception If an unexpected error occurs during execution
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("help")) {
            printHelp();
            return;
        }

        if (!validateRequiredParameters(args)) {
            printHelp();
            System.exit(1);
            return;
        }

        String dirPath = getOptionValue(args, "dir");
        int minLength = Integer.parseInt(getOptionValue(args, "min-length"));
        int topCount = Integer.parseInt(getOptionValue(args, "top"));

        String stopwordsPath = getOptionValue(args, "stopwords");
        String outputPath = getOptionValue(args, "output");
        ProcessingMode mode = ProcessingMode.fromString(getOptionValue(args, "mode"));
        int threads = parseThreads(getOptionValue(args, "threads"));

        logger.info("Starting text analysis - Directory: {}, Min length: {}, Top: {}, Mode: {}, Threads: {}",
                dirPath, minLength, topCount, mode.getValue(), threads);

        Set<String> stopWords = stopWordsService.loadStopWords(stopwordsPath);
        logger.info("Loaded {} stop words", stopWords.size());

        List<Path> txtFiles = getTextFiles(dirPath);
        if (txtFiles.isEmpty()) {
            logger.warn("No .txt files found in directory: {}", dirPath);
            AnalysisResult emptyResult = createEmptyResult(dirPath, minLength, topCount,
                    mode.getValue(), threads, 0, 0L);
            emptyResult.setErrors(List.of(new ErrorInfo(dirPath, "No .txt files found")));
            outputResult(emptyResult, outputPath);
            return;
        }

        logger.info("Found {} .txt files to process", txtFiles.size());

        AnalysisResult result = executeAnalysis(dirPath, minLength, topCount,
                stopWords, mode, threads, txtFiles);

        outputResult(result, outputPath);
    }

    /**
     * Executes analysis in the specified mode.
     */
    private AnalysisResult executeAnalysis(String dirPath, int minLength, int topCount,
                                           Set<String> stopWords, ProcessingMode mode,
                                           int threads, List<Path> txtFiles) {
        ParallelFileProcessor.ProcessingResult processingResult;

        if (mode == ProcessingMode.SINGLE) {
            logger.info("Running in SINGLE mode");
            processingResult = parallelFileProcessor.processFilesSequential(txtFiles, minLength, stopWords);
        } else {
            logger.info("Running in MULTI mode with {} threads", threads);
            processingResult = parallelFileProcessor.processFilesParallel(txtFiles, minLength, stopWords, threads);
        }

        List<WordCount> topWords = getTopWords(processingResult.wordCounts, topCount);
        Map<String, Object> analysisInfo = createAnalysisInfo(dirPath, minLength, topCount);

        printSummary(mode, threads, processingResult.processedFiles, processingResult.executionTimeMs);

        return new AnalysisResult(analysisInfo, topWords, processingResult.errors,
                mode.getValue(), mode == ProcessingMode.SINGLE ? 1 : threads,
                processingResult.processedFiles, processingResult.executionTimeMs);
    }

    /**
     * Prints execution summary to console.
     */
    private void printSummary(ProcessingMode mode, int threads, int processedFiles, long executionTimeMs) {
        if (mode == ProcessingMode.MULTI) {
            System.out.printf("%nMode: MULTI (%d workers)%n", threads);
        } else {
            System.out.printf("%nMode: SINGLE%n");
        }
        System.out.printf("Processed %d files in %d ms%n", processedFiles, executionTimeMs);
    }

    /**
     * Outputs result to console or JSON file.
     */
    private void outputResult(AnalysisResult result, String outputPath) {
        if (outputPath != null && !outputPath.trim().isEmpty()) {
            jsonFileWriter.write(result, outputPath);
            System.out.println("\nResults saved to: " + outputPath);
        } else {
            consoleWriter.write(result);
        }

        if (!result.getErrors().isEmpty()) {
            logger.warn("Analysis completed with {} error(s)", result.getErrors().size());
        } else {
            logger.info("Analysis completed successfully");
        }
    }

    /**
     * Retrieves the value of a command-line option.
     *
     * @param args The command-line arguments
     * @param optionName The name of the option (without hyphens)
     * @return The option value, or null if not found
     */
    private String getOptionValue(ApplicationArguments args, String optionName) {
        if (args.containsOption(optionName)) {
            List<String> values = args.getOptionValues(optionName);
            if (values != null && !values.isEmpty()) {
                return values.get(0);
            }
        }

        String[] sourceArgs = args.getSourceArgs();
        for (int i = 0; i < sourceArgs.length - 1; i++) {
            if (("--" + optionName).equals(sourceArgs[i]) ||
                    ("-" + optionName).equals(sourceArgs[i])) {
                String value = sourceArgs[i + 1];
                if (!value.startsWith("-")) {
                    return value;
                }
            }
        }

        return null;
    }

    /**
     * Validates the required command-line parameters.
     *
     * @param args The command-line arguments
     * @return {@code true} if all required parameters are valid
     */
    private boolean validateRequiredParameters(ApplicationArguments args) {
        if (args.containsOption("help")) {
            return true;
        }

        String dirPath = getOptionValue(args, "dir");
        if (dirPath == null || dirPath.trim().isEmpty()) {
            logger.error("Missing required parameter: --dir");
            return false;
        }

        String minLengthStr = getOptionValue(args, "min-length");
        if (minLengthStr == null || minLengthStr.trim().isEmpty()) {
            logger.error("Missing required parameter: --min-length");
            return false;
        }

        String topCountStr = getOptionValue(args, "top");
        if (topCountStr == null || topCountStr.trim().isEmpty()) {
            logger.error("Missing required parameter: --top");
            return false;
        }

        try {
            int minLength = Integer.parseInt(minLengthStr);
            if (minLength <= 0) {
                logger.error("--min-length must be a positive integer");
                return false;
            }

            int topCount = Integer.parseInt(topCountStr);
            if (topCount <= 0) {
                logger.error("--top must be a positive integer");
                return false;
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid numeric parameter: {}", e.getMessage());
            return false;
        }

        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            logger.error("Directory does not exist: {}", dirPath);
            return false;
        }
        if (!Files.isDirectory(path)) {
            logger.error("Path is not a directory: {}", dirPath);
            return false;
        }

        return true;
    }

    /**
     * Parses the threads parameter.
     *
     * @param threadsStr Threads parameter value
     * @return Number of threads (default 2 if invalid)
     */
    private int parseThreads(String threadsStr) {
        if (threadsStr == null) {
            return DEFAULT_THREADS;
        }
        try {
            int threads = Integer.parseInt(threadsStr);
            if (threads > 0) {
                return threads;
            }
            logger.warn("--threads must be positive, using default: {}", DEFAULT_THREADS);
        } catch (NumberFormatException e) {
            logger.warn("Invalid --threads value: {}, using default: {}", threadsStr, DEFAULT_THREADS);
        }
        return DEFAULT_THREADS;
    }

    /**
     * Retrieves all .txt files from the directory recursively.
     *
     * @param dirPath Directory path
     * @return List of Path objects for .txt files
     * @throws IOException If directory cannot be read
     */
    private List<Path> getTextFiles(String dirPath) throws IOException {
        Path directory = Paths.get(dirPath);
        return Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                .collect(Collectors.toList());
    }

    /**
     * Extracts the top N most frequent words from the word frequency map.
     *
     * @param wordCounts Map of words to their frequencies
     * @param topCount Number of top words to return
     * @return List of WordCount objects sorted by frequency (descending)
     */
    private List<WordCount> getTopWords(Map<String, Integer> wordCounts, int topCount) {
        return wordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topCount)
                .map(entry -> new WordCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Creates a metadata map with analysis configuration parameters.
     *
     * @param dirPath Directory path that was analyzed
     * @param minLength Minimum word length parameter
     * @param topCount Top count parameter
     * @return Map containing analysis metadata
     */
    private Map<String, Object> createAnalysisInfo(String dirPath, int minLength, int topCount) {
        Map<String, Object> info = new HashMap<>();
        info.put("directory", dirPath);
        info.put("minWordLength", minLength);
        info.put("topCount", topCount);
        return info;
    }

    /**
     * Creates an empty analysis result.
     */
    private AnalysisResult createEmptyResult(String dirPath, int minLength, int topCount,
                                             String mode, int threads, int processedFiles, long executionTimeMs) {
        Map<String, Object> analysisInfo = createAnalysisInfo(dirPath, minLength, topCount);
        return new AnalysisResult(analysisInfo, Collections.emptyList(), new ArrayList<>(),
                mode, threads, processedFiles, executionTimeMs);
    }

    /**
     * Prints the help message to the console.
     */
    private void printHelp() {
        System.out.println("""
            \nText Analyzer - Word Frequency Analysis Tool
            =============================================
            
            DESCRIPTION:
                Analyzes text files (.txt) in a directory, counts word frequencies,
                and displays the most common words. Supports both single-threaded
                and multi-threaded processing modes.
            
            USAGE:
                java -jar text-analyzer.jar [OPTIONS]
            
            REQUIRED PARAMETERS:
                --dir <path>          Path to directory containing .txt files
                --min-length <int>    Minimum word length to include in analysis
                --top <int>           Number of most frequent words to display
            
            OPTIONAL PARAMETERS:
                --output <path>       Path to save results as JSON file
                --stopwords <path>    Path to file containing stop words (one per line)
                --mode single|multi   Processing mode (default: multi)
                --threads <int>       Number of threads for multi mode (default: 2)
                --help                Display this help message
            
            EXAMPLES:
                # Basic usage (multi-threaded, default 2 threads)
                java -jar text-analyzer.jar --dir ./texts --min-length 5 --top 10
                
                # Multi-threaded with 8 threads
                java -jar text-analyzer.jar --dir ./texts --min-length 5 --top 10 --mode multi --threads 8
                
                # Single-threaded mode for comparison
                java -jar text-analyzer.jar --dir ./texts --min-length 5 --top 10 --mode single
                
                # With stopwords and JSON output
                java -jar text-analyzer.jar --dir ./texts --min-length 5 --top 10 \\
                                           --stopwords ./stopwords.txt --output ./results.json
                
                # Show help
                java -jar text-analyzer.jar --help
            
            STOPWORDS FILE FORMAT:
                Each stop word on a separate line:
                the
                and
                for
                with
            
            OUTPUT FORMATS:
                Console: Formatted list with mode, timing, rankings, and errors
                JSON: Structured format with analysis metadata, words, and errors
            
            NOTES:
                - Words are case-insensitive (e.g., "Word" and "word" are the same)
                - Punctuation is ignored
                - Only .txt files are processed
                - Empty files are skipped silently
                - Errors are reported but don't stop processing
                - Multi-threaded mode can significantly improve performance on multi-core systems
            """);
    }
}