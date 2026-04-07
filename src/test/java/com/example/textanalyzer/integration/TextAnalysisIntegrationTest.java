package com.example.textanalyzer.integration;

import com.example.textanalyzer.config.ProcessorConfig;
import com.example.textanalyzer.processor.ParallelFileProcessor;
import com.example.textanalyzer.processor.WordProcessor;
import com.example.textanalyzer.service.StopWordsServiceImpl;
import com.example.textanalyzer.service.TextAnalysisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("End-to-End Integration Tests")
class TextAnalysisIntegrationTest {

    private TextAnalysisServiceImpl textAnalysisService;
    private ParallelFileProcessor parallelFileProcessor;
    private WordProcessor wordProcessor;
    private StopWordsServiceImpl stopWordsService;
    private ProcessorConfig config;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        wordProcessor = new WordProcessor();
        textAnalysisService = new TextAnalysisServiceImpl(wordProcessor);
        config = new ProcessorConfig();
        config.setExecutorTimeoutSeconds(120);
        parallelFileProcessor = new ParallelFileProcessor(textAnalysisService, config);
        stopWordsService = new StopWordsServiceImpl();
    }

    @Test
    @DisplayName("Full pipeline: process multiple files with stop words")
    void fullPipeline_MultipleFilesWithStopWords_CorrectResults() throws Exception {
        Path file1 = tempDir.resolve("doc1.txt");
        Path file2 = tempDir.resolve("doc2.txt");

        Files.writeString(file1, "The quick brown fox jumps over the lazy dog");
        Files.writeString(file2, "The dog runs quickly and the cat sleeps");

        Path stopwordsFile = tempDir.resolve("stopwords.txt");
        Files.writeString(stopwordsFile, "the\nand\nover\n");
        Set<String> stopWords = stopWordsService.loadStopWords(stopwordsFile.toString());

        List<Path> files = List.of(file1, file2);
        ParallelFileProcessor.ProcessingResult result = parallelFileProcessor.processFilesSequential(
                files, 3, stopWords
        );

        assertEquals(2, result.processedFiles);
        assertTrue(result.wordCounts.containsKey("quick"));
        assertTrue(result.wordCounts.containsKey("brown"));
        assertTrue(result.wordCounts.containsKey("fox"));
        assertTrue(result.wordCounts.containsKey("jumps"));
        assertTrue(result.wordCounts.containsKey("lazy"));
        assertTrue(result.wordCounts.containsKey("dog"));
        assertTrue(result.wordCounts.containsKey("runs"));
        assertTrue(result.wordCounts.containsKey("quickly"));
        assertTrue(result.wordCounts.containsKey("cat"));
        assertTrue(result.wordCounts.containsKey("sleeps"));

        assertFalse(result.wordCounts.containsKey("the"));
        assertFalse(result.wordCounts.containsKey("and"));
        assertFalse(result.wordCounts.containsKey("over"));

        assertEquals(2, result.wordCounts.get("dog"));
    }

    @Test
    @DisplayName("Sequential vs Parallel produce same results")
    void sequentialVsParallel_ConsistentResults() throws Exception {
        for (int i = 0; i < 10; i++) {
            Path file = tempDir.resolve("file" + i + ".txt");
            Files.writeString(file, "test word sample content " + i);
        }

        List<Path> files = Files.list(tempDir)
                .filter(p -> p.toString().endsWith(".txt"))
                .toList();

        ParallelFileProcessor.ProcessingResult sequentialResult =
                parallelFileProcessor.processFilesSequential(files, 3, Set.of());

        ParallelFileProcessor.ProcessingResult parallelResult =
                parallelFileProcessor.processFilesParallel(files, 3, Set.of(), 4);

        assertEquals(sequentialResult.wordCounts.size(), parallelResult.wordCounts.size());
        assertEquals(sequentialResult.processedFiles, parallelResult.processedFiles);

        for (String word : sequentialResult.wordCounts.keySet()) {
            assertEquals(sequentialResult.wordCounts.get(word), parallelResult.wordCounts.get(word));
        }
    }
}