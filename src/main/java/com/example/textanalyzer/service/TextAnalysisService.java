package com.example.textanalyzer.service;

import com.example.textanalyzer.model.AnalysisResult;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Service interface for text analysis operations.
 *
 * <p>Defines the contract for analyzing text files in a directory,
 * counting word frequencies, and producing analysis results.
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
public interface TextAnalysisService {

    /**
     * Analyzes all .txt files in the specified directory.
     *
     * <p>This method walks through the directory, processes each .txt file,
     * aggregates word frequencies, and returns the top N most frequent words
     * that meet the minimum length requirement and are not stop words.
     *
     * @param dirPath Path to the directory containing .txt files
     * @param minLength Minimum word length to include in analysis
     * @param topCount Number of most frequent words to return
     * @param stopWords Set of stop words to exclude from analysis
     * @return AnalysisResult containing top words, metadata, and any errors
     */
    AnalysisResult analyzeDirectory(String dirPath, int minLength, int topCount, Set<String> stopWords);

    /**
     * Processes a single text file and returns word frequency map.
     *
     * <p>Reads the file content, extracts words using regex pattern matching,
     * normalizes them to lowercase, validates against minimum length,
     * filters out stop words, and counts occurrences.
     *
     * @param file The text file to process
     * @param minLength Minimum word length to include
     * @param stopWords Set of stop words to exclude
     * @return Map where keys are words and values are their frequencies in the file
     * @throws IOException If an I/O error occurs reading the file
     */
    Map<String, Integer> processFile(java.io.File file, int minLength, Set<String> stopWords) throws IOException;
}