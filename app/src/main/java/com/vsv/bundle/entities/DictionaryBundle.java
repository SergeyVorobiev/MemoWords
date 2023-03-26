package com.vsv.bundle.entities;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.bundle.helpers.BundleNames;
import com.vsv.db.entities.Converter;
import com.vsv.db.entities.Dictionary;
import com.vsv.speech.SupportedLanguages;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DictionaryBundle implements Parcelable {

    public final long id;

    public final String name;

    public final long shelfId;

    public final String leftLocaleAbb;

    public final String rightLocaleAbb;

    public final String spreadsheetId;

    public final String sheetName;

    public int hideRemembered;

    public int sortedType;

    public final boolean canCopy;

    public float passedPercentage;

    public int rememberedCount;

    public int count;

    public Date successfulUpdateCheck;

    public Date dataDate;

    public boolean needUpdate;

    public long sheetId;

    public String spreadsheetName;

    public long todayScore;

    public long timestampForTodayScore;

    public String author;

    public DictionaryBundle(Dictionary dict) {
        this.id = dict.getId();
        this.shelfId = dict.getShelfId();
        this.name = dict.getName();
        this.leftLocaleAbb = dict.getLeftLocaleAbb();
        this.rightLocaleAbb = dict.getRightLocaleAbb();
        this.hideRemembered = dict.getHideRemembered();
        this.sortedType = dict.getSortedType();
        this.passedPercentage = dict.getPassedPercentage();
        this.canCopy = dict.canCopy;
        this.spreadsheetId = dict.spreadsheetId;
        this.sheetName = dict.sheetName;
        this.rememberedCount = dict.getRememberedCount();
        this.dataDate = dict.dataDate;
        this.successfulUpdateCheck = dict.successfulUpdateCheck;
        this.count = dict.getCount();
        this.needUpdate = dict.needUpdate;
        this.sheetId = dict.sheetId;
        this.spreadsheetName = dict.spreadsheetName;
        this.todayScore = dict.todayScore;
        this.timestampForTodayScore = dict.timestampForTodayScore;
        this.author = dict.author;
    }

    protected DictionaryBundle(Parcel in) {
        this.id = in.readLong();
        this.shelfId = in.readLong();
        this.name = in.readString();
        this.leftLocaleAbb = in.readString();
        this.rightLocaleAbb = in.readString();
        this.hideRemembered = in.readInt();
        this.sortedType = in.readInt();
        this.passedPercentage = in.readFloat();
        this.canCopy = in.readInt() == 1;
        this.spreadsheetId = in.readString();
        this.sheetName = in.readString();
        this.rememberedCount = in.readInt();
        this.count = in.readInt();
        long dataDate = in.readLong();
        long successDate = in.readLong();
        this.dataDate = dataDate == -1 ? null : Converter.fromTimestamp(dataDate);
        this.successfulUpdateCheck = successDate == -1 ? null : Converter.fromTimestamp(successDate);
        this.needUpdate = in.readInt() == 1;
        this.sheetId = in.readLong();
        this.spreadsheetName = in.readString();
        this.todayScore = in.readLong();
        this.timestampForTodayScore = in.readLong();
        this.author = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.shelfId);
        dest.writeString(this.name);
        dest.writeString(this.leftLocaleAbb);
        dest.writeString(this.rightLocaleAbb);
        dest.writeInt(this.hideRemembered);
        dest.writeInt(this.sortedType);
        dest.writeFloat(this.passedPercentage);
        dest.writeInt(this.canCopy ? 1 : 0);
        dest.writeString(this.spreadsheetId);
        dest.writeString(this.sheetName);
        dest.writeInt(this.rememberedCount);
        dest.writeInt(this.count);
        dest.writeLong(this.dataDate == null ? -1 : Converter.dateToTimestamp(dataDate));
        dest.writeLong(this.successfulUpdateCheck == null ? -1 : Converter.dateToTimestamp(successfulUpdateCheck));
        dest.writeInt(this.needUpdate ? 1 : 0);
        dest.writeLong(this.sheetId);
        dest.writeString(this.spreadsheetName);
        dest.writeLong(this.todayScore);
        dest.writeLong(this.timestampForTodayScore);
        dest.writeString(this.author);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public @NonNull
    Dictionary convertToDictionary() {
        Dictionary dictionary = new Dictionary(this.id, this.shelfId, this.name);
        dictionary.setLeftLocaleAbb(this.leftLocaleAbb);
        dictionary.setRightLocaleAbb(this.rightLocaleAbb);
        dictionary.setHideRemembered(this.hideRemembered);
        dictionary.setSortedType(this.sortedType);
        dictionary.setPassedPercentage(this.passedPercentage);
        dictionary.canCopy = this.canCopy;
        dictionary.spreadsheetId = this.spreadsheetId;
        dictionary.sheetName = this.sheetName;
        dictionary.setCount(this.count);
        dictionary.setRememberedCount(this.rememberedCount);
        dictionary.dataDate = this.dataDate;
        dictionary.successfulUpdateCheck = this.successfulUpdateCheck;
        dictionary.needUpdate = this.needUpdate;
        dictionary.sheetId = this.sheetId;
        dictionary.spreadsheetName = this.spreadsheetName;
        dictionary.todayScore = this.todayScore;
        dictionary.timestampForTodayScore = this.timestampForTodayScore;
        dictionary.author = this.author;
        return dictionary;
    }

    public @Nullable
    Locale getLeftLocale() {
        return SupportedLanguages.getLocale(this.leftLocaleAbb);
    }

    public @Nullable
    Locale getRightLocale() {
        return SupportedLanguages.getLocale(this.rightLocaleAbb);
    }

    @NonNull
    public Bundle toBundle(@NonNull Bundle bundle) {
        bundle.putParcelable(BundleNames.DICT, this);
        return bundle;
    }

    public static void toBundle(Bundle bundle, Dictionary dictionary) {
        bundle.putParcelable(BundleNames.DICT, new DictionaryBundle(dictionary));
    }

    public static @NonNull
    DictionaryBundle fromBundle(Bundle bundle) {
        return Objects.requireNonNull(bundle.getParcelable(BundleNames.DICT));
    }

    public static @Nullable
    DictionaryBundle fromBundleOrNull(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getParcelable(BundleNames.DICT);
    }

    public static @Nullable
    Dictionary fromBundleToDictionaryOrNull(Bundle bundle) {
        DictionaryBundle dictBundle = fromBundleOrNull(bundle);
        return dictBundle == null ? null : dictBundle.convertToDictionary();
    }

    public static @NonNull
    Dictionary fromBundleToDictionary(Bundle bundle) {
        return fromBundle(bundle).convertToDictionary();
    }

    public static final Creator<DictionaryBundle> CREATOR = new Creator<DictionaryBundle>() {

        @Override
        public DictionaryBundle createFromParcel(Parcel in) {
            return new DictionaryBundle(in);
        }

        @Override
        public DictionaryBundle[] newArray(int size) {
            return new DictionaryBundle[size];
        }
    };
}
