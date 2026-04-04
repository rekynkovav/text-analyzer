package com.example.textanalyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StopWordsService Tests")
class StopWordsServiceImplTest {

    private StopWordsServiceImpl stopWordsService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        stopWordsService = new StopWordsServiceImpl();
    }

    @Test
    @DisplayName("Load stop words from valid file")
    void loadStopWords_ValidFile_ReturnsSet() throws Exception {
        Path stopwordsFile = tempDir.resolve("stopwords.txt");
        String content = "the\nand\nfor\nwith\n";
        Files.writeString(stopwordsFile, content);

        Set<String> stopWords = stopWordsService.loadStopWords(stopwordsFile.toString());

        assertEquals(4, stopWords.size());
        assertTrue(stopWords.contains("the"));
        assertTrue(stopWords.contains("and"));
        assertTrue(stopWords.contains("for"));
        assertTrue(stopWords.contains("with"));
    }

    @Test
    @DisplayName("Stop words are case-insensitive")
    void loadStopWords_ShouldConvertToLowercase() throws Exception {
        Path stopwordsFile = tempDir.resolve("stopwords.txt");
        Files.writeString(stopwordsFile, "THE\nAnd\nFoR\n");

        Set<String> stopWords = stopWordsService.loadStopWords(stopwordsFile.toString());

        assertTrue(stopWords.contains("the"));
        assertTrue(stopWords.contains("and"));
        assertTrue(stopWords.contains("for"));
    }

    @Test
    @DisplayName("Empty lines are ignored")
    void loadStopWords_EmptyLinesIgnored() throws Exception {
        Path stopwordsFile = tempDir.resolve("stopwords.txt");
        Files.writeString(stopwordsFile, "the\n\n\nand\n  \nfor");

        Set<String> stopWords = stopWordsService.loadStopWords(stopwordsFile.toString());

        assertEquals(3, stopWords.size());
        assertTrue(stopWords.contains("the"));
        assertTrue(stopWords.contains("and"));
        assertTrue(stopWords.contains("for"));
    }

    @Test
    @DisplayName("Null path returns empty set")
    void loadStopWords_NullPath_ReturnsEmpty() {
        Set<String> result = stopWordsService.loadStopWords(null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Empty string path returns empty set")
    void loadStopWords_EmptyPath_ReturnsEmpty() {
        Set<String> result = stopWordsService.loadStopWords("");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Non-existent file returns empty set (logs warning)")
    void loadStopWords_FileNotFound_ReturnsEmpty() {
        Set<String> result = stopWordsService.loadStopWords("/nonexistent/path/file.txt");
        assertTrue(result.isEmpty());
    }
}