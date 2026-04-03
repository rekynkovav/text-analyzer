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

@Component
public class JsonFileWriter implements ResultWriter {
    private static final Logger logger = LoggerFactory.getLogger(JsonFileWriter.class);
    private final ObjectMapper objectMapper;

    public JsonFileWriter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void write(AnalysisResult result) {
        logger.warn("write() called without output path. Use write(result, outputPath) instead.");
    }

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