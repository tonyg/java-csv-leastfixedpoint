package com.leastfixedpoint.csv.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.leastfixedpoint.csv.CsvReader;
import com.leastfixedpoint.csv.CsvSyntaxError;

public class TestLenientMode {
    private String[][] lenientLoad(String s) throws IOException {
        try (final var r = new StringReader(s)) {
            final var csvr = new CsvReader(r);
            csvr.setLenientQuotes(true);
            return csvr.rows();
        }
    }
   
    private String[][] strictLoad(String s) throws IOException {
        try (final var r = new StringReader(s)) {
            final var csvr = new CsvReader(r);
            return csvr.rows();
        }
    }
   
    @Test
    public void testStrictOK1() throws IOException {
        var rows = strictLoad("a,\"bcd\",e");
        assertEquals(1, rows.length);
        assertEquals(3, rows[0].length);
        assertEquals("a", rows[0][0]);
        assertEquals("bcd", rows[0][1]);
        assertEquals("e", rows[0][2]);
    }
   
    @Test
    public void testStrictOK2() throws IOException {
        var rows = strictLoad("a,\"bcd\"");
        assertEquals(1, rows.length);
        assertEquals(2, rows[0].length);
        assertEquals("a", rows[0][0]);
        assertEquals("bcd", rows[0][1]);
    }
   
    @Test
    public void testStrictOK3() throws IOException {
        var rows = strictLoad("a,\"bcd\"\ne");
        assertEquals(2, rows.length);
        assertEquals(2, rows[0].length);
        assertEquals("a", rows[0][0]);
        assertEquals("bcd", rows[0][1]);
        assertEquals(1, rows[1].length);
        assertEquals("e", rows[1][0]);
    }
   
    @Test
    public void testLenientQuoting1() throws IOException {
        var rows = lenientLoad("a,b\"\"\"\"d,e");
        assertEquals(1, rows.length);
        assertEquals(3, rows[0].length);
        assertEquals("a", rows[0][0]);
        assertEquals("b\"d", rows[0][1]);
        assertEquals("e", rows[0][2]);
    }
   
    @Test
    public void testLenientQuoting2() throws IOException {
        var rows = lenientLoad("a,b\"\"d,e");
        assertEquals(1, rows.length);
        assertEquals(3, rows[0].length);
        assertEquals("a", rows[0][0]);
        assertEquals("bd", rows[0][1]);
        assertEquals("e", rows[0][2]);
    }
   
    @Test
    public void testLenientOpenAndClose() throws IOException {
        var rows = lenientLoad("a,b\"c\"d,e");
        assertEquals(1, rows.length);
        assertEquals(3, rows[0].length);
        assertEquals("a", rows[0][0]);
        assertEquals("bcd", rows[0][1]);
        assertEquals("e", rows[0][2]);
    }

    @Test
    public void testStrictOpenAndClose() throws IOException {
        try {
            strictLoad("a,b\"c\"d,e");
            fail();
        } catch (CsvSyntaxError cse) {
            assertEquals("Illegal opening double-quote position", cse.getMessage());
            assertEquals(0, cse.row);
            assertEquals(1, cse.column);
        }
    }
   
    @Test
    public void testStrictOpen() throws IOException {
        try {
            strictLoad("a,b\"cd\",e");
            fail();
        } catch (CsvSyntaxError cse) {
            assertEquals("Illegal opening double-quote position", cse.getMessage());
            assertEquals(0, cse.row);
            assertEquals(1, cse.column);
        }
    }
   
    @Test
    public void testStrictClose1() throws IOException {
        try {
            strictLoad("a,\"bc\"d,e");
            fail();
        } catch (CsvSyntaxError cse) {
            assertEquals("Illegal closing double-quote position", cse.getMessage());
            assertEquals(0, cse.row);
            assertEquals(1, cse.column);
        }
    }
   
    @Test
    public void testStrictClose2() throws IOException {
        try {
            strictLoad("x\ny\nz\na,\"bc\" ,e");
            fail();
        } catch (CsvSyntaxError cse) {
            assertEquals("Illegal closing double-quote position", cse.getMessage());
            assertEquals(3, cse.row);
            assertEquals(1, cse.column);
        }
    }
   
    @Test
    public void testStrictClose2b() throws IOException {
        try {
            strictLoad("x\ny\nz\na,\"bc\" e,e");
            fail();
        } catch (CsvSyntaxError cse) {
            assertEquals("Illegal closing double-quote position", cse.getMessage());
            assertEquals(3, cse.row);
            assertEquals(1, cse.column);
        }
    }

    @Test
    public void testLenientClose2b() throws IOException {
        var rows = lenientLoad("x\ny\nz\na,\"bc\" e,e");
        assertEquals(4, rows.length);
        assertEquals(3, rows[3].length);
        assertEquals("bc e", rows[3][1]);
    }
   
    @Test
    public void testLenientMissingClose() throws IOException {
        var rows = lenientLoad("a,b\"cd,e\nf");
        assertEquals(1, rows.length);
        assertEquals(2, rows[0].length);
        assertEquals("a", rows[0][0]);
        assertEquals("bcd,e\nf", rows[0][1]);
    }
}
