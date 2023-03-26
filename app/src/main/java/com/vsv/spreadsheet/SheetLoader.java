package com.vsv.spreadsheet;

import android.util.Pair;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.vsv.db.entities.Converter;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.Note;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.PipelineDialog;
import com.vsv.dialogs.WaitDialog;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.dialogs.entities.PipelineTask;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.entities.SheetUpdateData;
import com.vsv.memorizer.R;
import com.vsv.models.MainModel;
import com.vsv.repositories.DictionaryWithSamplesRepository;
import com.vsv.repositories.NotebookWithNotesRepository;
import com.vsv.speech.SupportedLanguages;
import com.vsv.spreadsheet.entities.SSDictData;
import com.vsv.spreadsheet.entities.SSNotebookData;
import com.vsv.statics.GlobalData;
import com.vsv.statics.GlobalExecutors;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.DateUtils;
import com.vsv.utils.GoogleTasksExceptionHandler;
import com.vsv.utils.SamplesHashGenerator;
import com.vsv.utils.SheetDataBuilder;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.TableConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SheetLoader {

    private SheetLoader() {

    }

    private static final int MAX_DATE_ITEMS = 500;

    // first - timestamp, second - sheet id.
    @NonNull
    public static Pair<TreeMap<String, SheetUpdateData>, TreeMap<Long, SheetUpdateData>> loadDates(@NonNull GoogleSignInAccount account,
                                                                                                   @NonNull String spreadsheetId) throws IOException {
        Sheets sheetsService = SheetsBuilder.buildSheetsService(account);
        final String range = "!E1";
        TreeMap<String, SheetUpdateData> titleMap = new TreeMap<>();
        TreeMap<Long, SheetUpdateData> idMap = new TreeMap<>();
        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = spreadsheet.getSheets();
        ArrayList<String> ranges = new ArrayList<>();
        ArrayList<Pair<String, Long>> titles = new ArrayList<>();
        if (sheets != null) {
            for (Sheet sheet : sheets) {
                String title = sheet.getProperties().getTitle();
                long sheetId = sheet.getProperties().getSheetId();
                titles.add(new Pair<>(title, sheetId));
                ranges.add(title + range);
            }
        }
        if (!ranges.isEmpty()) {
            Sheets.Spreadsheets.Values.BatchGet request =
                    sheetsService.spreadsheets().values().batchGet(spreadsheetId);
            request.setRanges(ranges);
            request.setValueRenderOption("UNFORMATTED_VALUE");
            request.setDateTimeRenderOption("SERIAL_NUMBER");
            BatchGetValuesResponse response = request.execute();
            List<ValueRange> valueRanges = response.getValueRanges();
            if (valueRanges != null && !valueRanges.isEmpty()) {
                int index = 0;
                for (ValueRange valueRange : valueRanges) {
                    // String stringRange = valueRange.getRange();
                    // Sometimes title in range returns with '' extra symbols.
                    // String title = stringRange.substring(0, stringRange.lastIndexOf("!"));
                    String title = titles.get(index).first;
                    long sheetId = titles.get(index++).second;
                    SheetUpdateData defUpdateData = new SheetUpdateData(title, sheetId, -1);
                    List<List<Object>> data = valueRange.getValues();
                    if (data != null && !data.isEmpty()) {
                        try {
                            long dataValue = -1;
                            try {
                                dataValue = ((BigDecimal) data.get(0).get(0)).toBigInteger().longValue();
                            } catch (Throwable th) {
                                try {
                                    dataValue = Long.parseLong(data.get(0).get(0).toString());
                                } catch (Throwable th2) {
                                    // Nothing to change
                                }
                            }
                            Date date = SheetDataBuilder.getDateOrDefault(dataValue, null);
                            if (date != null) {
                                SheetUpdateData updateData = new SheetUpdateData(title, sheetId, Converter.dateToTimestamp(date));
                                titleMap.put(title, updateData);
                                idMap.put(sheetId, updateData);
                            } else {
                                titleMap.put(title, defUpdateData);
                                idMap.put(sheetId, defUpdateData);
                            }
                        } catch (Throwable th) {
                            titleMap.put(title, defUpdateData);
                            idMap.put(sheetId, defUpdateData);
                        }
                    } else {
                        titleMap.put(title, defUpdateData);
                        idMap.put(sheetId, defUpdateData);
                    }
                    if (titleMap.size() >= MAX_DATE_ITEMS) {
                        break;
                    }
                }
            }
        }
        return new Pair<>(titleMap, idMap);
    }

    public static BackgroundTask<BatchUpdateValuesResponse> buildWriteTableIntoSheetTask(@NonNull String spreadsheetId,
                                                                                         @NonNull String sheetName,
                                                                                         @NonNull List<List<Object>> table,
                                                                                         @NonNull String startCell,
                                                                                         int timeoutInSeconds) {
        return new BackgroundTask<>(timeoutInSeconds, () -> {
            Sheets sheetsService = SheetsBuilder.buildSheetsService(GlobalData.account);
            List<ValueRange> values = new ArrayList<>();
            values.add(new ValueRange().setRange(sheetName + "!" + startCell).setValues(table));
            BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                    .setValueInputOption("RAW").setData(values);
            return sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, batchBody).execute();
        });
    }

    public static void sendShelf(@NonNull ArrayList<Dictionary> dictionaries, @NonNull String spreadsheetId) {
        GoogleSignInAccount account = GlobalData.getAccountOrToast();
        if (account == null) {
            return;
        }
        MainModel mainModel = StaticUtils.getModel();
        Sheets sheetsService = SheetsBuilder.buildSheetsService(account);
        ArrayList<Dictionary> names = null;
        ArrayList<BackgroundTask<?>> tasks = new ArrayList<>();
        BackgroundTask<?> createSheetsTask = new BackgroundTask<>(20, () -> {
            ArrayList<Pair<String, Long>> sheets = getAllSheets(sheetsService, spreadsheetId);
            ArrayList<String> existingSheets = new ArrayList<>();
            ArrayList<String> sheetsToAdd = new ArrayList<>();
            for (Pair<String, Long> pair : sheets) {
                existingSheets.add(pair.first);
            }
            for (Dictionary dictionary : dictionaries) {
                String sheetName = dictionary.sheetName;
                if (sheetName == null || sheetName.isEmpty()) {
                    sheetName = dictionary.getName();
                }
                if (!existingSheets.contains(sheetName)) {
                    sheetsToAdd.add(sheetName);
                }
            }
            if (!sheetsToAdd.isEmpty()) {
                GlobalExecutors.modelsExecutor.submit(SheetUpdater.createSheetsTask(sheetsService, spreadsheetId, sheetsToAdd)).get(10, TimeUnit.SECONDS);
            }
            return null;
        });
        tasks.add(createSheetsTask);
        createSheetsTask.setRunMainThreadOnFail((e) -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(e)));
        int i = 0;
        for (Dictionary dictionary : dictionaries) {
            BackgroundTask<?> task = SheetUpdater.createTaskToWriteToSheet(sheetsService, account.getEmail(),
                    spreadsheetId, dictionary.sheetName, dictionary, true);
            task.setRunMainThreadOnFail(e -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(e)));
            tasks.add(task);
            i++;
        }
        WaitDialog dialog = new WaitDialog(WeakContext.getContext(), tasks);
        dialog.setTimeoutBetweenTasks(500);
        dialog.showProgress();
        dialog.showInterruptButton();
        dialog.showOver();
    }

    // The new array will be created, source will not be changed.
    @NonNull
    private static ArrayList<SheetTab> cutToSize(@NonNull ArrayList<SheetTab> sheets, int size) {
        ArrayList<SheetTab> sheetsToAdd = new ArrayList<>();
        int count = 0;
        for (SheetTab sheet : sheets) {
            if (count >= size) {
                break;
            }
            sheetsToAdd.add(sheet);
            count++;
        }
        return sheetsToAdd;
    }

    @Nullable
    public static PipelineDialog buildLoadSpreadsheetDataDialog(@NonNull String spreadsheetId,
                                                                @Nullable String spreadsheetName,
                                                                @NonNull ArrayList<SheetTab> sheets,
                                                                @Nullable ArrayList<String> explicitDictionaryNames,
                                                                long shelfId,
                                                                boolean bindSpreadsheet,
                                                                boolean loadProgress, int maxDictionariesCount) {
        GoogleSignInAccount account = GlobalData.getAccountOrToast();
        if (account == null) {
            return null;
        }
        ArrayList<SheetTab> sheetsToAdd = cutToSize(sheets, maxDictionariesCount);
        ArrayList<PipelineTask> tasks = new ArrayList<>();
        if (!sheetsToAdd.isEmpty()) {
            PipelineTask task = new PipelineTask(20,
                    (previousResult) -> getDictionariesData(account, spreadsheetId, spreadsheetName, sheetsToAdd,
                            explicitDictionaryNames, shelfId, loadProgress, bindSpreadsheet, false));
            tasks.add(task);
            task = new PipelineTask(60, (previousResult) -> {
                ArrayList<DictionaryWithSamples> loadedData = null;
                ArrayList<Float> percentages = new ArrayList<>();
                if (previousResult != null) {

                    // noinspection unchecked
                    loadedData = (ArrayList<DictionaryWithSamples>) previousResult.taskResult;
                }
                MainModel model = StaticUtils.getModelOrNull();
                if (model != null && loadedData != null && !loadedData.isEmpty()) {
                    DictionaryWithSamplesRepository repository = model.getDictionaryWithSamplesRepository();
                    for (DictionaryWithSamples dictionaryWithSamples : loadedData) {
                        percentages.add(dictionaryWithSamples.dictionary.getPassedPercentage());
                        try {
                            repository.insertWithSamples(dictionaryWithSamples).get(5, TimeUnit.SECONDS);
                        } catch (ExecutionException | InterruptedException | TimeoutException e) {
                            // Nothing to do
                        }
                    }
                }
                return percentages;
            });
            tasks.add(task);
            return PipelineTask.buildPipelineDialog(tasks);
        }
        return null;
    }

    public static Pair<ArrayList<SpreadSheetInfo>, String> loadDictPresets(GoogleSignInAccount account,
                                                                           String spreadsheetId,
                                                                           String sheetName) {
        if (account == null) {
            return new Pair<>(null, StaticUtils.getString(R.string.toast_please_login));
        }
        Sheets sheets;
        try {
            sheets = SheetsBuilder.buildSheetsService(account);
        } catch (Throwable e) {
            return new Pair<>(null, GoogleTasksExceptionHandler.handle(e));
        }
        final String range = Spec.PRESETS_RANGE;
        try {
            ArrayList<SpreadSheetInfo> infos = new ArrayList<>();
            List<List<Object>> data = SheetsBuilder.buildAndGetSheetValues(sheets, spreadsheetId, sheetName, Spec.SPREADSHEETS_RANGE, Spec.MAX_PRESETS);
            TableConverter.convertTableToSpreadsheets(data, infos);
            return new Pair<>(infos, null);
        } catch (Throwable e) {
            return new Pair<>(null, GoogleTasksExceptionHandler.handle(e));
        }
    }

    @MainThread
    public static void loadNotes(GoogleSignInAccount account, @Nullable String notebookName,
                                 @NonNull String spreadsheetId, @NonNull String spreadsheetName,
                                 @NonNull String sheetName, long sheetId, boolean bindSpreadsheet, int timeout) {
        if (account == null) {
            Toasts.needLogin();
            return;
        }
        NotebookWithNotesRepository repository = StaticUtils.getModel().getNotebookWithNotesRepository();
        Callable<?> callable = () -> {
            SSNotebookData notebookData = getNotebookData(account, spreadsheetId, spreadsheetName,
                    sheetName, sheetId, true, bindSpreadsheet, notebookName).call();
            repository.insertWithSamples(notebookData);
            return null;
        };
        BackgroundTask<?> task = new BackgroundTask<>(timeout, callable);
        task.setRunMainThreadOnSuccess((empty) -> Toasts.success());
        task.setRunMainThreadOnFail(exception -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(exception)));
        task.buildWaitDialog().showOver();
    }

    private static ArrayList<Pair<String, Long>> getAllSheets(@NonNull Sheets sheetsService,
                                                              @NonNull String spreadsheetId) throws IOException {
        ArrayList<Pair<String, Long>> result = new ArrayList<>();
        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = spreadsheet.getSheets();
        if (sheets != null) {
            for (Sheet sheet : sheets) {
                String title = sheet.getProperties().getTitle();
                long id = sheet.getProperties().getSheetId();
                result.add(new Pair<>(title, id));
            }
        }
        return result;
    }

    private static long getSheetId(@NonNull ArrayList<Pair<String, Long>> list, @NonNull String title) {
        for (Pair<String, Long> pair : list) {
            if (title.equals(pair.first)) {
                return pair.second;
            }
        }
        return -1;
    }

    @Nullable
    private static String getSheetTitle(@NonNull ArrayList<Pair<String, Long>> list, long sheetId) {
        for (Pair<String, Long> pair : list) {
            if (sheetId == pair.second) {
                return pair.first;
            }
        }
        return null;
    }

    private static Pair<String, Long> getSheetData(@NonNull String spreadsheetId, @NonNull String sheetName,
                                                   long sheetId, @NonNull Sheets sheetsService) throws Exception {
        ArrayList<Pair<String, Long>> allSheets = getAllSheets(sheetsService, spreadsheetId);
        long id = getSheetId(allSheets, sheetName);
        if (id == -1) {
            String title = getSheetTitle(allSheets, sheetId);
            if (title != null) {
                sheetName = title;
            }
        } else {
            sheetId = id;
        }
        return new Pair<>(sheetName, sheetId);
    }

    @MainThread
    public static Callable<SSNotebookData> getNotebookData(@NonNull GoogleSignInAccount account,
                                                           @NonNull String spreadsheetId,
                                                           @NonNull String spreadsheetName,
                                                           @NonNull String sheetName, long sheetId,
                                                           boolean loadSheetData, // Load the ref sheet data.
                                                           boolean bindCurrentSpreadsheet,
                                                           @Nullable String defaultNotebookName) {
        return () -> {
            Sheets sheetsService = SheetsBuilder.buildSheetsService(account);
            Pair<String, Long> sheetData = getSheetData(spreadsheetId, sheetName, sheetId, sheetsService);
            String sheetTitle = sheetData.first;
            long usedSheetId = sheetData.second;
            List<List<Object>> data = SheetsBuilder.buildAndBatchGetSheetValues(sheetsService, spreadsheetId,
                    SheetsBuilder.buildRange(sheetTitle, Spec.NOTEBOOK_RANGE, Spec.MAX_NOTES_WITH_HEADER));
            ArrayList<Note> notes = new ArrayList<>();
            int notesCount = 0;
            SheetDataBuilder.NotebookData notebookData = null;
            boolean headerHandle = false;
            String useDefaultNotebookName = defaultNotebookName;
            if (useDefaultNotebookName == null || useDefaultNotebookName.isEmpty()) {
                if (!sheetTitle.isEmpty()) {
                    useDefaultNotebookName = sheetTitle;
                } else {
                    useDefaultNotebookName = StaticUtils.getString(R.string.default_notebook_name);
                }
            }
            if (data == null || data.isEmpty()) {
                if (bindCurrentSpreadsheet) {
                    SheetDataBuilder.NotebookData notebookData1 = new SheetDataBuilder.NotebookData(DateUtils.getCurrentDate(), useDefaultNotebookName,
                            spreadsheetId, spreadsheetName, sheetTitle, usedSheetId, true, null);
                    notebookData1.updateCheck = notebookData1.dataDate;
                    return new SSNotebookData(notebookData1, notes);
                } else {
                    return new SSNotebookData(new SheetDataBuilder.NotebookData(null, useDefaultNotebookName,
                            null, null, null, -1, true, null), notes);
                }
            }
            for (List<Object> row : data) {
                if (!headerHandle) {
                    headerHandle = true;
                    int type = loadSheetData ? SheetDataBuilder.SHEET_DATA_SAVE : SheetDataBuilder.NO_SHEET_DATA;
                    notebookData = SheetDataBuilder.getNotebookData(row, useDefaultNotebookName, type);
                    if (notebookData != null) {
                        if (notebookData.spreadsheetId == null || notebookData.spreadsheetId.isEmpty() ||
                                notebookData.sheetName == null || notebookData.sheetName.isEmpty()) { // We do not have ref spreadsheet
                            if (bindCurrentSpreadsheet) {
                                notebookData.spreadsheetName = spreadsheetName;
                                notebookData.spreadsheetId = spreadsheetId;
                                notebookData.sheetName = sheetTitle;
                                notebookData.sheetId = usedSheetId;
                                notebookData.updateCheck = DateUtils.getCurrentDate(); // Because we have just load it.
                            }
                        } else { // We have ref spreadsheet
                            notebookData.dataDate = notebookData.spreadsheetDate; // Update check date here should be null.
                        }
                        continue; // We have got the header.
                    } else {
                        if (bindCurrentSpreadsheet) {
                            notebookData = new SheetDataBuilder.NotebookData(DateUtils.getCurrentDate(), useDefaultNotebookName,
                                    spreadsheetId, spreadsheetName, sheetTitle, usedSheetId, true, null);
                            notebookData.updateCheck = notebookData.dataDate;
                        } else {
                            notebookData = new SheetDataBuilder.NotebookData(null, useDefaultNotebookName,
                                    null, null, null, -1, true, null);
                        }
                    }
                }
                boolean result = SheetDataBuilder.addNote(row, notes, notes.size());
                if (result) {
                    notesCount++;
                }
                if (notesCount == Spec.MAX_NOTES) {
                    break;
                }
            }
            return new SSNotebookData(notebookData, notes);
        };
    }

    public static BackgroundTask<ArrayList<SpreadSheetInfo>> loadSpreadsheets(@NonNull String spreadsheetId,
                                                                              @NonNull String sheetName) {
        Callable<ArrayList<SpreadSheetInfo>> callable = () -> {
            ArrayList<SpreadSheetInfo> result = new ArrayList<>();
            Sheets sheetsService = SheetsBuilder.buildSheetsService(GlobalData.account);
            String range = SheetsBuilder.buildRange(sheetName, Spec.SPREADSHEETS_RANGE, Spec.MAX_SPREADSHEETS_TO_LOAD);
            List<List<Object>> table = SheetsBuilder.buildAndBatchGetSheetValues(sheetsService, spreadsheetId, range);
            TableConverter.convertTableToSpreadsheets(table, result);
            return result;
        };
        return new BackgroundTask<>(15, callable);
    }

    private static SSDictData parseSheet(@Nullable List<List<Object>> data, @Nullable String email, @Nullable String defaultDictionaryName,
                                         @NonNull String spreadsheetId, @NonNull String sheetTitle, long usedSheetId, boolean loadSheetData, boolean loadProgress) {
        ArrayList<Sample> samples = new ArrayList<>();
        int samplesCount = 0;
        SheetDataBuilder.DictData dictData = null;
        String useDefaultDictName = defaultDictionaryName;
        boolean headerHandle = false;
        if (useDefaultDictName == null || useDefaultDictName.isEmpty()) {
            if (!sheetTitle.isEmpty()) {
                useDefaultDictName = sheetTitle;
            }
        }
        if (data == null) {
            dictData = SheetDataBuilder.getDefaultDictData(useDefaultDictName);
            dictData.sheetId = usedSheetId;
            dictData.sheetName = sheetTitle;
            dictData.spreadsheetId = spreadsheetId;
            return new SSDictData(dictData, samples);
        }
        boolean progress = false;
        boolean doNotHaveHeader = false;
        for (List<Object> row : data) {
            if (!headerHandle) {
                headerHandle = true;
                int loadSheet = loadSheetData ? SheetDataBuilder.SHEET_DATA_SAVE : SheetDataBuilder.NO_SHEET_DATA;
                dictData = SheetDataBuilder.getDictData(row, useDefaultDictName, loadSheet);
                if (dictData == null) {
                    doNotHaveHeader = true;
                    dictData = SheetDataBuilder.getDefaultDictData(useDefaultDictName);
                }
                if (dictData.spreadsheetId.isEmpty() && dictData.sheetName.isEmpty()) {
                    dictData.spreadsheetId = spreadsheetId;
                    dictData.sheetId = usedSheetId;
                    dictData.sheetName = sheetTitle;
                }
                progress = loadProgress && dictData.hash != null && !dictData.hash.isEmpty();
                if (!doNotHaveHeader) {
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
        if (!samples.isEmpty() && progress) {
            String ssh = SamplesHashGenerator.getSSH(samples, email);
            if (!ssh.equals(dictData.hash)) {
                for (Sample sample : samples) {
                    sample.setLeftPercentage(0);
                    sample.setRightPercentage(0);
                }
            }
        }
        return new SSDictData(dictData, samples);
    }

    // #1
    @MainThread
    public static ArrayList<DictionaryWithSamples> getDictionariesData(@NonNull GoogleSignInAccount account,
                                                                       @NonNull String spreadsheetId,
                                                                       @Nullable String defaultSpreadsheetTitle,
                                                                       @NonNull ArrayList<SheetTab> sheetTabs,
                                                                       @Nullable ArrayList<String> explicitDictionaryNames,
                                                                       long shelfId,
                                                                       boolean loadProgress,
                                                                       boolean bindCurrentSpreadsheet,
                                                                       boolean synchronizeSheetTitlesWithIds) throws IOException {

        Sheets sheets = SheetsBuilder.buildSheetsService(account);
        String spreadsheetTitle;
        if (synchronizeSheetTitlesWithIds) {
            spreadsheetTitle = SheetsBuilder.buildActualSheetTabs(sheets, spreadsheetId, sheetTabs);
        } else {
            spreadsheetTitle = SheetsBuilder.buildSpreadsheet(sheets, spreadsheetId).getProperties().getTitle();
        }
        if (defaultSpreadsheetTitle != null && !defaultSpreadsheetTitle.isEmpty()) {
            spreadsheetTitle = defaultSpreadsheetTitle;
        }
        ArrayList<String> ranges = SheetsBuilder.buildRanges(sheetTabs, Spec.DICTIONARY_RANGE, Spec.MAX_SAMPLES_WITH_HEADER);
        List<ValueRange> valueRanges = SheetsBuilder.buildAndGetRangeValues(sheets, spreadsheetId, ranges);
        return SheetParser.parseDictionariesByValueRanges(spreadsheetId, spreadsheetTitle, sheetTabs, explicitDictionaryNames, valueRanges, shelfId,
                bindCurrentSpreadsheet, loadProgress, account.getEmail());
    }

    // #1
    @MainThread
    public static Callable<SSDictData> getDictionaryData(@NonNull GoogleSignInAccount account,
                                                         @NonNull String spreadsheetId, @NonNull String sheetName,
                                                         long sheetId, @Nullable String defaultDictionaryName,
                                                         boolean loadProgress, boolean loadSheetData) {
        return () -> {
            Sheets sheets = SheetsBuilder.buildSheetsService(account);
            Pair<String, Long> sheetData = getSheetData(spreadsheetId, sheetName, sheetId, sheets);
            String sheetTitle = sheetData.first;
            long usedSheetId = sheetData.second;
            List<List<Object>> data = SheetsBuilder.buildAndBatchGetSheetValues(sheets, spreadsheetId, SheetsBuilder.buildRange(sheetTitle, Spec.DICTIONARY_RANGE, Spec.MAX_SAMPLES_WITH_HEADER));
            return parseSheet(data, account.getEmail(), defaultDictionaryName, spreadsheetId, sheetTitle, usedSheetId, loadSheetData, loadProgress);
        };
    }

    @NonNull
    public static BackgroundTask<String> buildSpreadsheetTitleTask(@NonNull GoogleSignInAccount account, @NonNull String spreadsheetId) {
        return new BackgroundTask<>(10, () -> {
            Sheets sheets = SheetsBuilder.buildSheetsService(account);
            return SheetsBuilder.buildSpreadsheet(sheets, spreadsheetId).getProperties().getTitle();
        });
    }

    @MainThread
    public static boolean loadSheetData(@Nullable String dictName,
                                        String leftLocaleAbb, String rightLocaleAbb, SheetTab sheetTab,
                                        String spreadsheetId, @Nullable String spreadsheetName, long shelfId,
                                        boolean loadProgress, boolean bindSpreadsheet) {
        GoogleSignInAccount account = GlobalData.getAccountOrToast();
        if (account == null) {
            return false;
        }
        ArrayList<SheetTab> sheetList = new ArrayList<>();
        sheetList.add(sheetTab);
        ArrayList<String> dictNames = new ArrayList<>();
        if (dictName != null) {
            dictNames.add(dictName);
        }
        ArrayList<PipelineTask> tasks = new ArrayList<>();
        PipelineTask task = new PipelineTask(15,
                (prevTaskResult) -> SheetLoader.getDictionariesData(account, spreadsheetId, spreadsheetName, sheetList,
                        dictNames, shelfId, loadProgress, bindSpreadsheet, false));
        tasks.add(task);
        task = new PipelineTask(10, (prevTaskResult) -> {
            assert prevTaskResult != null;
            assert prevTaskResult.taskResult != null;
            MainModel model = StaticUtils.getModelOrNull();
            if (model != null) {
                TreeMap<String, String> map = model.getSpreadsheetsRepository().getAllIdNames(5);
                ArrayList<DictionaryWithSamples> dictionaries = (ArrayList<DictionaryWithSamples>) prevTaskResult.taskResult;
                for (DictionaryWithSamples dictionaryWithSamples : dictionaries) {
                    Dictionary dictionary = dictionaryWithSamples.dictionary;
                    String existedSpreadsheetName = map.get(dictionary.spreadsheetId);
                    if (existedSpreadsheetName != null && !existedSpreadsheetName.isEmpty()) {
                        dictionary.spreadsheetName = existedSpreadsheetName;
                    }
                }
            }
            return prevTaskResult.taskResult;
        });
        tasks.add(task);
        task = new PipelineTask(10, (prevTaskResult) -> {
            MainModel model = StaticUtils.getModelOrNull();
            if (model != null && prevTaskResult != null) {
                assert prevTaskResult.taskResult != null;
                ArrayList<DictionaryWithSamples> dictionaries = (ArrayList<DictionaryWithSamples>) prevTaskResult.taskResult;
                for (DictionaryWithSamples dictionaryWithSamples : dictionaries) {
                    Dictionary dictionary = dictionaryWithSamples.dictionary;
                    if (SupportedLanguages.isNotSpecified(dictionary.getLeftLocaleAbb())) {
                        if (leftLocaleAbb != null && !leftLocaleAbb.isEmpty()) {
                            dictionary.setLeftLocaleAbb(leftLocaleAbb);
                        }
                    }
                    if (SupportedLanguages.isNotSpecified(dictionary.getLeftLocaleAbb())) {
                        dictionary.setLeftLocaleAbb(SupportedLanguages.convertToCorrect(leftLocaleAbb));
                    }
                    if (SupportedLanguages.isNotSpecified(dictionary.getRightLocaleAbb())) {
                        dictionary.setRightLocaleAbb(SupportedLanguages.convertToCorrect(rightLocaleAbb));
                    }
                    model.getDictionaryWithSamplesRepository().insertWithSamples(dictionaryWithSamples);
                }
            }
            return null;
        });
        tasks.add(task);
        PipelineDialog dialog = PipelineTask.buildPipelineDialog(tasks);
        dialog.setRunMainThreadOnFail((e) -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(e)));
        dialog.showOver();
        return true;
    }

    public static BackgroundTask<ArrayList<SheetTab>> loadSheetTabs(@NonNull String spreadsheetId) {
        Callable<ArrayList<SheetTab>> callable = () -> {
            Sheets sheetsService = SheetsBuilder.buildSheetsService(GlobalData.account);
            return SheetsBuilder.buildAllSheetTabs(sheetsService, spreadsheetId);
        };
        return new BackgroundTask<>(10, callable);
    }
}
