package com.vsv.utils;

import androidx.annotation.NonNull;

public final class StringUtils {

    private static final StringBuilder stringBuilder = new StringBuilder();

    private StringUtils() {

    }

    public static String generateSameSymbolsString(@NonNull String symbol, int count) {
        if (symbol.isEmpty()) {
            throw new RuntimeException("Symbol cannot be empty to generate the string from it");
        }
        stringBuilder.setLength(0);
        for (int i = 0; i < count; i++) {
            stringBuilder.append(symbol);
        }
        return stringBuilder.toString();
    }
}
