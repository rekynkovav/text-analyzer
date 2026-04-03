package com.example.textanalyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link StopWordsService} that loads stop words from
 * a text file using Java NIO.
 *
 * <p>This service reads the file line by line, trims whitespace,
 * filters out empty lines, and converts all words to lowercase.
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
@Service
public class StopWordsServiceImpl implements StopWordsService {
    private static final Logger logger = LoggerFactory.getLogger(StopWordsServiceImpl.class);

    /**
     * {@inheritDoc}
     *
     * <p>If the file cannot be read or does not exist, a warning is logged
     * and an empty set is returned, allowing analysis to continue without
     * stop word filtering.
     */
    @Override
    public Set<String> loadStopWords(String stopwordsPath) {
        if (stopwordsPath == null || stopwordsPath.trim().isEmpty()) {
            return Collections.emptySet();
        }

        try {
            Path path = Paths.get(stopwordsPath);
            if (!Files.exists(path)) {
                logger.warn("Stopwords file not found: {}", stopwordsPath);
                return Collections.emptySet();
            }

            return Files.lines(path)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            logger.error("Error reading stopwords file: {}", stopwordsPath, e);
            return Collections.emptySet();
        }
    }
}