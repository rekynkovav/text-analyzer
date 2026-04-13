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
 * @author Text Analyzer Team
 * @version 2.0
 */
@Service
public class TextAnalysisServiceImpl implements TextAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(TextAnalysisServiceImpl.class);
    private final WordProcessor wordProcessor;

    public TextAnalysisServiceImpl(WordProcessor wordProcessor) {
        this.wordProcessor = wordProcessor;
    }

    @Override
    public Map<String, Integer> processFile(File file, int minLength, Set<String> stopWords) throws IOException {
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
}