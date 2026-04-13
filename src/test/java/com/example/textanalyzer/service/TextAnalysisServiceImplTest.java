package com.example.textanalyzer.service;

import com.example.textanalyzer.processor.WordProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TextAnalysisServiceImplTest {

    private WordProcessor realWordProcessor;
    private TextAnalysisServiceImpl textAnalysisService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        realWordProcessor = new WordProcessor();
        textAnalysisService = new TextAnalysisServiceImpl(realWordProcessor);
    }

    @Test
    @DisplayName("Process file with valid content")
    void processFile_ValidContent_ReturnsWordCounts() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Hello world hello java");

        Map<String, Integer> result = textAnalysisService.processFile(
                testFile.toFile(), 3, Set.of()
        );

        assertEquals(3, result.size());
        assertEquals(2, result.get("hello"));
        assertEquals(1, result.get("world"));
        assertEquals(1, result.get("java"));
    }

    @Test
    @DisplayName("Empty file returns empty map")
    void processFile_EmptyFile_ReturnsEmpty() throws Exception {
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.writeString(emptyFile, "");

        Map<String, Integer> result = textAnalysisService.processFile(
                emptyFile.toFile(), 3, Set.of()
        );

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Stop words are filtered out")
    void processFile_StopWords_ExcludedFromCount() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "the quick brown fox jumps over the lazy dog");

        Set<String> stopWords = Set.of("the", "over");
        Map<String, Integer> result = textAnalysisService.processFile(
                testFile.toFile(), 3, stopWords
        );

        assertFalse(result.containsKey("the"));
        assertFalse(result.containsKey("over"));
        assertTrue(result.containsKey("quick"));
        assertTrue(result.containsKey("brown"));
        assertTrue(result.containsKey("fox"));
        assertTrue(result.containsKey("jumps"));
        assertTrue(result.containsKey("lazy"));
        assertTrue(result.containsKey("dog"));
    }

    @Test
    @DisplayName("Words shorter than minLength are filtered out")
    void processFile_MinLengthFilter_ExcludesShortWords() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "a an the cat dog elephant");

        Map<String, Integer> result = textAnalysisService.processFile(
                testFile.toFile(), 5, Set.of()
        );

        assertFalse(result.containsKey("a"));
        assertFalse(result.containsKey("an"));
        assertFalse(result.containsKey("the"));
        assertFalse(result.containsKey("cat"));
        assertFalse(result.containsKey("dog"));
        assertTrue(result.containsKey("elephant"));
        assertEquals(1, result.get("elephant"));
    }

    @Test
    @DisplayName("Case insensitive word counting")
    void processFile_CaseInsensitive_CountsTogether() throws Exception {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Hello HELLO hello HeLlO");

        Map<String, Integer> result = textAnalysisService.processFile(
                testFile.toFile(), 3, Set.of()
        );

        assertEquals(1, result.size());
        assertEquals(4, result.get("hello"));
    }
}