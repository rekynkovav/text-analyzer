package com.example.textanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Represents the complete analysis result containing word frequencies,
 * analysis metadata, and any errors encountered during processing.
 *
 * <p>This class encapsulates all output data from the text analysis,
 * including the top words by frequency, analysis configuration parameters,
 * and error information for failed file operations.
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
public class AnalysisResult {
    private Map<String, Object> analysisInfo;
    private List<WordCount> words;
    private List<ErrorInfo> errors;

    /**
     * Default constructor required for JSON deserialization.
     */
    public AnalysisResult() {
    }

    /**
     * Constructs a complete analysis result with all components.
     *
     * @param analysisInfo Map containing analysis metadata (directory, minLength, topCount)
     * @param words List of word frequency objects, sorted descending by count
     * @param errors List of errors encountered during file processing
     */
    public AnalysisResult(Map<String, Object> analysisInfo,
                          List<WordCount> words,
                          List<ErrorInfo> errors) {
        this.analysisInfo = analysisInfo;
        this.words = words;
        this.errors = errors;
    }

    /**
     * Returns the analysis metadata.
     *
     * @return Map containing configuration parameters like directory path,
     *         minimum word length, and top count limit
     */
    @JsonProperty("analysisInfo")
    public Map<String, Object> getAnalysisInfo() {
        return analysisInfo;
    }

    /**
     * Sets the analysis metadata.
     *
     * @param analysisInfo Map containing analysis configuration
     */
    public void setAnalysisInfo(Map<String, Object> analysisInfo) {
        this.analysisInfo = analysisInfo;
    }

    /**
     * Returns the list of top words with their frequencies.
     *
     * @return List of WordCount objects in descending frequency order
     */
    @JsonProperty("words")
    public List<WordCount> getWords() {
        return words;
    }

    /**
     * Sets the list of top words.
     *
     * @param words List of WordCount objects
     */
    public void setWords(List<WordCount> words) {
        this.words = words;
    }

    /**
     * Returns the list of processing errors.
     *
     * @return List of ErrorInfo objects detailing file processing failures
     */
    @JsonProperty("errors")
    public List<ErrorInfo> getErrors() {
        return errors;
    }

    /**
     * Sets the list of processing errors.
     *
     * @param errors List of ErrorInfo objects
     */
    public void setErrors(List<ErrorInfo> errors) {
        this.errors = errors;
    }
}