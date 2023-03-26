package com.vsv.utils;

public final class Spec {

    public static final String DICTIONARY_RANGE = "A1:M";

    public static final String NOTEBOOK_RANGE = "A1:H";

    public static final String SPREADSHEETS_RANGE = "A1:C";

    public static final String PRESETS_RANGE = "A1:C";

    public static final int MAX_PRESETS = 1000;

    public static final int MAX_SAMPLES = 5000;

    public static final int MAX_SAMPLES_WITH_HEADER = MAX_SAMPLES + 1;

    public static final int MAX_SPREADSHEETS = 1000;

    public static final int MAX_SPREADSHEETS_TO_LOAD = 1000;

    public static final int MAX_DICTIONARIES = 100;

    public static final int MAX_SHELVES = 100;

    public static final int MAX_NOTEBOOKS = 200;

    public static final int MAX_NOTES = 500;

    public static final int MAX_NOTES_WITH_HEADER = MAX_NOTES + 1;

    public static final int TIME_TO_DICTIONARY_CHECK_UPDATE = 60 * 12; // Minutes

    public static final int MIN_SCROLL_HIT = 25;
    private Spec() {

    }
}
