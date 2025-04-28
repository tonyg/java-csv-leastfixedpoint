package com.leastfixedpoint.csv;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;

public class CsvReader {
    private final PushbackReader r;

    private ArrayList<String[]> rowList = new ArrayList<>();
    private String[][] answer = null;

    private boolean inQuote = false;
    private ArrayList<String> currentRow = null;
    private StringBuilder currentCell = null;

    private boolean lenientQuotes = false;

    public CsvReader(Reader inner) {
        this.r = new PushbackReader(inner);
    }

    public String[][] rows() throws IOException {
        if (this.rowList != null) {
            this.read();
            answer = rowList.toArray(new String[rowList.size()][]);
            rowList = null;
        }
        return this.answer;
    }

    public boolean getLenientQuotes() {
        return lenientQuotes;
    }

    /// RFC 4180 requires quotes to appear immediately at cell start and end.
    /// We support a "lenient" mode where quotes may start anywhere in a cell
    /// to begin escaping commas.
    public void setLenientQuotes(boolean newLenientQuotes) {
        lenientQuotes = newLenientQuotes;
    }

    private int peek() throws IOException {
        int ch = r.read();
        if (ch != -1) r.unread(ch);
        return ch;
    }

    private void emitChar(int ch) {
        currentCell.append((char) ch);
    }

    private void emitCell() {
        if (currentCell != null) {
            currentRow.add(currentCell.toString());
            currentCell = null;
        }
    }

    private void emitRow() {
        if (currentRow != null) {
            emitCell();
            var row = new String[currentRow.size()];
            rowList.add(currentRow.toArray(row));
            currentRow = null;
        }
    }

    private void read() throws IOException {
        while (true) {
            final int ch = r.read();
            if (ch == -1) {
                if (!lenientQuotes && inQuote) {
                    syntaxError("Unclosed double-quote");
                }
                emitRow();
                return;
            }
            
            if (currentRow == null) currentRow = new ArrayList<>();
            if (currentCell == null) currentCell = new StringBuilder();
            
            if (inQuote) {
                if (ch == '"') {
                    if (peek() == '"') {
                        r.read();
                        emitChar('"');
                    } else {
                        inQuote = false;
                        if (!lenientQuotes) {
                            int next = peek();
                            if (next != -1 && next != ',' && !Character.isWhitespace((char) next)) {
                                syntaxError("Illegal closing double-quote position");
                            }
                        }
                    }
                } else {
                    emitChar(ch);
                }
            } else {
                switch (ch) {
                    case '"':
                        if (!lenientQuotes && currentCell.length() != 0) {
                            syntaxError("Illegal opening double-quote position");
                        }
                        inQuote = true;
                        break;
                    case ',':
                        emitCell();
                        break;
                    case '\r':
                        if (peek() == '\n') r.read();
                        // fall through
                    case '\n':
                        emitRow();
                        break;
                    default:
                        emitChar(ch);
                        break;
                }
            }
        }
    }

    private void syntaxError(String string) throws CsvSyntaxError {
        throw new CsvSyntaxError(string, rowList.size(), currentRow.size());
    }
}
