package com.example.textanalyzer.service;

import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.model.ErrorInfo;
import com.example.textanalyzer.model.WordCount;
import com.example.textanalyzer.processor.WordProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
public class TextAnalysisServiceImpl implements TextAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(TextAnalysisServiceImpl.class);
    private final WordProcessor wordProcessor;

    public TextAnalysisServiceImpl(WordProcessor wordProcessor) {
        this.wordProcessor = wordProcessor;
    }

    @Override
    public AnalysisResult analyzeDirectory(String dirPath, int minLength, int topCount, Set<String> stopWords) {
        Map<String, Integer> totalWordCounts = new HashMap<>();
        List<ErrorInfo> errors = new ArrayList<>();

        Path directory = Paths.get(dirPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            logger.error("Directory does not exist: {}", dirPath);
            errors.add(new ErrorInfo(dirPath, "Directory does not exist"));
            return createEmptyResult(dirPath, minLength, topCount, errors);
        }

        try {
            List<Path> txtFiles = Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                    .collect(Collectors.toList());

            if (txtFiles.isEmpty()) {
                logger.warn("No .txt files found in directory: {}", dirPath);
            }

            for (Path filePath : txtFiles) {
                try {
                    Map<String, Integer> fileWordCounts = processFile(filePath.toFile(), minLength, stopWords);
                    fileWordCounts.forEach((word, count) ->
                            totalWordCounts.merge(word, count, Integer::sum)
                    );
                } catch (IOException e) {
                    logger.error("Error processing file: {}", filePath, e);
                    errors.add(new ErrorInfo(filePath.toString(), e.getMessage()));
                }
            }
        } catch (IOException e) {
            logger.error("Error walking directory: {}", dirPath, e);
            errors.add(new ErrorInfo(dirPath, "Error accessing directory: " + e.getMessage()));
        }

        List<WordCount> topWords = getTopWords(totalWordCounts, topCount);
        Map<String, Object> analysisInfo = createAnalysisInfo(dirPath, minLength, topCount);

        return new AnalysisResult(analysisInfo, topWords, errors);
    }

    @Override
    public Map<String, Integer> processFile(java.io.File file, int minLength, Set<String> stopWords) throws IOException {
        Map<String, Integer> wordCounts = new HashMap<>();
        String content = Files.readString(file.toPath());

        if (content.trim().isEmpty()) {
            logger.debug("Empty file: {}", file.getName());
            return wordCounts;
        }

        Matcher matcher = wordProcessor.getWordMatcher(content);

        while (matcher.find()) {
            String rawWord = matcher.group();
            String normalizedWord = wordProcessor.normalizeWord(rawWord);

            if (wordProcessor.isValidWord(normalizedWord, minLength) &&
                !stopWords.contains(normalizedWord)) {
                wordCounts.merge(normalizedWord, 1, Integer::sum);
            }
        }

        logger.debug("Processed file: {} - found {} unique words", file.getName(), wordCounts.size());
        return wordCounts;
    }

    private List<WordCount> getTopWords(Map<String, Integer> wordCounts, int topCount) {
        return wordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topCount)
                .map(entry -> new WordCount(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private AnalysisResult createEmptyResult(String dirPath, int minLength, int topCount, List<ErrorInfo> errors) {
        Map<String, Object> analysisInfo = createAnalysisInfo(dirPath, minLength, topCount);
        return new AnalysisResult(analysisInfo, Collections.emptyList(), errors);
    }

    private Map<String, Object> createAnalysisInfo(String dirPath, int minLength, int topCount) {
        Map<String, Object> info = new HashMap<>();
        info.put("directory", dirPath);
        info.put("minWordLength", minLength);
        info.put("topCount", topCount);
        return info;
    }
}