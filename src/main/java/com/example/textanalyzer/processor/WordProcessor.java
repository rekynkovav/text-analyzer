package com.example.textanalyzer.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WordProcessor {
    private static final Logger logger = LoggerFactory.getLogger(WordProcessor.class);
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\p{L}+(?:'\\p{L}+)?\\b");

    public boolean isValidWord(String word, int minLength) {
        boolean isValid = word != null && word.length() >= minLength;
        if (logger.isTraceEnabled() && word != null) {
            logger.trace("Word '{}' (length {}) is valid: {}", word, word.length(), isValid);
        }
        return isValid;
    }

    public String normalizeWord(String word) {
        if (word == null) {
            logger.trace("Normalizing null word");
            return null;
        }
        String normalized = word.toLowerCase();
        logger.trace("Normalized '{}' to '{}'", word, normalized);
        return normalized;
    }

    public Matcher getWordMatcher(String text) {
        logger.debug("Creating word matcher for text of length: {}", text.length());
        return WORD_PATTERN.matcher(text);
    }
}