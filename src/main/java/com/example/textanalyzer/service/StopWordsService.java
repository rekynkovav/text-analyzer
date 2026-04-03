package com.example.textanalyzer.service;

import java.util.Set;

/**
 * Service interface for loading stop words from external sources.
 *
 * <p>Stop words are common words (e.g., "the", "and", "for") that are
 * filtered out during text analysis as they don't carry significant meaning.
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
public interface StopWordsService {

    /**
     * Loads a set of stop words from the specified file path.
     *
     * <p>The file should contain one stop word per line. Empty lines are ignored.
     * Words are automatically converted to lowercase for case-insensitive matching.
     *
     * @param stopwordsPath Path to the stop words file (can be null or empty)
     * @return A Set of lowercase stop words, or an empty set if:
     *         <ul>
     *           <li>The path is null or empty</li>
     *           <li>The file does not exist</li>
     *           <li>An I/O error occurs during reading</li>
     *         </ul>
     */
    Set<String> loadStopWords(String stopwordsPath);
}