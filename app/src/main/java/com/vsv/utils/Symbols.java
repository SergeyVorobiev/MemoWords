package com.vsv.utils;

public final class Symbols {

    public static final String ROCKET = "🚀";

    public static final String EDUCATION = "🎓";

    public static final String graph = "📊";

    public static final String SHEET = "📄";

    public static final String SCORE = "✨";

    public static final String BOOKS = "📚";

    public static final String PUZZLE = "🧩";

    public static final String USER = "🧑";

    public static final String SEARCH = "🔎";

    public static final String ID = "🆔";

    public static final String N_1 = "\uD83C\uDF2A";

    public static final String N_2 = "\uD83C\uDF00";

    public static final String N_3 = "\uD83D\uDD25";

    public static final String N_4 = "\uD83E\uDD96";

    public static final String N_5 = "\uD83C\uDF1F";

    public static final String N_6 = "⭐";

    public static final String N_7 = "\uD83C\uDF53";

    public static final String N_8 = "\uD83E\uDD16";

    public static final String N_9 = "\uD83C\uDF83";

    public static final String N_10 = "✨";

    public static final String N_11 = "\uD83D\uDCA5";

    public static final String N_12 = "\uD83D\uDCAB";

    public static final String N_13 = "\uD83D\uDD25";

    public static final String N_14 = "\uD83C\uDF3A";

    public static final String N_15 = "\uD83E\uDEE7";

    public static final String N_16 = "\uD83C\uDF0A";

    public static final String N_17 = "\uD83C\uDF6C";

    public static final String N_18 = "\uD83C\uDF88";

    public static final String N_19 = "\uD83D\uDC8E";

    public static final String N_20 = "❤";

    public static final String[] nameSymbols = new String[] {N_1, N_2, N_3, N_4, N_5, N_6, N_7, N_8,
            N_9, N_10, N_11, N_12, N_13, N_14, N_15, N_16, N_17, N_18, N_19, N_20};

    private Symbols() {

    }

    public static String getRandomNameSymbol() {
        return Symbols.nameSymbols[StaticUtils.random.nextInt(nameSymbols.length)];
    }
}
