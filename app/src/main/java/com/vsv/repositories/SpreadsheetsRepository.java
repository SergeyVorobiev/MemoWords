package com.vsv.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.vsv.db.dao.SpreadsheetDao;
import com.vsv.db.entities.SpreadSheetInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpreadsheetsRepository {

    private final SpreadsheetDao dao;

    private final ExecutorService executor;

    public SpreadsheetsRepository(SpreadsheetDao dao, ExecutorService executor) {
        this.dao = dao;
        this.executor = executor;
    }

    public Future<ArrayList<SpreadSheetInfo>> getAll() {
        return executor.submit(() -> (ArrayList<SpreadSheetInfo>) dao.getAll());
    }

    @NonNull
    public TreeMap<String, String> getAllIdNames(int timeoutInSeconds) {
        TreeMap<String, String> result = new TreeMap<>();
        try {
            ArrayList<SpreadSheetInfo> infos = getAll().get(timeoutInSeconds, TimeUnit.SECONDS);
            if (infos != null) {
                for (SpreadSheetInfo info : infos) {
                    result.put(info.spreadSheetId, info.name);
                }
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            // Return empty.
        }
        return result;
    }

    public LiveData<List<SpreadSheetInfo>> getAllLive() {
        return dao.getAllLive();
    }

    public void delete(SpreadSheetInfo spreadSheetInfo) {
        executor.execute(() -> dao.delete(spreadSheetInfo));
    }

    public void insert(@NonNull String spreadSheetId, @NonNull String name, int type) {
        executor.execute(() -> {
            SpreadSheetInfo spreadSheetInfo = new SpreadSheetInfo(spreadSheetId, name);
            spreadSheetInfo.type = type;
            spreadSheetInfo.id = dao.insert(spreadSheetInfo);
        });
    }

    public void insert(@NonNull SpreadSheetInfo spreadsheet) {
        executor.execute(() -> dao.insert(spreadsheet));
    }

    public void update(SpreadSheetInfo spreadSheetInfo) {
        executor.execute(() -> dao.update(spreadSheetInfo));
    }

    public void insertSeveral(List<SpreadSheetInfo> spreadsheetInfos) {
        executor.execute(() -> dao.insertSeveral(spreadsheetInfos));
    }
}
