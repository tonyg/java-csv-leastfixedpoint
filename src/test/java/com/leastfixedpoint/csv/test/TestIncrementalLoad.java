package com.leastfixedpoint.csv.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.leastfixedpoint.csv.CsvReader;

public class TestIncrementalLoad {
    public static final String INPUT = "a,b\nc,d\ne,f\n";

    @Test
    public void testBulkLoad() throws IOException {
        final var r = new CsvReader(new StringReader(INPUT)).rows();
        assertEquals(3, r.length);
        assertEquals(2, r[0].length);
        assertEquals(2, r[1].length);
        assertEquals(2, r[2].length);
    }

    @Test
    public void testIncrementalLoad() throws IOException {
        final var r = new CsvReader(new StringReader(INPUT));
        int i = 0;
        for (var row : r) {
            assertEquals(2, row.length);
            i++;
        }
        assertEquals(i, 3);
    }
}