package com.leastfixedpoint.csv;

import java.io.IOException;

public class CsvSyntaxError extends IOException {
    public final int row;
    public final int column;

    public CsvSyntaxError(String message, int row, int column) {
        super(message);
        this.row = row;
        this.column = column;
    }
}
