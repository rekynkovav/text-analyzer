package com.example.textanalyzer.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WordProcessor Unit Tests")
class WordProcessorTest {

    private WordProcessor wordProcessor;

    @BeforeEach
    void setUp() {
        wordProcessor = new WordProcessor();
    }

    @Test
    @DisplayName("Normalize word to lowercase")
    void normalizeWord_ShouldConvertToLowerCase() {
        assertEquals("hello", wordProcessor.normalizeWord("Hello"));
        assertEquals("world", wordProcessor.normalizeWord("WORLD"));
        assertEquals("mixed", wordProcessor.normalizeWord("MiXeD"));
    }

    @Test
    @DisplayName("Normalize returns null for null input")
    void normalizeWord_NullInput_ReturnsNull() {
        assertNull(wordProcessor.normalizeWord(null));
    }

    @ParameterizedTest
    @CsvSource({
            "hello, 3, true",
            "hi, 3, false",
            "longword, 10, false",
            "exact, 5, true"
    })
    @DisplayName("Validate word length requirements")
    void isValidWord_ShouldCheckLength(String word, int minLength, boolean expected) {
        assertEquals(expected, wordProcessor.isValidWord(word, minLength));
    }

    @Test
    @DisplayName("Null word is invalid regardless of minLength")
    void isValidWord_NullWord_ReturnsFalse() {
        assertFalse(wordProcessor.isValidWord(null, 1));
        assertFalse(wordProcessor.isValidWord(null, 100));
    }

    @Test
    @DisplayName("Word matcher extracts words correctly")
    void getWordMatcher_ShouldExtractWords() {
        String text = "the quick brown fox jumps high";
        Matcher matcher = wordProcessor.getWordMatcher(text);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        assertEquals(6, count);

        matcher.reset();
        assertTrue(matcher.find());
        assertEquals("the", matcher.group());
    }

    @Test
    @DisplayName("Word matcher handles punctuation correctly")
    void getWordMatcher_WithPunctuation_ExtractsWordsOnly() {
        String text = "Hello, world! This is a test.";
        Matcher matcher = wordProcessor.getWordMatcher(text);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        assertEquals(6, count); // Hello, world, This, is, a, test
    }

    @Test
    @DisplayName("Word matcher handles contractions as single words")
    void getWordMatcher_ShouldHandleContractions() {
        String text = "Don't do that. It's fine.";
        Matcher matcher = wordProcessor.getWordMatcher(text);

        assertTrue(matcher.find());
        assertEquals("Don't", matcher.group());
        assertTrue(matcher.find());
        assertEquals("do", matcher.group());
        assertTrue(matcher.find());
        assertEquals("that", matcher.group());
        assertTrue(matcher.find());
        assertEquals("It's", matcher.group());
        assertTrue(matcher.find());
        assertEquals("fine", matcher.group());

        assertFalse(matcher.find());
    }

    @Test
    @DisplayName("Empty string returns no matches")
    void getWordMatcher_EmptyString_ReturnsNoMatches() {
        String text = "";
        Matcher matcher = wordProcessor.getWordMatcher(text);

        assertFalse(matcher.find());
    }
}