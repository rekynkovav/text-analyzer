package com.example.textanalyzer.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Component responsible for word processing operations including validation,
 * normalization, and pattern matching.
 *
 * <p>This utility class provides core text processing functionality:
 * <ul>
 *   <li>Validating words against minimum length requirements</li>
 *   <li>Normalizing words to lowercase for case-insensitive comparison</li>
 *   <li>Creating regex matchers for word extraction</li>
 * </ul>
 *
 * <p>The word pattern matches Unicode letters and handles apostrophes
 * for contractions (e.g., "don't").
 *
 * <p>Performance optimizations in version 2.0:
 * <ul>
 *   <li>Removed TRACE-level logging from hot path methods</li>
 *   <li>Simplified validation and normalization logic</li>
 * </ul>
 *
 * @author Text Analyzer Team
 * @version 2.0
 */
@Component
public class WordProcessor {
    private static final Logger logger = LoggerFactory.getLogger(WordProcessor.class);

    /**
     * Regular expression pattern for matching words.
     * Matches Unicode letters and allows apostrophes for contractions.
     */
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\p{L}+(?:'\\p{L}+)?\\b");

    /**
     * Validates whether a word meets the minimum length requirement.
     *
     * <p>This method is called frequently during text processing, so it has
     * been optimized to avoid unnecessary operations and logging overhead.
     *
     * @param word The word to validate (can be null)
     * @param minLength The minimum required length (must be positive)
     * @return {@code true} if the word is not null and its length is >= minLength,
     *         {@code false} otherwise
     */
    public boolean isValidWord(String word, int minLength) {
        return word != null && word.length() >= minLength;
    }

    /**
     * Normalizes a word by converting it to lowercase.
     *
     * <p>This enables case-insensitive word counting (e.g., "Word" and "word"
     * are treated as the same word).
     *
     * <p>This method is called for every word in the text, so it has been
     * optimized to use a ternary operator for minimal overhead.
     *
     * @param word The word to normalize (can be null)
     * @return The lowercase version of the word, or {@code null} if input is null
     */
    public String normalizeWord(String word) {
        return word == null ? null : word.toLowerCase();
    }

    /**
     * Creates a Matcher for extracting words from the given text.
     *
     * <p>The returned matcher uses the predefined word pattern and can be
     * used to iterate through all word matches in the text.
     *
     * @param text The input text to match against
     * @return A Matcher configured with the word pattern for the given text
     */
    public Matcher getWordMatcher(String text) {
        logger.debug("Creating word matcher for text of length: {}", text.length());
        return WORD_PATTERN.matcher(text);
    }
}