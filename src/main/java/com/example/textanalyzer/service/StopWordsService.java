package com.example.textanalyzer.service;

import java.util.Set;

public interface StopWordsService {
    Set<String> loadStopWords(String stopwordsPath);
}