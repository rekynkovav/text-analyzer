package com.example.textanalyzer.runner;

import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.service.StopWordsService;
import com.example.textanalyzer.service.TextAnalysisService;
import com.example.textanalyzer.writer.ConsoleWriter;
import com.example.textanalyzer.writer.JsonFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Application runner that orchestrates the text analysis process.
 *
 * <p>This component is automatically executed after the Spring Boot
 * application starts. It parses command-line arguments, validates
 * required parameters, loads stop words, performs the analysis,
 * and outputs results either to console or to a JSON file.
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
@Component
public class TextAnalysisRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(TextAnalysisRunner.class);
    private final TextAnalysisService analysisService;
    private final StopWordsService stopWordsService;
    private final ConsoleWriter consoleWriter;
    private final JsonFileWriter jsonFileWriter;

    /**
     * Constructs the TextAnalysisRunner with required dependencies.
     *
     * @param analysisService Service for performing text analysis
     * @param stopWordsService Service for loading stop words
     * @param consoleWriter Writer for console output
     * @param jsonFileWriter Writer for JSON file output
     */
    public TextAnalysisRunner(TextAnalysisService analysisService,
                              StopWordsService stopWordsService,
                              ConsoleWriter consoleWriter,
                              JsonFileWriter jsonFileWriter) {
        this.analysisService = analysisService;
        this.stopWordsService = stopWordsService;
        this.consoleWriter = consoleWriter;
        this.jsonFileWriter = jsonFileWriter;
    }

    /**
     * Entry point for the analysis process after application startup.
     *
     * <p>This method:
     * <ol>
     *   <li>Displays help if requested</li>
     *   <li>Validates required parameters (--dir, --min-length, --top)</li>
     *   <li>Parses numeric parameters</li>
     *   <li>Loads stop words from optional file</li>
     *   <li>Executes the analysis</li>
     *   <li>Outputs results to console or JSON file</li>
     * </ol>
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
        String minLengthStr = getOptionValue(args, "min-length");
        String topCountStr = getOptionValue(args, "top");

        if (dirPath == null || minLengthStr == null || topCountStr == null) {
            logger.error("Required parameters are missing");
            System.exit(1);
            return;
        }

        int minLength = Integer.parseInt(minLengthStr);
        int topCount = Integer.parseInt(topCountStr);

        String stopwordsPath = getOptionValue(args, "stopwords");
        String outputPath = getOptionValue(args, "output");

        logger.info("Starting text analysis - Directory: {}, Min length: {}, Top: {}",
                dirPath, minLength, topCount);

        Set<String> stopWords = stopWordsService.loadStopWords(stopwordsPath);
        logger.info("Loaded {} stop words", stopWords.size());

        AnalysisResult result = analysisService.analyzeDirectory(dirPath, minLength, topCount, stopWords);

        if (outputPath != null && !outputPath.trim().isEmpty()) {
            jsonFileWriter.write(result, outputPath);
            System.out.println("Results saved to: " + outputPath);
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
     * <p>Supports both --option value and -option value formats.
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
     * <p>Checks that --dir, --min-length, and --top are present,
     * have valid values, and that the directory exists.
     *
     * @param args The command-line arguments
     * @return {@code true} if all required parameters are valid,
     *         {@code false} otherwise
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

        java.nio.file.Path path = java.nio.file.Paths.get(dirPath);
        if (!java.nio.file.Files.exists(path)) {
            logger.error("Directory does not exist: {}", dirPath);
            return false;
        }
        if (!java.nio.file.Files.isDirectory(path)) {
            logger.error("Path is not a directory: {}", dirPath);
            return false;
        }

        return true;
    }

    /**
     * Prints the help message to the console.
     *
     * <p>Displays usage instructions, parameter descriptions,
     * and examples for using the Text Analyzer tool.
     */
    private void printHelp() {
        System.out.println("""
            \nText Analyzer - Word Frequency Analysis Tool
            =============================================
            
            DESCRIPTION:
                Analyzes text files (.txt) in a directory, counts word frequencies,
                and displays the most common words.
            
            USAGE:
                java -jar text-analyzer.jar [OPTIONS]
            
            REQUIRED PARAMETERS:
                --dir <path>          Path to directory containing .txt files
                --min-length <int>    Minimum word length to include in analysis
                --top <int>           Number of most frequent words to display
            
            OPTIONAL PARAMETERS:
                --output <path>       Path to save results as JSON file
                --stopwords <path>    Path to file containing stop words (one per line)
                --help                Display this help message
            
            EXAMPLES:
                # Basic usage (output to console)
                java -jar text-analyzer.jar --dir ./texts --min-length 5 --top 10
                
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
                Console: Simple formatted list with rankings
                JSON: Structured format with analysis metadata and errors
            
            NOTES:
                - Words are case-insensitive (e.g., "Word" and "word" are the same)
                - Punctuation is ignored
                - Only .txt files are processed
                - Empty files are skipped silently
                - Errors are reported but don't stop processing
            """);
    }
}