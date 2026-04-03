package com.example.textanalyzer.service;

import com.example.textanalyzer.model.AnalysisResult;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface TextAnalysisService {
    AnalysisResult analyzeDirectory(String dirPath, int minLength, int topCount, Set<String> stopWords);
    Map<String, Integer> processFile(java.io.File file, int minLength, Set<String> stopWords) throws IOException;
}