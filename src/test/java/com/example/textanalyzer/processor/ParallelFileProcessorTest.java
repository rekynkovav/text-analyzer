package com.example.textanalyzer.processor;

import com.example.textanalyzer.service.TextAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParallelFileProcessor Tests")
class ParallelFileProcessorTest {

    @Mock
    private TextAnalysisService analysisService;

    private ParallelFileProcessor processor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        processor = new ParallelFileProcessor(analysisService);
    }

    @Test
    @DisplayName("Sequential processing aggregates results correctly")
    void processFilesSequential_SuccessfulFiles_AggregatesCounts() throws Exception {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Files.writeString(file1, "hello world");
        Files.writeString(file2, "hello java");

        Map<String, Integer> file1Counts = Map.of("hello", 1, "world", 1);
        Map<String, Integer> file2Counts = Map.of("hello", 1, "java", 1);

        when(analysisService.processFile(any(File.class), eq(3), anySet()))
                .thenReturn(file1Counts)
                .thenReturn(file2Counts);

        ParallelFileProcessor.ProcessingResult result = processor.processFilesSequential(
                List.of(file1, file2), 3, Set.of()
        );

        assertEquals(2, result.processedFiles);
        assertEquals(3, result.wordCounts.size());
        assertEquals(2, result.wordCounts.get("hello"));
        assertTrue(result.errors.isEmpty());
        assertTrue(result.executionTimeMs >= 0);
    }

    @Test
    @DisplayName("Sequential processing handles errors gracefully")
    void processFilesSequential_ErrorFile_ContinuesProcessing() throws Exception {
        Path goodFile = tempDir.resolve("good.txt");
        Path badFile = tempDir.resolve("bad.txt");
        Files.writeString(goodFile, "test content");
        Files.writeString(badFile, "will fail");

        Map<String, Integer> goodCounts = Map.of("test", 1);
        when(analysisService.processFile(eq(goodFile.toFile()), eq(3), anySet()))
                .thenReturn(goodCounts);
        when(analysisService.processFile(eq(badFile.toFile()), eq(3), anySet()))
                .thenThrow(new IOException("File access denied"));

        ParallelFileProcessor.ProcessingResult result = processor.processFilesSequential(
                List.of(goodFile, badFile), 3, Set.of()
        );

        assertEquals(1, result.processedFiles);
        assertEquals(1, result.errors.size());
        assertEquals("File access denied", result.errors.get(0).getMessage());
        assertEquals(1, result.wordCounts.get("test"));
    }

    @Test
    @DisplayName("Empty file list returns empty result")
    void processFilesSequential_EmptyList_ReturnsEmptyResult() {
        ParallelFileProcessor.ProcessingResult result = processor.processFilesSequential(
                Collections.emptyList(), 3, Set.of()
        );

        assertTrue(result.wordCounts.isEmpty());
        assertTrue(result.errors.isEmpty());
        assertEquals(0, result.processedFiles);
        assertEquals(0, result.executionTimeMs);
    }

    @Test
    @DisplayName("Parallel processing with thread pool works correctly")
    void processFilesParallel_WithThreadPool_AggregatesResults() throws Exception {
        List<Path> files = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Path file = tempDir.resolve("file" + i + ".txt");
            Files.writeString(file, "common word " + i);
            files.add(file);
        }

        when(analysisService.processFile(any(File.class), eq(3), anySet()))
                .thenReturn(Map.of("common", 1, "unique" + System.currentTimeMillis(), 1));

        ParallelFileProcessor.ProcessingResult result = processor.processFilesParallel(
                files, 3, Set.of(), 3
        );

        assertEquals(5, result.processedFiles);
        assertTrue(result.wordCounts.containsKey("common"));
        assertEquals(5, result.wordCounts.get("common"));
        assertTrue(result.errors.isEmpty());
    }

    @Test
    @DisplayName("Parallel processing handles partial failures")
    void processFilesParallel_PartialFailures_ContinuesOthers() throws Exception {
        Path goodFile = tempDir.resolve("good.txt");
        Path badFile = tempDir.resolve("bad.txt");
        Files.writeString(goodFile, "good content");
        Files.writeString(badFile, "bad content");

        when(analysisService.processFile(eq(goodFile.toFile()), eq(3), anySet()))
                .thenReturn(Map.of("good", 1));
        when(analysisService.processFile(eq(badFile.toFile()), eq(3), anySet()))
                .thenThrow(new IOException("Cannot read file"));

        ParallelFileProcessor.ProcessingResult result = processor.processFilesParallel(
                List.of(goodFile, badFile), 3, Set.of(), 2
        );

        assertEquals(1, result.processedFiles);
        assertEquals(1, result.errors.size());
        assertEquals(1, result.wordCounts.get("good"));
    }
}