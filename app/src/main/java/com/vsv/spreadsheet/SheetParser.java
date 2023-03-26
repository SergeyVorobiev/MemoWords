package com.vsv.spreadsheet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.Sample;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.utils.DateUtils;
import com.vsv.utils.SamplesHashGenerator;
import com.vsv.utils.SheetDataBuilder;
import com.vsv.utils.Spec;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class SheetParser {

    private SheetParser() {

    }

    @NonNull
    public static ArrayList<DictionaryWithSamples> parseDictionariesByValueRanges(@NonNull String spreadsheetId,
                                                                                  @NonNull String spreadsheetTitle,
                                                                                  @NonNull ArrayList<SheetTab> sheetTabs,
                                                                                  @Nullable ArrayList<String> explicitDictionaryNames,
                                                                                  @Nullable List<ValueRange> ranges,
                                                                                  long shelfId,
                                                                                  boolean bindCurrentSpreadsheet,
                                                                                  boolean loadProgress,
                                                                                  @Nullable String email) {
        ArrayList<DictionaryWithSamples> result = new ArrayList<>();
        if (ranges == null || ranges.isEmpty()) {
            return result;
        }
        int i = 0;
        for (ValueRange range : ranges) { // We rely on correct order of ranges which match to the sheet tabs.
            List<List<Object>> sheetData = range.getValues();
            String explicitDictionaryName = (explicitDictionaryNames != null && explicitDictionaryNames.size() > i) ? explicitDictionaryNames.get(i) : null;
            DictionaryWithSamples dictionaryWithSamples = parseDictionary(sheetData, spreadsheetId, spreadsheetTitle,
                    sheetTabs.get(i), explicitDictionaryName, shelfId, bindCurrentSpreadsheet, loadProgress, email);
            result.add(dictionaryWithSamples);
            i++;
        }
        return result;
    }

    public static DictionaryWithSamples parseDictionary(@Nullable List<List<Object>> tableData,
                                                        @NonNull String spreadsheetId,
                                                        @NonNull String spreadsheetTitle,
                                                        @NonNull SheetTab sheetTab,
                                                        @Nullable String explicitDictionaryName,
                                                        long shelfId,
                                                        boolean bindCurrentSpreadsheet,
                                                        boolean loadProgress,
                                                        @Nullable String email) {
        DictionaryWithSamples dictionaryWithSamples = new DictionaryWithSamples();
        String defaultDictionaryName = (explicitDictionaryName == null || explicitDictionaryName.isEmpty()) ? sheetTab.getTitle() : explicitDictionaryName;
        if (tableData == null || tableData.isEmpty()) {
            dictionaryWithSamples.dictionary = Dictionary.buildDefault(defaultDictionaryName);
            dictionaryWithSamples.samples = new ArrayList<>();
            dictionaryWithSamples.dictionary.setShelfId(shelfId);
            if (bindCurrentSpreadsheet) {
                dictionaryWithSamples.dictionary.spreadsheetName = spreadsheetTitle;
                dictionaryWithSamples.dictionary.spreadsheetId = spreadsheetId;
                dictionaryWithSamples.dictionary.sheetName = sheetTab.getTitle();
                dictionaryWithSamples.dictionary.sheetId = sheetTab.getId();
                dictionaryWithSamples.dictionary.dataDate = DateUtils.getCurrentDate();
                dictionaryWithSamples.dictionary.successfulUpdateCheck = DateUtils.getCurrentDate();
            }
        } else {
            boolean readHeader = false;
            int currentSamplesCount = 0;
            ArrayList<Sample> samples = new ArrayList<>();
            Dictionary dictionary = new Dictionary(0, "");
            String hash = null;
            boolean progress = false;
            Date currentSpreadsheetDate = DateUtils.getCurrentDate();
            boolean handledFirstRow = true;
            int samplesCount = 0;
            for (List<Object> row : tableData) {
                if (!readHeader) {
                    readHeader = true;
                    SheetDataBuilder.DictData dictData = SheetDataBuilder.getDictData(row, defaultDictionaryName,
                            SheetDataBuilder.SHEET_DATA_SAVE);
                    if (dictData == null) {
                        dictData = SheetDataBuilder.getDefaultDictData(defaultDictionaryName);
                        handledFirstRow = false;
                    }
                    hash = (loadProgress && email != null && dictData.hash != null && !dictData.hash.isEmpty()) ? dictData.hash : null;
                    progress = hash != null;
                    dictionary.spreadsheetId = dictData.spreadsheetId;
                    dictionary.spreadsheetName = dictData.spreadsheetName;
                    dictionary.sheetName = dictData.sheetName;
                    dictionary.sheetId = dictData.sheetId;
                    dictionary.canCopy = dictData.canCopy;
                    dictionary.setName(dictData.dictName);
                    dictionary.setLeftLocaleAbb(dictData.leftLanguage);
                    dictionary.setRightLocaleAbb(dictData.rightLanguage);
                    dictionary.dataDate = dictData.dataSpreadsheetDate;
                    dictionary.author = dictData.author;
                    currentSpreadsheetDate = dictData.dataDate;
                    if (handledFirstRow) {
                        continue; // We have got the header.
                    }
                }
                boolean result = SheetDataBuilder.addSample(row, samples, progress);
                if (result) {
                    samplesCount++;
                }
                if (samplesCount == Spec.MAX_SAMPLES) {
                    break;
                }
            }
            if (!samples.isEmpty() && hash != null) {
                String ssh = SamplesHashGenerator.getSSH(samples, email);
                if (!ssh.equals(hash)) {
                    for (Sample sample : samples) {
                        sample.setLeftPercentage(0);
                        sample.setRightPercentage(0);
                    }
                } else {
                    dictionary.setPassedPercentage(Dictionary.calculatePercentage(samples));
                }
            }
            dictionary.setCount(samples.size());
            dictionary.setRememberedCount((int) samples.stream().filter(Sample::isRemembered).count());
            dictionary.setShelfId(shelfId);
            if ((!dictionary.hasOwner() && bindCurrentSpreadsheet)) {
                dictionary.spreadsheetName = spreadsheetTitle;
                dictionary.spreadsheetId = spreadsheetId;
                dictionary.sheetName = sheetTab.getTitle();
                dictionary.sheetId = sheetTab.getId();
                dictionary.dataDate = currentSpreadsheetDate;
                dictionary.successfulUpdateCheck = DateUtils.getCurrentDate();
            }
            dictionaryWithSamples.dictionary = dictionary;
            dictionaryWithSamples.samples = samples;
        }
        return dictionaryWithSamples;
    }
}
