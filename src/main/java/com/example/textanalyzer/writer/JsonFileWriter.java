package com.example.textanalyzer.writer;

import com.example.textanalyzer.model.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Writes analysis results to a JSON file with pretty-printed formatting.
 *
 * <p>This writer automatically creates parent directories if they don't exist
 * and produces indented JSON for human readability.
 *
 * @author Text Analyzer Team
 * @version 1.0
 */
@Component
public class JsonFileWriter implements ResultWriter {
    private static final Logger logger = LoggerFactory.getLogger(JsonFileWriter.class);
    private final ObjectMapper objectMapper;

    /**
     * Constructs a JsonFileWriter with a configured ObjectMapper.
     *
     * <p>The ObjectMapper is configured with {@link SerializationFeature#INDENT_OUTPUT}
     * to produce pretty-printed JSON.
     */
    public JsonFileWriter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Default write method that logs a warning.
     *
     * <p>Use {@link #write(AnalysisResult, String)} instead to specify an output path.
     *
     * @param result The analysis result (ignored)
     */
    @Override
    public void write(AnalysisResult result) {
        logger.warn("write() called without output path. Use write(result, outputPath) instead.");
    }

    /**
     * Writes the analysis result to a JSON file at the specified path.
     *
     * <p>Creates parent directories automatically if they don't exist.
     * If an error occurs during writing, it is logged but not thrown.
     *
     * @param result The analysis result to write
     * @param outputPath The file path where the JSON should be saved
     */
    public void write(AnalysisResult result, String outputPath) {
        if (outputPath == null || outputPath.trim().isEmpty()) {
            logger.error("Output path not specified for JSON writer");
            return;
        }

        try {
            Path path = Paths.get(outputPath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            objectMapper.writeValue(path.toFile(), result);
            logger.info("Results written to JSON file: {}", outputPath);
        } catch (IOException e) {
            logger.error("Error writing JSON file: {}", outputPath, e);
        }
    }
}