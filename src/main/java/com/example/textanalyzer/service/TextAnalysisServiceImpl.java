package com.example.textanalyzer.service;

import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.model.ErrorInfo;
import com.example.textanalyzer.model.WordCount;
import com.example.textanalyzer.processor.WordProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Implementation of {@link TextAnalysisService} that performs text analysis
 * using a {@link WordProcessor} for word extraction and processing.
 *
 * <p>This service walks through directories recursively, processes all
 * .txt files, aggregates word frequencies across files, and handles
 * errors gracefully without interrupting the entire analysis.
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
@Service
public class TextAnalysisServiceImpl implements TextAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(TextAnalysisServiceImpl.class);
    private final WordProcessor wordProcessor;

    /**
     * Constructs a TextAnalysisServiceImpl with the required WordProcessor.
     *
     * @param wordProcessor The word processor component for text operations
     */
    public TextAnalysisServiceImpl(WordProcessor wordProcessor) {
        this.wordProcessor = wordProcessor;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation recursively walks the directory tree,
     * processes each .txt file individually, and aggregates results.
     * If the directory doesn't exist or contains no .txt files,
     * appropriate errors are recorded in the result.
     */
    @Override
    public AnalysisResult analyzeDirectory(String dirPath, int minLength, int topCount, Set<String> stopWords) {
        Map<String, Integer> totalWordCounts = new HashMap<>();
        List<ErrorInfo> errors = new ArrayList<>();

        Path directory = Paths.get(dirPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            logger.error("Directory does not exist: {}", dirPath);
            errors.add(new ErrorInfo(dirPath, "Directory does not exist"));
            return createEmptyResult(dirPath, minLength, topCount, errors);
        }

        try {
            List<Path> txtFiles = Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                    .collect(Collectors.toList());

            if (txtFiles.isEmpty()) {
                logger.warn("No .txt files found in directory: {}", dirPath);
            }

            for (Path filePath : txtFiles) {
                try {
                    Map<String, Integer> fileWordCounts = processFile(filePath.toFile(), minLength, stopWords);
                    fileWordCounts.forEach((word, count) ->
                            totalWordCounts.merge(word, count, Integer::sum)
                    );
                } catch (IOException e) {
                    logger.error("Error processing file: {}", filePath, e);
                    errors.add(new ErrorInfo(filePath.toString(), e.getMessage()));
                }
            }
        } catch (IOException e) {
            logger.error("Error walking directory: {}", dirPath, e);
            errors.add(new ErrorInfo(dirPath, "Error accessing directory: " + e.getMessage()));
        }

        List<WordCount> topWords = getTopWords(totalWordCounts, topCount);
        Map<String, Object> analysisInfo = createAnalysisInfo(dirPath, minLength, topCount);

        return new AnalysisResult(analysisInfo, topWords, errors);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation reads the entire file into memory, which is
     * suitable for reasonably sized text files. Empty files are skipped
     * silently with a debug log message.
     */
    @Override
    public Map<String, Integer> processFile(java.io.File file, int minLength, Set<String> stopWords) throws IOException {
        Map<String, Integer> wordCounts = new HashMap<>();
        String content = Files.readString(file.toPath());

        if (content.trim().isEmpty()) {
            logger.debug("Empty file: {}", file.getName());
            return wordCounts;
        }

        Matcher matcher = wordProcessor.getWordMatcher(content);

        while (matcher.find()) {
            String rawWord = matcher.group();
            String normalizedWord = wordProcessor.normalizeWord(rawWord);

            if (wordProcessor.isValidWord(normalizedWord, minLength) &&
                !stopWords.contains(normalizedWord)) {
                wordCounts.merge(normalizedWord, 1, Integer::sum);
            }
        }

        logger.debug("Processed file: {} - found {} unique words", file.getName(), wordCounts.size());
        return wordCounts;
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
     * Creates an empty analysis result when no files can be processed.
     *
     * @param dirPath Directory path that was analyzed
     * @param minLength Minimum word length parameter
     * @param topCount Top count parameter
     * @param errors List of errors encountered
     * @return AnalysisResult with empty word list
     */
    private AnalysisResult createEmptyResult(String dirPath, int minLength, int topCount, List<ErrorInfo> errors) {
        Map<String, Object> analysisInfo = createAnalysisInfo(dirPath, minLength, topCount);
        return new AnalysisResult(analysisInfo, Collections.emptyList(), errors);
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
}