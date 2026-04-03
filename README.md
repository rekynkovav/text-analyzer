# Text Analyzer

A Spring Boot console application for analyzing word frequencies in text files.

## Features

- Analyze all `.txt` files in a directory
- Filter words by minimum length
- Exclude stop words from analysis
- Output results to console or JSON file
- Handle errors gracefully (missing files, access issues, etc.)
- Configurable logging

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Building the Project

```bash
mvn clean package