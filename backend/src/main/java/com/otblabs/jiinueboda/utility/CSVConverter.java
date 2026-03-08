package com.otblabs.jiinueboda.utility;

import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@Component
public class CSVConverter {

    /**
     * Converts a list of objects to CSV string
     * @param objects List of objects to convert
     * @param <T> Type of objects
     * @return CSV formatted string
     */
    public <T> String convertToCSV(List<T> objects) {
        if (objects == null || objects.isEmpty()) {
            return "";
        }

        StringBuilder csv = new StringBuilder();

        // Get all fields from the first object
        T firstObject = objects.get(0);
        Field[] fields = firstObject.getClass().getDeclaredFields();

        // Make fields accessible
        for (Field field : fields) {
            field.setAccessible(true);
        }

        // Create header row
        String header = Arrays.stream(fields)
                .map(Field::getName)
                .collect(Collectors.joining(","));
        csv.append(header).append("\n");

        // Create data rows
        for (T obj : objects) {
            String row = Arrays.stream(fields)
                    .map(field -> {
                        try {
                            Object value = field.get(obj);
                            return escapeCSV(value != null ? value.toString() : "");
                        } catch (IllegalAccessException e) {
                            return "";
                        }
                    })
                    .collect(Collectors.joining(","));
            csv.append(row).append("\n");
        }

        return csv.toString();
    }

    /**
     * Escapes special characters in CSV values
     */
    private String escapeCSV(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        // If value contains comma, quote, newline, or starts with special chars, wrap in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") ||
                value.contains("\r") || value.startsWith(" ") || value.endsWith(" ")) {
            // Escape quotes by doubling them
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
