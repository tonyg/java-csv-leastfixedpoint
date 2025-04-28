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
import com.leastfixedpoint.json.JSONReader;

/// Tests cases drawn from the csv-spectrum repository
public class TestCsvSpectrum {
    @SuppressWarnings("unchecked")
    @Test
    public void testCsvSpectrum() throws IOException {
        final var csvsDir = new File("test-data/csv-spectrum-master/csvs");
        assertTrue(csvsDir.isDirectory());
        final var csvs = csvsDir.listFiles();
        for (var csvFilename : csvs) {
            final var fullBasename = csvFilename.getName();
            if (fullBasename.endsWith(".csv")) {
                final var baseName = fullBasename.substring(0, fullBasename.length() - 4);
                if (baseName.equals("location_coordinates")) {
                    // It's bogus. See:
                    // https://github.com/max-mapper/csv-spectrum/issues/20
                    // https://github.com/max-mapper/csv-spectrum/issues/21
                    // https://github.com/max-mapper/csv-spectrum/issues/22
                    continue;
                }

                final var jsonFilename = new File("test-data/csv-spectrum-master/json", baseName + ".json");
                List<Map<String, String>> expected = null;
                try (final var r = new FileReader(jsonFilename)) {
                    final var obj = JSONReader.readFrom(r);
                    try {
                        expected = (List<Map<String, String>>) obj;
                    } catch (ClassCastException cce) {
                        fail("Unexpected JSON input for " + baseName + ": " + obj);
                    }
                }

                try (final var r = new FileReader(csvFilename)) {
                    final var rows = new CsvReader(r).rows();
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
                }
            }
        }
    }
}
