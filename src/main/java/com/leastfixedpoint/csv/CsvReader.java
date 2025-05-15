package com.leastfixedpoint.csv;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CsvReader implements Iterable<String[]> {
    private final PushbackReader r;

    private boolean lenientQuotes = false;

    private int rowNumber = 0;

    public CsvReader(Reader inner) {
        this.r = new PushbackReader(inner);
    }

    public String[][] rows() throws IOException {
        ArrayList<String[]> rowList = new ArrayList<>();
        String[] row;
        while ((row = nextRow()) != null) {
            rowList.add(row);
        }
        return rowList.toArray(new String[rowList.size()][]);
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

    private static boolean isEndOfLine(char c) {
        return c == '\r' || c == '\n';
    }

    public String[] nextRow() throws IOException {
        ArrayList<String> currentRow = null;
        StringBuilder currentCell = null;
        boolean inQuote = false;

        rowLoop: while (true) {
            final int ch = r.read();
            if (ch == '\uFEFF') {
                // Skip BOM.
                continue;
            }

            if (ch == -1) {
                if (!lenientQuotes && inQuote) {
                    throw new CsvSyntaxError("Unclosed double-quote", rowNumber, currentRow.size());
                }
                if (currentCell != null) {
                    currentRow.add(currentCell.toString());
                    currentCell = null;
                }
                break rowLoop;
            }
            
            if (currentRow == null) currentRow = new ArrayList<String>();
            if (currentCell == null) currentCell = new StringBuilder();
            
            if (inQuote) {
                if (ch == '"') {
                    if (peek() == '"') {
                        r.read();
                        currentCell.append((char) (int) '"');
                    } else {
                        inQuote = false;
                        if (!lenientQuotes) {
                            int next = peek();
                            if (next != -1 && next != ',' && !isEndOfLine((char) next)) {
                                throw new CsvSyntaxError("Illegal closing double-quote position", rowNumber, currentRow.size());
                            }
                        }
                    }
                } else {
                    currentCell.append((char) ch);
                }
            } else {
                switch (ch) {
                    case '"':
                        if (!lenientQuotes && currentCell.length() != 0) {
                            throw new CsvSyntaxError("Illegal opening double-quote position", rowNumber, currentRow.size());
                        }
                        inQuote = true;
                        break;
                    case ',':
                        if (currentCell != null) {
                            currentRow.add(currentCell.toString());
                            currentCell = null;
                        }
                        break;
                    case '\r':
                        if (peek() == '\n') r.read();
                        // fall through
                    case '\n':
                        if (currentCell != null) {
                            currentRow.add(currentCell.toString());
                            currentCell = null;
                        }
                        break rowLoop;
                    default:
                        currentCell.append((char) ch);
                        break;
                }
            }
        }

        if (currentRow != null) {
            rowNumber++;
            return currentRow.toArray(new String[currentRow.size()]);
        } else {
            return null;
        }
    }

    @Override
    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {
            private String[] current = null;

            private void loadNext() {
                if (current == null) {
                    try {
                        current = CsvReader.this.nextRow();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            @Override
            public boolean hasNext() {
                loadNext();
                return (current != null);
            }

            @Override
            public String[] next() {
                loadNext();
                if (current == null) throw new NoSuchElementException();
                final var result = current;
                current = null;
                return result;
            }
        };
    }
}
