package com.vsv.db.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.vsv.memorizer.R;
import com.vsv.speech.SupportedLanguages;
import com.vsv.utils.DateUtils;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Date;

@Entity(tableName = "dictionary", foreignKeys = {@ForeignKey(entity = Shelf.class,
        parentColumns = "id",
        childColumns = "shelfId",
        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = {"shelfId"})})
@TypeConverters({Converter.class})
public class Dictionary implements UpdateDate {

    public static final int MAX_TRACKER_VALUES = 100;

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    private long shelfId;

    @ColumnInfo(name = "left_locale_abb")
    private String leftLocaleAbb;

    @ColumnInfo(name = "right_locale_abb")
    private String rightLocaleAbb;

    @ColumnInfo(name = "spreadsheet_name")
    public String spreadsheetName;

    @ColumnInfo(name = "spreadsheet_id")
    public String spreadsheetId;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "sheet_name")
    public String sheetName;

    @ColumnInfo(name = "sheet_id")
    public long sheetId;

    @ColumnInfo(name = "hide_remembered")
    private int hideRemembered = 0;

    // If need update is false then this flag is saying us how old the last check is.
    @Nullable
    @ColumnInfo(name = "update_check")
    public Date successfulUpdateCheck;

    @Nullable
    @ColumnInfo(name = "data_date")
    public Date dataDate;

    @ColumnInfo(name = "need_update", defaultValue = "0")
    public boolean needUpdate;

    // 0 - by date, 1 - by left value, 2 - by right value, 3 - by kind
    @ColumnInfo(name = "sorted_type")
    private int sortedType = 0;

    @ColumnInfo(name = "passed_percentage")
    private float passedPercentage = 0;

    @ColumnInfo(name = "count")
    private int count;

    @ColumnInfo(name = "remembered_count")
    private int rememberedCount;

    @ColumnInfo(name = "can_copy", defaultValue = "1")
    public boolean canCopy;

    @ColumnInfo(name = "today_score")
    public long todayScore;

    @ColumnInfo(name = "timestamp_for_today_score")
    public long timestampForTodayScore;

    @Ignore
    transient
    public static final int SORTED_BY_DATE = 0;

    @Ignore
    transient
    public boolean isChecked;

    @Ignore
    transient
    public static final int SORTED_BY_LEFT = 1;

    @Ignore
    transient
    public static final int SORTED_BY_RIGHT = 2;

    @Ignore
    transient
    public static final int SORTED_BY_KIND = 3;

    public Dictionary(long id, long shelfId, String name) {
        this.id = id;
        this.shelfId = shelfId;
        this.name = name;
        this.leftLocaleAbb = SupportedLanguages.noneAbbreviation;
        this.rightLocaleAbb = SupportedLanguages.noneAbbreviation;
    }

    @Ignore
    public Dictionary(long shelfId, String name) {
        this.shelfId = shelfId;
        this.name = name;
        this.leftLocaleAbb = SupportedLanguages.noneAbbreviation;
        this.rightLocaleAbb = SupportedLanguages.noneAbbreviation;
    }

    public static Dictionary buildDefault(@Nullable String name) {
        Dictionary dictionary = new Dictionary(0, name == null ? StaticUtils.getString(R.string.new_dictionary) : name);
        dictionary.sheetId = -1;
        dictionary.setLeftLocaleAbb(SupportedLanguages.convertToCorrect(null));
        dictionary.setRightLocaleAbb(SupportedLanguages.convertToCorrect(null));
        dictionary.dataDate = DateUtils.getCurrentDate();
        dictionary.successfulUpdateCheck = DateUtils.getCurrentDate();
        dictionary.canCopy = true;
        return dictionary;
    }

    public Dictionary copy() {
        Dictionary dictionary = new Dictionary(this.shelfId, this.name);
        dictionary.leftLocaleAbb = this.leftLocaleAbb;
        dictionary.rightLocaleAbb = this.rightLocaleAbb;
        dictionary.hideRemembered = this.hideRemembered;
        dictionary.passedPercentage = this.passedPercentage;
        dictionary.sortedType = this.sortedType;
        dictionary.canCopy = this.canCopy;
        dictionary.spreadsheetId = this.spreadsheetId;
        dictionary.sheetName = this.sheetName;
        dictionary.rememberedCount = this.rememberedCount;
        dictionary.count = this.count;
        if (this.dataDate != null) {
            dictionary.dataDate = (Date) this.dataDate.clone();
        }
        if (this.successfulUpdateCheck != null) {
            dictionary.successfulUpdateCheck = (Date) this.successfulUpdateCheck.clone();
        }
        dictionary.needUpdate = this.needUpdate;
        dictionary.sheetId = this.sheetId;
        dictionary.spreadsheetName = this.spreadsheetName;
        dictionary.todayScore = this.todayScore;
        dictionary.timestampForTodayScore = this.timestampForTodayScore;
        dictionary.author = this.author;
        return dictionary;
    }

    @Override
    public boolean needCheckUpdate(Date date) {
        return this.successfulUpdateCheck == null || DateUtils.getMinutesBetweenDates(this.successfulUpdateCheck, date) >= Spec.TIME_TO_DICTIONARY_CHECK_UPDATE;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setShelfId(long shelfId) {
        this.shelfId = shelfId;
    }

    public long getShelfId() {
        return this.shelfId;
    }

    public @NonNull
    String getLeftLocaleAbb() {
        return leftLocaleAbb;
    }

    public @NonNull
    String getRightLocaleAbb() {
        return rightLocaleAbb;
    }

    public void setLeftLocaleAbb(String leftLocaleAbb) {
        if (leftLocaleAbb == null || rightLocaleAbb.isEmpty()) {
            leftLocaleAbb = SupportedLanguages.noneAbbreviation;
        }
        this.leftLocaleAbb = leftLocaleAbb;
    }

    public void setRightLocaleAbb(String rightLocaleAbb) {
        if (rightLocaleAbb == null || rightLocaleAbb.isEmpty()) {
            rightLocaleAbb = SupportedLanguages.noneAbbreviation;
        }
        this.rightLocaleAbb = rightLocaleAbb;
    }

    public void setHideRemembered(int hideRemembered) {
        this.hideRemembered = hideRemembered;
    }

    // 0 - by date, 1 - by left value, 2 - by right value, 3 - by kind
    public void setSortedType(int sortedType) {
        this.sortedType = sortedType;
    }

    public void setPassedPercentage(float percentage) {
        this.passedPercentage = percentage;
    }

    public void setRememberedCount(int rememberedCount) {
        this.rememberedCount = rememberedCount;
    }

    public int getRememberedCount() {
        return this.rememberedCount;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }

    public int getHideRemembered() {
        return this.hideRemembered;
    }

    // 0 - by date, 1 - by left value, 2 - by right value, 3 - by kind
    public int getSortedType() {
        return this.sortedType;
    }

    public float getPassedPercentage() {
        return this.passedPercentage;
    }

    @Override
    public boolean hasOwner() {
        return spreadsheetId != null && !spreadsheetId.isEmpty() && sheetName != null && !sheetName.isEmpty();
    }

    public boolean hasOwner(@Nullable String spreadsheetId, @Nullable String sheetName) {
        if (spreadsheetId == null || spreadsheetId.isEmpty() || sheetName == null || sheetName.isEmpty()) {
            return false;
        }
        return spreadsheetId.equals(this.spreadsheetId) && sheetName.equals(this.sheetName);
    }

    @Override
    @Nullable
    public String getSpreadsheetId() {
        return spreadsheetId;
    }

    @Override
    public long getSheetId() {
        return sheetId;
    }

    @Override
    public void setSheetId(long sheetId) {
        this.sheetId = sheetId;
    }

    @Override
    public boolean needUpdate() {
        return needUpdate;
    }

    @Override
    @Nullable
    public Date getLastUpdatedDate() {
        return dataDate;
    }

    @Override
    @NonNull
    public String getSheetName() {
        return sheetName;
    }

    @Override
    public int getType() {
        return DICTIONARY;
    }

    @Ignore
    public static float calculatePercentage(@Nullable ArrayList<Sample> samples) {
        if (samples == null || samples.isEmpty()) {
            return 0;
        }
        int maxItemsPercentage = samples.size() * 2 * 100;
        int result = 0;
        for (Sample sample : samples) {
            result += (sample.getLeftPercentage() + sample.getRightPercentage());
        }
        return (result / (float) maxItemsPercentage) * 100.0f;
    }
}
