package com.vsv.spreadsheet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.DeleteSheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Note;
import com.vsv.db.entities.Notebook;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.SheetDatesUpdate;
import com.vsv.dialogs.WaitDialog;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.memorizer.R;
import com.vsv.models.MainModel;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.CodeNames;
import com.vsv.utils.DateUtils;
import com.vsv.utils.GoogleTasksExceptionHandler;
import com.vsv.utils.SamplesHashGenerator;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class SheetUpdater {

    private SheetUpdater() {

    }

    public static BackgroundTask<BatchUpdateSpreadsheetResponse> addTabTask(@NonNull GoogleSignInAccount account,
                                                                            @NonNull String spreadsheetId,
                                                                            @NonNull String tabName) {
        Callable<BatchUpdateSpreadsheetResponse> callable = () -> {
            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().setTitle(tabName))));
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            Sheets sheets = SheetsBuilder.buildSheetsService(account);
            return sheets.spreadsheets().batchUpdate(spreadsheetId, body).execute();
        };
        return new BackgroundTask<>(30, callable);
    }

    public static Callable<BatchUpdateSpreadsheetResponse> createSheetsTask(@NonNull Sheets sheetsService,
                                                                          @NonNull String spreadsheetId,
                                                                          @NonNull ArrayList<String> sheetNames) {
        assert !sheetNames.isEmpty();
        return () -> {
            List<Request> requests = new ArrayList<>();
            for (String name : sheetNames) {
                requests.add(new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().setTitle(name))));
            }
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            return sheetsService.spreadsheets().batchUpdate(spreadsheetId, body).execute();
        };
    }

    public static BackgroundTask<BatchUpdateSpreadsheetResponse> updateTabTask(@NonNull GoogleSignInAccount account,
                                                                               @NonNull String spreadsheetId,
                                                                               long sheetId,
                                                                               @NonNull String tabName) {
        Callable<BatchUpdateSpreadsheetResponse> callable = () -> {
            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setUpdateSheetProperties(new UpdateSheetPropertiesRequest().
                    setFields("Title").setProperties(new SheetProperties().setSheetId((int) sheetId).setTitle(tabName))));
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            Sheets sheets = SheetsBuilder.buildSheetsService(account);
            return sheets.spreadsheets().batchUpdate(spreadsheetId, body).execute();
        };
        return new BackgroundTask<>(30, callable);
    }

    public static BackgroundTask<BatchUpdateSpreadsheetResponse> deleteTabTask(@NonNull GoogleSignInAccount account, @NonNull String spreadsheetId, int sheetId) {
        Callable<BatchUpdateSpreadsheetResponse> callable = () -> {
            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setDeleteSheet(new DeleteSheetRequest().setSheetId(sheetId)));
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            Sheets sheets = SheetsBuilder.buildSheetsService(account);
            return sheets.spreadsheets().batchUpdate(spreadsheetId, body).execute();
        };
        return new BackgroundTask<>(30, callable);
    }

    public static BackgroundTask<BatchUpdateValuesResponse> createTaskToWriteToSheet(@NonNull Sheets sheets,
                                                                                     @Nullable String email,
                                                                                     @NonNull String spreadsheetId,
                                                                                     @Nullable String sheetName,
                                                                                     @NonNull Dictionary dictionary,
                                                                                     boolean sendProgress) {
        Callable<BatchUpdateValuesResponse> callable = () -> {
            List<ValueRange> data = new ArrayList<>();
            ArrayList<List<Object>> values = new ArrayList<>();
            ArrayList<Sample> samples = StaticUtils.getModel().getSamplesRepository().getSamples(dictionary.getId()).get(10, TimeUnit.SECONDS);

            // Add a header
            Date currentDate = DateUtils.getCurrentDate();
            ArrayList<Object> sampleData = new ArrayList<>();
            sampleData.add(CodeNames.DICT);
            sampleData.add(dictionary.getName());
            sampleData.add(dictionary.getLeftLocaleAbb());
            sampleData.add(dictionary.getRightLocaleAbb());
            if (!dictionary.hasOwner() || dictionary.hasOwner(spreadsheetId, sheetName)) { // We send dictionary into his spreadsheet and therefore we need to update data.
                MainModel model = StaticUtils.getModelOrNull();
                if (model != null) {
                    SheetDatesUpdate sheetDatesUpdate = new SheetDatesUpdate();
                    sheetDatesUpdate.sheetName = dictionary.sheetName;
                    sheetDatesUpdate.sheetId = dictionary.sheetId;
                    sheetDatesUpdate.needUpdate = false;
                    sheetDatesUpdate.id = dictionary.getId();
                    sheetDatesUpdate.dataDate = currentDate;
                    sheetDatesUpdate.successfulUpdateCheck = currentDate;
                    model.getDictionariesRepository().updateDateSeveral(Collections.singletonList(sheetDatesUpdate));
                }
            }
            sampleData.add(currentDate.getTime());
            sampleData.add(dictionary.author == null ? "": dictionary.author);
            if (dictionary.hasOwner()) {
                String dictionarySpreadsheetId = dictionary.spreadsheetId;
                String dictionarySheet = dictionary.sheetName;
                if (!dictionarySpreadsheetId.equals(spreadsheetId) || !dictionarySheet.equals(sheetName)) {
                    sampleData.add(dictionary.spreadsheetName == null ? "" : dictionary.spreadsheetName);
                    sampleData.add(dictionarySpreadsheetId);
                    sampleData.add(dictionarySheet);
                    sampleData.add(dictionary.sheetId);
                    sampleData.add(dictionary.dataDate == null ? "" : dictionary.dataDate.getTime());
                } else {
                    sampleData.add("");
                    sampleData.add("");
                    sampleData.add("");
                    sampleData.add("");
                    sampleData.add("");
                }
            } else {
                sampleData.add("");
                sampleData.add("");
                sampleData.add("");
                sampleData.add("");
                sampleData.add("");
            }
            if (sendProgress) {
                sampleData.add(SamplesHashGenerator.getSSH(samples, email));
                sampleData.add(StaticUtils.getString(R.string.load_progress_message, email));
                sampleData.add("");
            } else {
                sampleData.add("");
                sampleData.add("");
                sampleData.add("");
            }
            values.add(sampleData);

            // Add samples
            int index = 0;
            for (Sample sample : samples) {
                sampleData = new ArrayList<>();
                String leftValue = sample.getLeftValue();
                if ("?".equals(leftValue)) {
                    leftValue = buildFormula(index, true);
                }
                String rightValue = sample.getRightValue();
                if ("?".equals(rightValue)) {
                    rightValue = buildFormula(index, false);
                }
                sampleData.add(leftValue);
                sampleData.add(rightValue);
                sampleData.add(sample.getType());
                sampleData.add(sample.getExample());
                if (sendProgress) {
                    sampleData.add(String.valueOf(sample.getLeftPercentage()));
                    sampleData.add(String.valueOf(sample.getRightPercentage()));
                    sampleData.add("");
                } else {
                    sampleData.add("");
                    sampleData.add("");
                    sampleData.add("");
                }
                values.add(sampleData);
                index++;
            }
            int emptyFields = Spec.MAX_SAMPLES + 1 - samples.size();
            assert emptyFields > -1;
            List<Object> emptyRow = Arrays.asList("", "", "", "", "", "", "");
            for (int i = 0; i < emptyFields; i++) {
                values.add(emptyRow);
            }
            String rangeName = (sheetName == null || sheetName.isEmpty()) ? dictionary.getName() : sheetName;
            data.add(new ValueRange().setRange(rangeName + "!A1").setValues(values));
            BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED").setData(data);
            return sheets.spreadsheets().values()
                    .batchUpdate(spreadsheetId, batchBody).execute();
        };
        BackgroundTask<BatchUpdateValuesResponse> task = new BackgroundTask<>(60, callable);
        task.setRunMainThreadOnFail((e) -> Toasts.shortShowRaw(GoogleTasksExceptionHandler.handle(e)));
        return task;
    }

    private static String buildFormula(int index, boolean isLeft) {
        index = index + 2;
        String cell = isLeft? "B" + index: "A" + index;
        return isLeft ? "=GOOGLETRANSLATE(" + cell + "; $D$1; $C$1)" : "=GOOGLETRANSLATE(" + cell + "; $C$1; $D$1)";
    }

    public static void writeToSheet(@Nullable GoogleSignInAccount account, @NonNull String spreadsheetId,
                                    @NonNull String sheetName, @NonNull Dictionary dictionary,
                                    boolean sendProgress) {
        if (account == null) {
            Toasts.needLogin();
            return;
        }
        MainModel model = StaticUtils.getModelOrNull();
        if (model == null) {
            Toasts.unexpectedError();
            return;
        }
        Sheets sheets = SheetsBuilder.buildSheetsService(account);
        BackgroundTask<BatchUpdateValuesResponse> task = createTaskToWriteToSheet(sheets, account.getEmail(),
                spreadsheetId, sheetName, dictionary, sendProgress);
        WaitDialog dialog = new WaitDialog(WeakContext.getContext(), task);
        dialog.showOver();
    }

    public static void writeNotebookToSheet(GoogleSignInAccount account, String spreadsheetId, String sheetName, Notebook notebook) {
        if (account == null) {
            Toasts.needLogin();
            return;
        }
        Date currentTime = DateUtils.getCurrentDate();
        Callable<BatchUpdateValuesResponse> callable = () -> {
            Sheets sheets = SheetsBuilder.buildSheetsService(account);
            List<ValueRange> data = new ArrayList<>();
            ArrayList<List<Object>> values = new ArrayList<>();
            ArrayList<Note> notes = StaticUtils.getModel().getNotesRepository().getNotes(notebook.getId()).get(10, TimeUnit.SECONDS);

            // Add a header
            ArrayList<Object> notesData = new ArrayList<>();
            if (!notebook.hasOwner() || notebook.hasOwner(spreadsheetId, sheetName)) {
                notesData.add(CodeNames.NOTE);
                notesData.add(notebook.getName());
                notesData.add("");
                notesData.add("");
                notesData.add(currentTime.getTime());
            } else {
                notesData.add(CodeNames.NOTE);
                notesData.add(notebook.getName());
                notesData.add(notebook.spreadsheetName);
                notesData.add(notebook.spreadsheetId);
                notesData.add(currentTime.getTime());
                notesData.add(notebook.sheetName);
                notesData.add(notebook.sheetId);
                notesData.add(notebook.dataDate == null ? "" : notebook.dataDate.getTime());
            }
            values.add(notesData);

            // Add notes
            notes.sort(Comparator.comparingInt(Note::getNumber));
            for (Note note : notes) {
                notesData = new ArrayList<>();
                notesData.add(note.getName());
                notesData.add(note.getContent());
                values.add(notesData);
            }
            data.add(new ValueRange().setRange(sheetName + "!A1").setValues(values));
            BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                    .setValueInputOption("RAW").setData(data);
            return sheets.spreadsheets().values()
                    .batchUpdate(spreadsheetId, batchBody).execute();
        };
        BackgroundTask<BatchUpdateValuesResponse> task = new BackgroundTask<>(30, callable);
        task.setRunMainThreadOnSuccess((object) -> {
            if (notebook.hasOwner(spreadsheetId, sheetName)) { // Synchronize updated time
                SheetDatesUpdate sheetDatesUpdate = new SheetDatesUpdate();
                sheetDatesUpdate.dataDate = currentTime;
                sheetDatesUpdate.successfulUpdateCheck = currentTime;
                sheetDatesUpdate.needUpdate = false;
                sheetDatesUpdate.id = notebook.getId();
                sheetDatesUpdate.sheetName = notebook.sheetName;
                sheetDatesUpdate.sheetId = notebook.sheetId;
                StaticUtils.getModel().getNotebooksRepository().updateDateSeveral(Collections.singletonList(sheetDatesUpdate));
            }
            Toasts.success();
        });
        task.setRunMainThreadOnFail((e) -> Toasts.shortShowRaw(GoogleTasksExceptionHandler.handle(e)));
        task.buildWaitDialog().showOver();
    }
}
