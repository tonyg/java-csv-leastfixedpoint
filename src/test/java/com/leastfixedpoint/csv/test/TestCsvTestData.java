package com.leastfixedpoint.csv.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.leastfixedpoint.csv.CsvReader;
import com.leastfixedpoint.csv.CsvSyntaxError;
import com.leastfixedpoint.json.JSONReader;

/// Tests cases drawn from the csv-test-data repository
public class TestCsvTestData {
    private String[][] readCsvFile(File f) throws IOException {
        try (final var r = new FileReader(f)) {
            return new CsvReader(r).rows();
        }
    }

    @Test
    public void testCsvTestData() throws IOException {
        final var csvsDir = new File("test-data/csv-test-data-master/csv");
        assertTrue(csvsDir.isDirectory());
        final var csvs = csvsDir.listFiles();
        for (var csvFilename : csvs) {
            final var fullBasename = csvFilename.getName();
            if (fullBasename.endsWith(".csv")) {
                final var baseName = fullBasename.substring(0, fullBasename.length() - 4);

                if (baseName.startsWith("bad-")) {
                    try {
                        final var rows = readCsvFile(csvFilename);
                        switch (baseName) {
                            case "bad-header-no-header":
                                assertEquals(0, rows.length, "Special case for bad-header-no-header");
                                break;
                            case "bad-header-more-fields":
                            case "bad-header-less-fields":
                            case "bad-header-wrong-header":
                                break;
                            default:
                                fail("Unexpected pass in test " + baseName);
                        }
                    } catch (CsvSyntaxError csve) {
                        // OK
                    }
                    continue;
                }

                final var rows = readCsvFile(csvFilename);

                final var jsonFilename = new File("test-data/csv-test-data-master/json", baseName + ".json");
                Object jsonObj = null;
                try (var r = new FileReader(jsonFilename)) {
                    jsonObj = JSONReader.readFrom(r);
                }

                if (baseName.startsWith("header-")) {
                    @SuppressWarnings("unchecked")
                    var expected = (List<Map<String, String>>) jsonObj;
                    final var header = rows[0];
                    for (int i = 1; i < rows.length; i++) {
                        final var row = rows[i];
                        final var expectedObject = expected.get(i - 1); // no header in JSON
                        for (int j = 0; j < Math.min(row.length, header.length); j++) {
                            final var key = header[j];
                            assertEquals(
                                expectedObject.get(key),
                                row[j],
                                () -> "Test case " + baseName + ", key " + key);
                        }
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    var expected = (List<List<String>>) jsonObj;
                    for (int i = 0; i < rows.length; i++) {
                        final var row = rows[i];
                        final var expectedRow = expected.get(i);
                        assertEquals(expectedRow.size(), row.length, "Test case " + baseName + " row " + i);
                        for (int j = 0; j < row.length; j++) {
                            assertEquals(
                                expectedRow.get(j),
                                row[j],
                                "Test case " + baseName + " row " + i + " column " + j);
                        }
                    }
                }
            }
        }
    }
}
