package com.example.textanalyzer.writer;

import com.example.textanalyzer.model.AnalysisResult;

/**
 * Interface for writing analysis results to various output destinations.
 *
 * <p>Implementations can write to console, files, databases, or other
 * output streams. This abstraction allows easy addition of new output formats.
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
public interface ResultWriter {

    /**
     * Writes the analysis result to the target destination.
     *
     * <p>The exact behavior depends on the implementation:
     * <ul>
     *   <li>{@link ConsoleWriter} - Prints formatted output to System.out</li>
     *   <li>{@link JsonFileWriter} - Requires additional path parameter;
     *       calling this method will log a warning</li>
     * </ul>
     *
     * @param result The analysis result to write
     */
    void write(AnalysisResult result);
}