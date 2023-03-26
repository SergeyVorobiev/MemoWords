package com.vsv.spreadsheet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public final class SheetsBuilder {

    public static final String SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets";

    // public static final String SHEETS_SCOPE_READ_ONLY = "https://www.googleapis.com/auth/spreadsheets.readonly";
    private static final String[] scopes = {SHEETS_SCOPE, /*SHEETS_SCOPE_READ_ONLY*/};

    private SheetsBuilder() {

    }

    @NonNull
    public static Sheets buildSheetsService(@NonNull GoogleSignInAccount account) {
        String appName = StaticUtils.getString(R.string.app_name);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(WeakContext.getContext(), Arrays.asList(scopes));
        //GoogleAccountCredential credential1 = GoogleAccountCredential.usingAudience(this.activity, getResources().getString(R.string.client_id));
        credential.setSelectedAccount(account.getAccount());
        credential.setBackOff(new ExponentialBackOff());
        return new Sheets.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).setApplicationName(appName).build();
    }

    @NonNull
    public static ArrayList<String> buildRanges(@NonNull ArrayList<SheetTab> sheetTabs, @NonNull String columnRange, int rowCount) {
        ArrayList<String> ranges = new ArrayList<>();
        String range = columnRange + rowCount;
        for (SheetTab sheetTab : sheetTabs) {
            ranges.add(sheetTab.getTitle() + "!" + range);
        }
        return ranges;
    }

    @NonNull
    public static Sheets.Spreadsheets.Get buildGetDataRequest(@NonNull Sheets sheets, @NonNull String spreadsheetId,
                                               @NonNull ArrayList<String> ranges, boolean includeGridData) throws IOException {
        Sheets.Spreadsheets.Get request = sheets.spreadsheets().get(spreadsheetId);
        request.setRanges(ranges);
        request.setIncludeGridData(includeGridData);
        return request;
    }

    @NonNull
    public static Sheets.Spreadsheets.Values.BatchGet buildBatchGetValuesRequest(@NonNull Sheets sheets, @NonNull String spreadsheetId,
                                                       @NonNull List<String> ranges) throws IOException {
        return sheets.spreadsheets().values().batchGet(spreadsheetId).setRanges(ranges)
                .setValueRenderOption("UNFORMATTED_VALUE")
                .setDateTimeRenderOption("SERIAL_NUMBER");
    }

    public static ArrayList<SheetTab> buildAllSheetTabs(@NonNull Sheets sheetsService, @NonNull String spreadsheetId) throws IOException {
        ArrayList<SheetTab> tabs = new ArrayList<>();
        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = spreadsheet.getSheets();
        if (sheets != null) {
            for (Sheet sheet : sheets) {
                tabs.add(new SheetTab(sheet.getProperties().getTitle(), sheet.getProperties().getSheetId()));
            }
        }
        return tabs;
    }

    public static Spreadsheet buildSpreadsheet(@NonNull Sheets sheetsService, @NonNull String spreadsheetId) throws IOException {
        return sheetsService.spreadsheets().get(spreadsheetId).execute();
    }

    // It replaces the data in sheetTabs.
    // Returns spreadsheet title.
    public static String buildActualSheetTabs(@NonNull Sheets sheetsService, @NonNull String spreadsheetId,
                                                                @NonNull ArrayList<SheetTab> sheetTabs) throws IOException {
        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
        String spreadsheetTitle = spreadsheet.getProperties().getTitle();
        List<Sheet> sheets = spreadsheet.getSheets();
        TreeMap<String, Integer> titleMap = new TreeMap<>();
        TreeMap<Integer, String> idMap = new TreeMap<>();
        if (sheets != null) {
            for (Sheet sheet : sheets) {
                String title = sheet.getProperties().getTitle();
                int sheetId = sheet.getProperties().getSheetId();
                titleMap.put(title, sheetId);
                idMap.put(sheetId, title);
            }
            for (SheetTab sheetTab : sheetTabs) {
                Integer id = titleMap.get(sheetTab.getTitle());
                if (id == null) {
                    String title = idMap.get(sheetTab.getId());
                    if (title != null) {
                        sheetTab.setTitle(title);
                    }
                } else {
                    sheetTab.setId(id);
                }
            }
        }
        return spreadsheetTitle;
    }

    public static Spreadsheet buildAndGetSheetData(@NonNull Sheets sheets, @NonNull String spreadsheetId,
                                                   @NonNull ArrayList<String> ranges, boolean includeGridData) throws IOException {
        return buildGetDataRequest(sheets, spreadsheetId, ranges, includeGridData).execute();
    }

    public static String buildRange(@NonNull String sheetTitle, @NonNull String range, int size) {
        return sheetTitle + "!" + range + size;
    }

    @NonNull
    public static List<ValueRange> buildAndGetRangeValues(@NonNull Sheets sheetsService, @NonNull String spreadsheetId,
                                                          @NonNull ArrayList<String> ranges) throws IOException {
        return buildBatchGetValuesRequest(sheetsService, spreadsheetId, ranges).execute().getValueRanges();
    }

    @Nullable
    public static List<List<Object>> buildAndGetSheetValues(@NonNull Sheets sheetsService,
                                                            @NonNull String spreadsheetId,
                                                            @NonNull String sheetTitle,
                                                            @NonNull String range, int maxCount) throws IOException {
        Sheets.Spreadsheets ss = sheetsService.spreadsheets();
        Sheets.Spreadsheets.Values values = ss.values();
        Sheets.Spreadsheets.Values.Get get = values.get(spreadsheetId, buildRange(sheetTitle, range, maxCount));
        ValueRange valueRange = get.execute();
        return valueRange.getValues();
    }

    @Nullable
    public static List<List<Object>> buildAndBatchGetSheetValues(@NonNull Sheets sheetsService, @NonNull String spreadsheetId,
                                                                 @NonNull String range) throws IOException {
        List<ValueRange> valueRanges = buildBatchGetValuesRequest(sheetsService, spreadsheetId,
                Collections.singletonList(range)).execute().getValueRanges();
        if (valueRanges == null || valueRanges.isEmpty()) {
            return null;
        }
        return valueRanges.get(0).getValues();
    }
}
