package com.leastfixedpoint.csv.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.leastfixedpoint.csv.CsvReader;

public class TestBOM {
    @Test
    void testBOM() throws IOException {
        final var r = new CsvReader(new StringReader("\uFEFFabc")).rows();
        assertEquals(1, r.length);
        assertEquals(1, r[0].length);
        assertEquals("abc", r[0][0]);
    }
}