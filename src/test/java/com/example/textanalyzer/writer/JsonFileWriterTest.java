package com.example.textanalyzer.writer;

import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.model.WordCount;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonFileWriter Tests")
class JsonFileWriterTest {

    private JsonFileWriter jsonFileWriter;
    private ObjectMapper objectMapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jsonFileWriter = new JsonFileWriter();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Write analysis result to JSON file")
    void write_ValidResult_CreatesJsonFile() throws Exception {
        Map<String, Object> analysisInfo = new HashMap<>();
        analysisInfo.put("directory", "/test/path");
        analysisInfo.put("minWordLength", 3);
        analysisInfo.put("topCount", 5);

        List<WordCount> words = List.of(
                new WordCount("hello", 10),
                new WordCount("world", 5)
        );

        AnalysisResult result = new AnalysisResult(analysisInfo, words, Collections.emptyList(),
                "multi", 4, 10, 1234L);

        Path outputFile = tempDir.resolve("result.json");
        jsonFileWriter.write(result, outputFile.toString());

        assertTrue(Files.exists(outputFile));
        String content = Files.readString(outputFile);
        assertTrue(content.contains("hello"));
        assertTrue(content.contains("10"));
        assertTrue(content.contains("multi"));
    }

    @Test
    @DisplayName("Write creates parent directories automatically")
    void write_NestedPath_CreatesParentDirectories() throws Exception {
        Path nestedFile = tempDir.resolve("subdir").resolve("nested").resolve("result.json");

        AnalysisResult result = new AnalysisResult(
                Map.of(), Collections.emptyList(), Collections.emptyList()
        );

        jsonFileWriter.write(result, nestedFile.toString());

        assertTrue(Files.exists(nestedFile));
    }

    @Test
    @DisplayName("Null output path logs error but doesn't throw")
    void write_NullOutputPath_NoException() {
        AnalysisResult result = new AnalysisResult();
        assertDoesNotThrow(() -> jsonFileWriter.write(result, null));
    }

    @Test
    @DisplayName("Empty output path logs warning")
    void write_EmptyOutputPath_NoException() {
        AnalysisResult result = new AnalysisResult();
        assertDoesNotThrow(() -> jsonFileWriter.write(result, ""));
    }

    @Test
    @DisplayName("Default write method logs warning")
    void write_DefaultMethod_LogsWarning() {
        AnalysisResult result = new AnalysisResult();
        assertDoesNotThrow(() -> jsonFileWriter.write(result));
    }
}