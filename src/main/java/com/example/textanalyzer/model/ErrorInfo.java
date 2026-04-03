package com.example.textanalyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an error that occurred during file processing.
 *
 * <p>Contains information about which file caused the error and the
 * associated error message for debugging and reporting purposes.
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
public class ErrorInfo {
    private String file;
    private String message;

    /**
     * Default constructor required for JSON deserialization.
     */
    public ErrorInfo() {
    }

    /**
     * Constructs an error info object with file path and error message.
     *
     * @param file The path to the file that caused the error
     * @param message The error description
     */
    public ErrorInfo(String file, String message) {
        this.file = file;
        this.message = message;
    }

    /**
     * Returns the file path associated with this error.
     *
     * @return The file path as a string
     */
    @JsonProperty("file")
    public String getFile() {
        return file;
    }

    /**
     * Sets the file path for this error.
     *
     * @param file The file path
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Returns the error message.
     *
     * @return The error description
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message.
     *
     * @param message The error description
     */
    public void setMessage(String message) {
        this.message = message;
    }
}