package com.example.textanalyzer.service;

import com.example.textanalyzer.processor.WordProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Implementation of {@link TextAnalysisService} that performs text analysis
 * using a {@link WordProcessor} for word extraction and processing.
 *
 * <p>This implementation uses streaming file processing via {@link Files#lines}
 * to minimize memory consumption, making it suitable for processing large files
 * that may not fit entirely in memory.
 *
 * @author Text Analyzer Team
 * @version 3.0
 */
@Service
public class TextAnalysisServiceImpl implements TextAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(TextAnalysisServiceImpl.class);
    private final WordProcessor wordProcessor;

    /**
     * Constructs a TextAnalysisServiceImpl with the specified word processor.
     *
     * @param wordProcessor Processor for word validation and normalization
     */
    public TextAnalysisServiceImpl(WordProcessor wordProcessor) {
        this.wordProcessor = wordProcessor;
    }

    /**
     * Processes a text file and counts word frequencies.
     *
     * <p>This method uses streaming line-by-line processing to handle files
     * of any size without loading the entire content into memory. Each line
     * is processed independently, extracting words using regex patterns,
     * normalizing them to lowercase, and filtering based on minimum length
     * and stop words.
     *
     * <p>Memory efficiency:
     * <ul>
     *   <li>Files are read line-by-line using {@link Files#lines}</li>
     *   <li>Only word counts are kept in memory, not file content</li>
     *   <li>Suitable for processing files larger than available heap space</li>
     * </ul>
     *
     * @param file The text file to process
     * @param minLength Minimum word length to include in results
     * @param stopWords Set of words to exclude from counting
     * @return Map of words to their frequency counts
     * @throws IOException If the file cannot be read
     */
    @Override
    public Map<String, Integer> processFile(File file, int minLength, Set<String> stopWords) throws IOException {
        Map<String, Integer> wordCounts = new HashMap<>();

        try (var lines = Files.lines(file.toPath())) {
            lines.forEach(line -> {
                Matcher matcher = wordProcessor.getWordMatcher(line);
                while (matcher.find()) {
                    String rawWord = matcher.group();
                    String normalizedWord = wordProcessor.normalizeWord(rawWord);

                    if (wordProcessor.isValidWord(normalizedWord, minLength) &&
                            !stopWords.contains(normalizedWord)) {
                        wordCounts.merge(normalizedWord, 1, Integer::sum);
                    }
                }
            });
        }

        logger.debug("Processed file: {} - found {} unique words", file.getName(), wordCounts.size());
        return wordCounts;
    }
}