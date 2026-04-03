package com.example.textanalyzer.writer;

import com.example.textanalyzer.model.AnalysisResult;

public interface ResultWriter {
    void write(AnalysisResult result);
}