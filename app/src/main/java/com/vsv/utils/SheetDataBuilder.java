package com.vsv.utils;

import android.text.Html;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;

import com.vsv.db.entities.Converter;
import com.vsv.db.entities.Note;
import com.vsv.db.entities.Sample;
import com.vsv.memorizer.R;
import com.vsv.speech.SupportedLanguages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public final class SheetDataBuilder {

    public static final int SHEET_DATA_CSV = 0; // Load sheet data from csv file

    public static final int SHEET_DATA_SAVE = 1; // Load sheet data from spreadsheet in which the data from another sheet was saved.

    public static final int NO_SHEET_DATA = 2;

    private SheetDataBuilder() {

    }

    public static final class DictData {

        public final String hash;

        public final String dictName;

        public final String leftLanguage;

        public final String rightLanguage;

        public final boolean canCopy;

        public String spreadsheetName;

        public String spreadsheetId;

        public String sheetName;

        public long sheetId;

        @Nullable
        public Date dataDate;

        @Nullable
        public Date dataSpreadsheetDate;

        @Nullable
        public String author;

        public DictData(String dictName, String leftLanguage, String rightLanguage, String spreadsheetId, String spreadsheetName,
                        String sheetName, String hash, long sheetId, boolean canCopy,
                        @Nullable Date dataDate, @Nullable Date dataSpreadsheetDate, @Nullable String author) {
            this.author = author;
            this.dictName = dictName;
            this.leftLanguage = leftLanguage;
            this.rightLanguage = rightLanguage;
            this.spreadsheetName = spreadsheetName;
            this.spreadsheetId = spreadsheetId;
            this.sheetName = sheetName;
            this.canCopy = canCopy;
            this.sheetId = sheetId;
            this.hash = hash;
            this.dataDate = dataDate;
            this.dataSpreadsheetDate = dataSpreadsheetDate;
        }
    }

    public static final class NotebookData {

        public final @NonNull
        String notebookName;

        public final boolean canCopy;

        public String spreadsheetId;

        public String spreadsheetName;

        public String sheetName;

        public long sheetId;

        public Date dataDate; // date of the current spreadsheet.

        public Date updateCheck;

        public Date spreadsheetDate; // date of the loaded spreadsheet.

        public String author;

        public NotebookData(Date dataDate, @Nullable String notebookName, @Nullable String spreadsheetId,
                            @Nullable String spreadsheetName, @Nullable String sheetName, long sheetId, boolean canCopy, String author) {
            if (notebookName == null || notebookName.isEmpty()) {
                this.notebookName = StaticUtils.getString(R.string.default_notebook_name);
            } else {
                this.notebookName = notebookName;
            }
            this.author = author;
            this.dataDate = dataDate;
            this.spreadsheetId = spreadsheetId;
            this.spreadsheetName = spreadsheetName;
            this.sheetName = sheetName;
            this.canCopy = canCopy;
            this.sheetId = sheetId;
        }
    }

    public static final class SpreadsheetData {

        public final @NonNull
        String spreadsheetId;

        public final @NonNull
        String sheetName;

        public final int type;

        public SpreadsheetData(@NonNull String spreadsheetId, @NonNull String sheetname, int type) {
            this.spreadsheetId = spreadsheetId;
            this.sheetName = sheetname;
            this.type = type;
        }
    }

    public static final int EXAMPLE_LENGTH = 1200;

    public static final int NOTE_HEADER_LENGTH = 200;

    public static final int NOTE_CONTENT_LENGTH = 20000;

    public static final int STRING_LENGTH = 200;

    public static final int KIND_LENGTH = 100;

    public static final int DICT_LENGTH = 100;

    public static final int ABB_LENGTH = 10;

    private static final String[] cache = new String[2];

    // Returns true if it filled into cache strings array, false if not.
    private static boolean fillFirstTwoValuesOrSkip(@Nullable List<?> rawLine, @SuppressWarnings("SameParameterValue") int maxLength) {
        if (rawLine == null || rawLine.size() < 2) {
            return false;
        }
        Object left = rawLine.get(0);
        Object right = rawLine.get(1);
        if (left == null || right == null) {
            return false;
        }
        String leftValue = left.toString().trim();
        String rightValue = right.toString().trim();

        // There is no a pair of words.
        if (leftValue.isEmpty() || rightValue.isEmpty()) {
            return false;
        }
        if (maxLength > 0) {
            if (leftValue.length() > maxLength) {
                leftValue = leftValue.substring(0, maxLength);
            }
            if (rightValue.length() > maxLength) {
                rightValue = rightValue.substring(0, maxLength);
            }
        }
        cache[0] = leftValue;
        cache[1] = rightValue;
        return true;
    }

    @NonNull
    public static SpreadsheetData getSpreadsheetData(@Nullable List<?> rawLine) {
        String empty = StaticUtils.getString(R.string.empty);
        if (rawLine == null) {
            return new SpreadsheetData(empty, empty, 0);
        }
        String sheetName = getCellValueOrDefault(rawLine, 1, STRING_LENGTH, empty);
        String spreadsheetId = getCellValueOrDefault(rawLine, 2, STRING_LENGTH, empty);
        String type = getCellValueOrDefault(rawLine, 3, STRING_LENGTH, "0");
        int intType = 0;
        try {
            assert type != null;
            intType = Integer.parseInt(type);
        } catch (Exception e) {
            //
        }
        assert spreadsheetId != null;
        assert sheetName != null;
        return new SpreadsheetData(spreadsheetId, sheetName, intType);
    }

    @Nullable
    public static NotebookData getNotebookData(@Nullable List<?> rawLine, @Nullable String defaultNotebookName, int loadSheetData) {
        if (rawLine == null) {
            return null;
        }
        String code = getCellValueOrEmpty(rawLine, 0, DICT_LENGTH);
        boolean canCopy;
        if (code.equals(CodeNames.NOTE)) {
            canCopy = true;
        } else if (code.equals(CodeNames.NOTE_NOT_COPY)) {
            canCopy = false;
        } else {
            return null;
        }
        String name = getCellValueOrEmpty(rawLine, 1, DICT_LENGTH);
        if (name.isEmpty()) {
            if (defaultNotebookName == null || defaultNotebookName.isEmpty()) {
                name = StaticUtils.getString(R.string.default_notebook_name);
            } else {
                name = defaultNotebookName;
            }
        }
        String spreadsheetName = "";
        String spreadsheetId = "";
        String sheetName = "";
        String stringSheetId;
        long sheetId = -1;
        Date date = DateUtils.getCurrentDate();
        Date spreadsheetDate = null;
        String author = null;
        if (loadSheetData == SHEET_DATA_CSV) { // For csv files
            spreadsheetName = getCellValueOrEmpty(rawLine, 2, STRING_LENGTH);
            spreadsheetId = getCellValueOrEmpty(rawLine, 3, STRING_LENGTH);
            date = getDateOrDefault(rawLine, 4, STRING_LENGTH, null);
            sheetName = getCellValueOrEmpty(rawLine, 5, STRING_LENGTH);
            stringSheetId = getCellValueOrEmpty(rawLine, 6, STRING_LENGTH);
            author = getCellValueOrEmpty(rawLine, 7, STRING_LENGTH);
            if (!stringSheetId.isEmpty()) {
                try {
                    sheetId = Long.parseLong(stringSheetId);
                } catch (Exception e) {
                    // Nothing.
                }
            }
            if (spreadsheetId.isEmpty() || sheetName.isEmpty()) {
                date = null;
                spreadsheetName = "";
                spreadsheetId = "";
                sheetName = "";
                sheetId = -1;
            }
        } else { // For sheet list
            if (loadSheetData == SHEET_DATA_SAVE) {
                spreadsheetName = getCellValueOrEmpty(rawLine, 2, STRING_LENGTH);
                spreadsheetId = getCellValueOrEmpty(rawLine, 3, STRING_LENGTH);
                date = getDateOrDefault(rawLine, 4, STRING_LENGTH, date);
                author = getCellValueOrEmpty(rawLine, 5, STRING_LENGTH);
                sheetName = getCellValueOrEmpty(rawLine, 6, STRING_LENGTH);
                stringSheetId = getCellValueOrEmpty(rawLine, 7, STRING_LENGTH);
                spreadsheetDate = getDateOrDefault(rawLine, 8, STRING_LENGTH, null);
                if (!stringSheetId.isEmpty()) {
                    try {
                        sheetId = Long.parseLong(stringSheetId);
                    } catch (Exception e) {
                        // Nothing.
                    }
                }
                if (spreadsheetId.isEmpty() || sheetName.isEmpty()) {
                    spreadsheetDate = null;
                    spreadsheetName = "";
                    spreadsheetId = "";
                    sheetName = "";
                    sheetId = -1;
                }
            } else { // Not load ref sheet data but we still need to load date of current sheet.
                date = getDateOrDefault(rawLine, 4, STRING_LENGTH, date);
                author = getCellValueOrEmpty(rawLine, 5, STRING_LENGTH);
            }
        }
        NotebookData data = new NotebookData(date, name, spreadsheetId, spreadsheetName, sheetName, sheetId, canCopy, author);
        data.spreadsheetDate = spreadsheetDate;
        return data;
    }

    public static boolean isNoteHeader(@Nullable List<?> rawLine) {
        if (rawLine == null) {
            return false;
        }
        String code = getCellValueOrEmpty(rawLine, 0, DICT_LENGTH);
        return code.equals(CodeNames.NOTE) || code.equals(CodeNames.NOTE_NOT_COPY);
    }

    public static boolean isSpreadsheetHeader(@Nullable List<?> rawLine) {
        if (rawLine == null) {
            return false;
        }
        String code = getCellValueOrEmpty(rawLine, 0, DICT_LENGTH);
        return code.equals(CodeNames.SPREADSHEET);
    }

    @Nullable
    public static DictData getDictData(@Nullable List<?> rawLine, @Nullable String defaultDictName, int loadSheetData) {
        if (rawLine == null) {
            return null;
        }
        String code = getCellValueOrEmpty(rawLine, 0, DICT_LENGTH);
        boolean canCopy;
        if (code.equals(CodeNames.DICT)) {
            canCopy = true;
        } else if (code.equals(CodeNames.DICT_NOT_COPY)) {
            canCopy = false;
        } else {
            return null;
        }
        String name = getCellValueOrEmpty(rawLine, 1, STRING_LENGTH);
        if (name.isEmpty()) {
            if (defaultDictName == null || defaultDictName.isEmpty()) {
                name = StaticUtils.getString(R.string.new_dictionary);
            } else {
                name = defaultDictName;
            }
        }
        String leftLanguage = getCellValueOrEmpty(rawLine, 2, ABB_LENGTH);
        String rightLanguage = getCellValueOrEmpty(rawLine, 3, ABB_LENGTH);
        String spreadsheetId = "";
        String sheetName = "";
        String hash = "";
        String author;
        String stringSheetId;
        Date currentDate = getDateOrDefault(rawLine, 4, STRING_LENGTH, DateUtils.getCurrentDate());
        Date dataSpreadsheetDate = null;
        long sheetId = -1;
        String spreadsheetName = "";
        if (loadSheetData == SHEET_DATA_CSV) { // For csv files
            spreadsheetName = getCellValueOrEmpty(rawLine, 5, STRING_LENGTH);
            spreadsheetId = getCellValueOrEmpty(rawLine, 6, STRING_LENGTH);
            sheetName = getCellValueOrEmpty(rawLine, 7, STRING_LENGTH);
            stringSheetId = getCellValueOrEmpty(rawLine, 8, STRING_LENGTH);
            author = getCellValueOrEmpty(rawLine, 9, STRING_LENGTH);
            if (!stringSheetId.isEmpty()) {
                try {
                    sheetId = Long.parseLong(stringSheetId);
                } catch (Exception e) {
                    // Nothing.
                }
            }
            if (spreadsheetId.isEmpty() || sheetName.isEmpty()) {
                currentDate = DateUtils.getCurrentDate();
                spreadsheetName = "";
                spreadsheetId = "";
                sheetName = "";
                sheetId = -1;
            }
        } else { // For sheet list
            author = getCellValueOrEmpty(rawLine, 5, STRING_LENGTH);
            hash = getCellValueOrEmpty(rawLine, 11, STRING_LENGTH);
            if (loadSheetData == SHEET_DATA_SAVE) {
                spreadsheetName = getCellValueOrEmpty(rawLine, 6, STRING_LENGTH);
                if (spreadsheetName.isEmpty()) {
                    spreadsheetName = StaticUtils.getString(R.string.default_spreadsheet_name);
                }
                spreadsheetId = getCellValueOrEmpty(rawLine, 7, STRING_LENGTH);
                sheetName = getCellValueOrEmpty(rawLine, 8, STRING_LENGTH);
                sheetId = getCellIntegerOrDefault(rawLine, 9, STRING_LENGTH, -1);
                dataSpreadsheetDate = getDateOrDefault(rawLine, 10, STRING_LENGTH, null);
                if (spreadsheetId.isEmpty() || sheetName.isEmpty()) {
                    if (currentDate == null) { // If current date is not existed for current sheet, then we give current date.
                        currentDate = DateUtils.getCurrentDate();
                    }
                    spreadsheetName = "";
                    spreadsheetId = "";
                    sheetName = "";
                    sheetId = -1;
                }
            }
        }
        return new DictData(name, SupportedLanguages.convertToCorrect(leftLanguage),
                SupportedLanguages.convertToCorrect(rightLanguage), spreadsheetId, spreadsheetName, sheetName, hash,
                sheetId, canCopy, currentDate, dataSpreadsheetDate, author);
    }

    public static DictData getDefaultDictData(@Nullable String defaultDictName) {
        if (defaultDictName == null || defaultDictName.isEmpty()) {
            defaultDictName = StaticUtils.getString(R.string.new_dictionary);
        }
        Date dataDate = DateUtils.getCurrentDate();
        return new DictData(defaultDictName, SupportedLanguages.convertToCorrect(null),
                SupportedLanguages.convertToCorrect(null), "", "", "", "",
                -1L, true, dataDate, dataDate, null);
    }

    public static boolean isHtmlStringNullOrEmpty(@Nullable String string) {
        return string == null || Html.fromHtml(string, HtmlCompat.FROM_HTML_MODE_COMPACT).length() == 0;
    }

    @NonNull
    public static String getCellValueOrEmpty(@NonNull List<?> rawLine, int index, int maxLength) {
        return Objects.requireNonNull(getCellValueOrDefault(rawLine, index, maxLength, ""));
    }

    public static int getCellIntegerOrDefault(@NonNull List<?> rawLine, int index, int maxLength, int defaultValue) {
        String value = getCellValueOrEmpty(rawLine, index, maxLength);
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    public static long getCellLongOrDefault(@NonNull List<?> rawLine, int index, int maxLength, long defaultValue) {
        String value = getCellValueOrEmpty(rawLine, index, maxLength);
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Nullable
    public static Date getDateOrDefault(@NonNull List<?> rawLine, int index, int maxLength, @Nullable Date defaultDate) {
        long longDate = getCellLongOrDefault(rawLine, index, maxLength, -1);
        return getDateOrDefault(longDate, defaultDate);
    }

    @Nullable
    public static Date getDateOrDefault(long value, @Nullable Date defaultDate) {
        if (value < 0) {
            return defaultDate;
        }
        Date date = DateUtils.convertLotus123(value);
        date = DateUtils.checkCorrectedOrNull(date);
        if (date == null) { // try to convert timestamp
            date = Converter.fromTimestamp(value * 1000L);
            date = DateUtils.checkCorrectedOrNull(date);
            if (date == null) {
                date = Converter.fromTimestamp(value);
                date = DateUtils.checkCorrectedOrNull(date);
            }
        }
        return date == null ? defaultDate : date;
    }

    public static int getCellPositiveIntegerOrZero(@NonNull List<?> rawLine, int index, int maxLength, int maxValue) {
        int value = getCellIntegerOrDefault(rawLine, index, maxLength, 0);
        value = Math.max(0, value);
        if (value > maxValue) {
            value = maxValue;
        }
        return value;
    }

    @Nullable
    public static String getCellValueOrDefault(@NonNull List<?> rawLine, int index, int maxLength, @Nullable String defValue) {
        if (rawLine.size() > index) {
            Object line = rawLine.get(index);
            if (line != null) {
                String result = line.toString().trim();
                if (maxLength > 0 && result.length() > maxLength) {
                    result = result.substring(0, maxLength);
                }
                return result;
            }
        }
        return defValue;
    }

    public static boolean addNote(@Nullable List<?> rawLine, ArrayList<Note> notes, int index) {
        if (rawLine == null) {
            return false;
        }
        String header = SheetDataBuilder.getCellValueOrEmpty(rawLine, 0, NOTE_HEADER_LENGTH);
        String content = SheetDataBuilder.getCellValueOrEmpty(rawLine, 1, NOTE_CONTENT_LENGTH);
        if (header.isEmpty() || content.isEmpty()) {
            return false;
        }
        if (SheetDataBuilder.isHtmlStringNullOrEmpty(header) || SheetDataBuilder.isHtmlStringNullOrEmpty(content)) {
            return false;
        }
        notes.add(new Note(0, header, content, index + 1));
        return true;
    }

    public static boolean addSample(@Nullable List<?> rawLine, ArrayList<Sample> samples, boolean progress) {
        String leftValue;
        String rightValue;
        if (fillFirstTwoValuesOrSkip(rawLine, STRING_LENGTH)) {
            leftValue = cache[0];
            rightValue = cache[1];
        } else {
            return false;
        }
        if (leftValue.equals(CodeNames.DICT) || leftValue.equals(CodeNames.DICT_NOT_COPY)) {
            return false;
        }
        String type = getCellValueOrEmpty(rawLine, 2, KIND_LENGTH);
        String example = getCellValueOrEmpty(rawLine, 3, EXAMPLE_LENGTH);
        Sample sample = new Sample(-1, leftValue, rightValue, 0, 0);
        sample.setType(type);
        sample.setExample(example);
        samples.add(sample);
        if (progress) {
            int leftPercentage = getCellPositiveIntegerOrZero(rawLine, 4, KIND_LENGTH, 100);
            int rightPercentage = getCellPositiveIntegerOrZero(rawLine, 5, KIND_LENGTH, 100);
            sample.setLeftPercentage(leftPercentage);
            sample.setRightPercentage(rightPercentage);
        }
        return true;
    }
}
