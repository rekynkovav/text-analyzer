package com.example.textanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a word with its frequency count in the analyzed text.
 *
 * <p>This immutable-style class pairs a normalized word string with
 * the number of times it appears across all processed files.
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
public class WordCount {
    private String word;
    private int count;

    /**
     * Default constructor required for JSON deserialization.
     */
    public WordCount() {
    }

    /**
     * Constructs a word-count pair.
     *
     * @param word The normalized word (lowercase, no punctuation)
     * @param count The frequency of this word in the analyzed text
     */
    public WordCount(String word, int count) {
        this.word = word;
        this.count = count;
    }

    /**
     * Returns the word string.
     *
     * @return The normalized word
     */
    @JsonProperty("word")
    public String getWord() {
        return word;
    }

    /**
     * Sets the word string.
     *
     * @param word The normalized word
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * Returns the frequency count of this word.
     *
     * @return The number of occurrences
     */
    @JsonProperty("count")
    public int getCount() {
        return count;
    }

    /**
     * Sets the frequency count of this word.
     *
     * @param count The number of occurrences
     */
    public void setCount(int count) {
        this.count = count;
    }
}