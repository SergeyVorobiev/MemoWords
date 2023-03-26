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

import com.vsv.utils.DateUtils;
import com.vsv.utils.HashGenerator;

import java.util.Date;

@Entity(tableName = "sample", foreignKeys = {@ForeignKey(entity = Dictionary.class,
        parentColumns = "id",
        childColumns = "dictionaryId",
        onDelete = ForeignKey.CASCADE)},
        indices = {
                @Index("dictionaryId"),
        })
@TypeConverters({Converter.class})
public class Sample {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long dictionaryId;

    @NonNull
    @ColumnInfo(name = "left_value", defaultValue = "")
    private String leftValue;

    @NonNull
    @ColumnInfo(name = "right_value", defaultValue = "")
    private String rightValue;

    @ColumnInfo(name = "left_answered", defaultValue = "0")
    private int leftAnswered;

    @ColumnInfo(name = "google_translate_left", defaultValue = "0")
    public int googleTranslateLeft;

    @ColumnInfo(name = "google_translate_right", defaultValue = "0")
    public int googleTranslateRight;

    @ColumnInfo(name = "right_answered", defaultValue = "0")
    private int rightAnswered;

    @ColumnInfo(name = "left_percentage", defaultValue = "0")
    private int leftPercentage;

    @ColumnInfo(name = "right_percentage", defaultValue = "0")
    private int rightPercentage;

    @ColumnInfo(name = "last_correct")
    public boolean lastCorrect;

    @ColumnInfo(name = "correct_series")
    public int correctSeries;

    @NonNull
    @ColumnInfo(name = "type", defaultValue = "")
    private String type;

    @NonNull
    @ColumnInfo(name = "example", defaultValue = "")
    private String example;

    @Ignore
    transient
    public int tempIndex = -1;

    @Ignore
    transient
    public boolean showExample = false;

    // It is used when the sample is prepared to train.
    @Ignore
    transient
    public String answerString = "";

    // It is used when the sample is prepared to train.
    @Ignore
    transient
    public String partAnswerString = "";

    @Nullable
    @ColumnInfo(name = "answered_date")
    public Date answeredDate;

    public static final int LOCK_SERIES_COUNT = 3;

    // In minutes
    public static final int UNLOCK_TIME = 360;

    // In minutes, Time during which the correct series will count up.
    public static final int SERIES_TIME = 60;

    public Sample(long id, long dictionaryId, @NonNull String leftValue, @NonNull String rightValue, int leftAnswered, int rightAnswered) {
        this.id = id;
        this.dictionaryId = dictionaryId;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.leftAnswered = leftAnswered;
        this.rightAnswered = rightAnswered;
        this.leftPercentage = 0;
        this.rightPercentage = 0;
        this.type = "";
        this.example = "";
    }

    @Ignore
    public Sample(long dictionaryId, @NonNull String leftValue, @NonNull String rightValue, int leftAnswered, int rightAnswered) {
        this.dictionaryId = dictionaryId;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.leftAnswered = leftAnswered;
        this.rightAnswered = rightAnswered;
        this.leftPercentage = 0;
        this.rightPercentage = 0;
        this.type = "";
        this.example = "";
    }

    // Make a copy of the sample without its id.
    public Sample copy() {
        Sample sample = new Sample(this.dictionaryId, this.leftValue, this.rightValue, this.leftAnswered, this.rightAnswered);
        sample.setLeftPercentage(this.leftPercentage);
        sample.setRightPercentage(this.rightPercentage);
        sample.setType(this.getType());
        sample.setExample(this.getExample());
        if (this.answeredDate != null) {
            sample.answeredDate = (Date) this.answeredDate.clone();
        }
        sample.lastCorrect = this.lastCorrect;
        sample.correctSeries = this.correctSeries;
        sample.googleTranslateLeft = this.googleTranslateLeft;
        sample.googleTranslateRight = this.googleTranslateRight;
        return sample;
    }

    public boolean isLocked() {
        return correctSeries >= LOCK_SERIES_COUNT && !isRemembered();
    }

    public long getRemainedLockTime() {
        return Math.max(0, UNLOCK_TIME - getPastTime());
    }

    public String buildMD5() {
        return HashGenerator.getMD5(leftValue + rightValue + leftPercentage + rightPercentage);
    }

    public boolean needUnlock() {
        if (answeredDate == null && isLocked()) {
            return true;
        }
        return isLocked() && getPastTime() >= UNLOCK_TIME;
    }

    // In minutes
    public long getPastTime() {
        if (answeredDate == null) {
            return 0;
        }
        return DateUtils.getMinutesBetweenDates(this.answeredDate, DateUtils.getCurrentDate());
    }

    public void unlock() {
        correctSeries = 0;
    }

    public boolean unlockIfNeed() {
        if (needUnlock()) {
            unlock();
            return true;
        }
        return false;
    }

    public int getLeftAnswered() {
        return leftAnswered;
    }

    public void setLeftAnswered(int leftAnswered) {
        this.leftAnswered = leftAnswered;
    }

    public int getRightAnswered() {
        return rightAnswered;
    }

    public void setRightAnswered(int rightAnswered) {
        this.rightAnswered = rightAnswered;
    }

    public @NonNull
    String getLeftValue() {
        return this.leftValue;
    }

    public @NonNull
    String getRightValue() {
        return this.rightValue;
    }

    public void setLeftValue(@NonNull String leftValue) {
        this.leftValue = leftValue;
    }

    public void setRightValue(@NonNull String rightValue) {
        this.rightValue = rightValue;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDictionaryId() {
        return this.dictionaryId;
    }

    public boolean isGoogleTranslateLeft() {
        return googleTranslateLeft != 0;
    }

    public boolean isGoogleTranslateRight() {
        return googleTranslateRight != 0;
    }

    public void setDictionaryId(long dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    // 0 - 100
    public int getRightPercentage() {
        return rightPercentage;
    }

    // 0 - 100
    public int getLeftPercentage() {
        return leftPercentage;
    }

    // 0 - 100
    public void setLeftPercentage(int leftPercentage) {
        this.leftPercentage = leftPercentage;
    }

    // 0 - 100
    public void setRightPercentage(int rightPercentage) {
        this.rightPercentage = rightPercentage;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public String getType() {
        return this.type;
    }

    public void setExample(@NonNull String example) {
        this.example = example;
    }

    @NonNull
    public String getExample() {
        return this.example;
    }

    public boolean isRemembered() {
        return leftPercentage + rightPercentage == 200;
    }
}
