package com.example.textanalyzer.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Service interface for text analysis operations.
 *
 * @author Text Analyzer Team
 * @version 2.0
 */
public interface TextAnalysisService {

    /**
     * Processes a single text file and returns word frequency map.
     *
     * @param file The text file to process
     * @param minLength Minimum word length to include
     * @param stopWords Set of stop words to exclude
     * @return Map where keys are words and values are their frequencies in the file
     * @throws IOException If an I/O error occurs reading the file
     */
    Map<String, Integer> processFile(File file, int minLength, Set<String> stopWords) throws IOException;
}