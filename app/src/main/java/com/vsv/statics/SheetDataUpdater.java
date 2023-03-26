package com.vsv.statics;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.vsv.db.entities.Converter;
import com.vsv.db.entities.SheetDatesUpdate;
import com.vsv.db.entities.UpdateDate;
import com.vsv.entities.SheetUpdateEntity;
import com.vsv.entities.SheetUpdateData;
import com.vsv.models.MainModel;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.utils.DateUtils;
import com.vsv.utils.GoogleTasksExceptionHandler;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Timer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class SheetDataUpdater {

    private static ExecutorService runExecutor;

    // String - title, Long - timestamp, Long - added time.
    private static TreeMap<String, Pair<Pair<TreeMap<String, SheetUpdateData>, TreeMap<Long, SheetUpdateData>>, Long>> cache = new TreeMap<>();

    private static final TreeMap<String, TreeSet<SheetUpdateEntity>> queue = new TreeMap<>();

    private static long start;

    private static final int CHECK_TIME = 60;

    private static final Object key = new Object();

    // dictionary / notebook id, timestamp
    private static final Consumer<TreeSet<SheetUpdateEntity>> callback = SheetDataUpdater::updateDB;

    private SheetDataUpdater() {

    }

    private static ExecutorService getExecutor() {
        return Executors.newSingleThreadExecutor((runnable) -> {
            Thread thread = new Thread(runnable);
            thread.setName("SheetUpdaterRun");
            thread.setDaemon(true);
            return thread;
        });
    }

    private static void removeOld() {
        float pastTime = Timer.nanoTimeDiffFromNowInMinutes(start);
        if (pastTime > CHECK_TIME) {
            start = System.nanoTime();
            TreeMap<String, Pair<Pair<TreeMap<String, SheetUpdateData>, TreeMap<Long, SheetUpdateData>>, Long>> newCache = new TreeMap<>();
            for (Map.Entry<String, Pair<Pair<TreeMap<String, SheetUpdateData>, TreeMap<Long, SheetUpdateData>>, Long>> entry : cache.entrySet()) {
                long time = entry.getValue().second;
                pastTime = Timer.nanoTimeDiffFromNowInMinutes(time);
                if (pastTime < Spec.TIME_TO_DICTIONARY_CHECK_UPDATE) {
                    newCache.put(entry.getKey(), entry.getValue());
                }
            }
            cache = newCache;
        }
    }

    public static void shutdown() {
        if (runExecutor != null) {
            queue.clear();
            runExecutor.shutdownNow();
            runExecutor = null;
        }
    }

    private static void updateDB(TreeSet<SheetUpdateEntity> entities) {
        MainModel model = StaticUtils.getModelOrNull();
        if (model == null) {
            return;
        }
        ArrayList<SheetDatesUpdate> list = new ArrayList<>();
        Date currentTime = DateUtils.getCurrentDate();
        for (SheetUpdateEntity entity : entities) {
            list.add(entity.convertToDBEntity(currentTime, false));
        }
        if (!list.isEmpty()) {
            int type = entities.first().type; // All entities must have the same type.
            if (UpdateDate.DICTIONARY == type) {
                model.getDictionariesRepository().updateDateSeveral(list);
            } else if (UpdateDate.NOTEBOOK == type) {
                model.getNotebooksRepository().updateDateSeveral(list);
            }
        }
    }

    public static void run() {
        if (runExecutor == null || runExecutor.isShutdown()) {
            runExecutor = getExecutor();
            start = System.nanoTime();
            runExecutor.execute(() -> {
                while (!Thread.currentThread().isInterrupted()) {

                    try {
                        Map.Entry<String, TreeSet<SheetUpdateEntity>> entry;
                        synchronized (key) {
                            while (queue.firstEntry() == null) {
                                key.wait();
                            }
                            entry = queue.firstEntry();
                        }
                        if (entry != null) {
                            handle(entry);
                        }
                        //noinspection BusyWait
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }

    private static void handle(@NonNull Map.Entry<String, TreeSet<SheetUpdateEntity>> entry) {
        GoogleSignInAccount account = GlobalData.account;
        if (account == null) {
            synchronized (key) {
                queue.clear();
            }
            return;
        }
        String spreadsheetId = entry.getKey();
        TreeSet<SheetUpdateEntity> value = entry.getValue();
        try {
            // first - sheet name, timestamp, second - sheet id, timestamp.
            Pair<TreeMap<String, SheetUpdateData>, TreeMap<Long, SheetUpdateData>> result = SheetLoader.loadDates(account, spreadsheetId);
            synchronized (key) {
                cache.put(spreadsheetId, new Pair<>(new Pair<>(result.first, result.second), System.nanoTime()));
                for (SheetUpdateEntity sheetUpdateEntity : value) {
                    SheetUpdateData updateData = result.first.getOrDefault(sheetUpdateEntity.sheetName, null);
                    if (updateData == null) {
                        sheetUpdateEntity.newDateTimestamp = -1;
                        updateData = result.second.getOrDefault(sheetUpdateEntity.sheetId, null);
                    }
                    if (updateData != null) {
                        sheetUpdateEntity.newDateTimestamp = updateData.timestamp;
                        sheetUpdateEntity.newSheetId = updateData.sheetId;
                        sheetUpdateEntity.newSheetName = updateData.sheetName;
                    }
                }
                queue.remove(entry.getKey());
            }
            callback.accept(value);
        } catch (IOException e) {
            int error = GoogleTasksExceptionHandler.getErrorCode(e);
            if (error == GoogleTasksExceptionHandler.NO_PERMISSION || error == GoogleTasksExceptionHandler.NO_SPREADSHEET) {
                synchronized (key) {
                    cache.put(spreadsheetId, new Pair<>(new Pair<>(new TreeMap<>(), new TreeMap<>()), System.nanoTime()));
                    queue.remove(entry.getKey());
                    for (SheetUpdateEntity entity : value) {
                        entity.newDateTimestamp = -1L;
                    }
                }
                callback.accept(value);
            }
        }
    }

    private static void putToQueue(@NonNull String spreadsheetId, @NonNull ArrayList<SheetUpdateEntity> idsNames) {
        TreeSet<SheetUpdateEntity> entry = queue.getOrDefault(spreadsheetId, null);
        if (entry == null) {
            TreeSet<SheetUpdateEntity> treeSet = new TreeSet<>();
            for (SheetUpdateEntity entity : idsNames) {
                treeSet.add(entity.clone());
            }
            queue.put(spreadsheetId, treeSet);
            key.notify();
        } else {
            for (SheetUpdateEntity entity : idsNames) {
                entry.add(entity.clone());
            }
        }
    }

    // If null means we did not check date yet. DictionaryId / NotebookId is used only if it needs to put into queue.
    // <id, timestamp>
    @Nullable
    public static ArrayList<SheetUpdateEntity> getUpdateDateOrPutToQueue(@NonNull String spreadsheetId,
                                                                         @NonNull ArrayList<SheetUpdateEntity> idsNames) {
        if (idsNames.isEmpty()) {
            return idsNames;
        }
        synchronized (key) {
            removeOld();
            Pair<Pair<TreeMap<String, SheetUpdateData>, TreeMap<Long, SheetUpdateData>>, Long> data =
                    cache.getOrDefault(spreadsheetId, null);
            if (data != null) {
                for (SheetUpdateEntity entity : idsNames) {
                    if (entity.oldDateTimestamp > 0) { // Data in the cache is older than current.
                        if (entity.oldDateTimestamp >= data.second) {
                            entity.newDateTimestamp = -1; // We do not have newer timestamp yet.
                            continue;
                        }
                    }
                    SheetUpdateData updateData = data.first.first.getOrDefault(entity.sheetName, null);
                    if (updateData == null) {
                        entity.newDateTimestamp = -1;
                        updateData = data.first.second.getOrDefault(entity.sheetId, null);
                    }
                    if (updateData != null) {
                        entity.newDateTimestamp = updateData.timestamp;
                        entity.newSheetId = updateData.sheetId;
                        entity.newSheetName = updateData.sheetName;
                    }
                }
                return idsNames;
            } else {
                putToQueue(spreadsheetId, idsNames);
                return null;
            }
        }
    }

    public static ArrayList<SheetDatesUpdate> updateDates(ArrayList<UpdateDate> items) {
        ArrayList<SheetDatesUpdate> toUpdate = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return toUpdate;
        }
        TreeMap<String, ArrayList<SheetUpdateEntity>> map = new TreeMap<>();
        Date currentTime = DateUtils.getCurrentDate();
        for (UpdateDate item : items) {
            if (!item.needUpdate() && item.hasOwner() && item.needCheckUpdate(currentTime)) {
                ArrayList<SheetUpdateEntity> list = map.getOrDefault(item.getSpreadsheetId(), null);
                long timestamp = item.getLastUpdatedDate() == null ? -1 : Converter.dateToTimestamp(item.getLastUpdatedDate());
                SheetUpdateEntity updateEntity = new SheetUpdateEntity(item.getType(), item.getId(),
                        item.getSheetName(), timestamp, -1, item.getSheetId());
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(updateEntity);
                    map.put(item.getSpreadsheetId(), list);
                } else {
                    list.add(updateEntity);
                }
            }
        }
        for (Map.Entry<String, ArrayList<SheetUpdateEntity>> entry : map.entrySet()) {
            ArrayList<SheetUpdateEntity> result = SheetDataUpdater.getUpdateDateOrPutToQueue(entry.getKey(), entry.getValue());
            if (result != null) {
                for (SheetUpdateEntity entity : result) {
                    toUpdate.add(entity.convertToDBEntity(currentTime, false));
                }
            }
        }
        return toUpdate;
    }
}
