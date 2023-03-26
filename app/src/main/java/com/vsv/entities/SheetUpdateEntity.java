package com.vsv.entities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.vsv.db.entities.Converter;
import com.vsv.db.entities.SheetDatesUpdate;
import com.vsv.utils.DateUtils;

import java.util.Date;

public class SheetUpdateEntity implements Comparable<SheetUpdateEntity> {

    public long id;

    public @NonNull
    String sheetName;

    public long oldDateTimestamp;

    public long newDateTimestamp;

    public long sheetId;

    public String newSheetName; // Sheet name after updating, if null means the same.

    public long newSheetId = -1; // Sheet id after updating, if -1 means the same.

    public final int type;

    public SheetUpdateEntity(int type, long id, @NonNull String sheetName, long oldDateTimestamp, long newDateTimestamp, long sheetId) {
        this.id = id;
        this.sheetName = sheetName;
        this.oldDateTimestamp = oldDateTimestamp;
        this.newDateTimestamp = newDateTimestamp;
        this.sheetId = sheetId;
        this.type = type;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @NonNull
    public SheetUpdateEntity clone() {
        return new SheetUpdateEntity(type, id, sheetName, oldDateTimestamp, newDateTimestamp, sheetId);
    }

    // isUpdated = false means that we do not want to update dataDate because the dictionary / notebook content was not updated.
    @NonNull
    public SheetDatesUpdate convertToDBEntity(Date currentTime, boolean isUpdated) {
        SheetDatesUpdate updateEntity = new SheetDatesUpdate();
        Date dataDate = oldDateTimestamp == -1 ? null : Converter.fromTimestamp(oldDateTimestamp);
        updateEntity.dataDate = dataDate;
        updateEntity.sheetName = this.newSheetName == null ? this.sheetName : this.newSheetName;
        updateEntity.sheetId = this.newSheetId == -1 ? this.sheetId : this.newSheetId;
        if (newDateTimestamp != -1) {
            Date newDate = Converter.fromTimestamp(newDateTimestamp);
            if (newDate == null) {
                Log.e("SheetUpdate", "Can not get date for " + newDateTimestamp + ", use now date");
                newDate = DateUtils.getCurrentDate();
            }
            if (dataDate != null) {
                long minutes = DateUtils.getMinutesBetweenDates(dataDate, newDate);
                if (minutes > 0) {
                    if (isUpdated) {
                        updateEntity.dataDate = newDate;
                    }
                    updateEntity.needUpdate = true;
                }
            } else {
                if (isUpdated) {
                    updateEntity.dataDate = newDate;
                }
                updateEntity.needUpdate = true;
            }
        }
        updateEntity.id = this.id;
        updateEntity.successfulUpdateCheck = currentTime;
        return updateEntity;
    }

    @Override
    public int compareTo(SheetUpdateEntity entity) {
        return Long.compare(this.id, entity.id);
    }

    @NonNull
    @Override
    public String toString() {
        return "SheetUpdateEntity{" +
                "id=" + id +
                ", sheetName='" + sheetName + '\'' +
                ", oldDateTimestamp=" + oldDateTimestamp +
                ", newDateTimestamp=" + newDateTimestamp +
                '}';
    }
}
