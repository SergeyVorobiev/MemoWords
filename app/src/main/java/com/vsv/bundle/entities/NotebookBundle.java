package com.vsv.bundle.entities;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.bundle.helpers.BundleNames;
import com.vsv.db.entities.Converter;
import com.vsv.db.entities.Notebook;

import java.util.Date;
import java.util.Objects;

public class NotebookBundle implements Parcelable {

    public final long id;

    public final String name;

    public final String sheetName;

    public final String spreadsheetId;

    public final boolean canCopy;

    public boolean sorted;

    public int fontNotesTitleIndex;

    public int notesCount;

    public Date updateCheck;

    public Date dataDate;

    public boolean needUpdate;

    public long sheetId;

    public String spreadsheetName;

    public String author;

    public NotebookBundle(Notebook notebook) {
        this.id = notebook.getId();
        this.name = notebook.getName();
        this.canCopy = notebook.canCopy;
        this.sorted = notebook.sorted;
        this.sheetName = notebook.sheetName;
        this.spreadsheetId = notebook.spreadsheetId;
        this.fontNotesTitleIndex = notebook.fontNotesTitleIndex;
        this.notesCount = notebook.notesCount;
        this.updateCheck = notebook.updateCheck;
        this.dataDate = notebook.dataDate;
        this.needUpdate = notebook.needUpdate;
        this.sheetId = notebook.sheetId;
        this.spreadsheetName = notebook.spreadsheetName;
        this.author = notebook.author;
    }

    protected NotebookBundle(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.canCopy = in.readInt() == 1;
        this.sorted = in.readInt() == 1;
        this.spreadsheetId = in.readString();
        this.sheetName = in.readString();
        this.fontNotesTitleIndex = in.readInt();
        this.notesCount = in.readInt();
        long dataDate = in.readLong();
        long successDate = in.readLong();
        this.dataDate = dataDate == -1 ? null : Converter.fromTimestamp(dataDate);
        this.updateCheck = successDate == -1 ? null : Converter.fromTimestamp(successDate);
        this.needUpdate = in.readInt() == 1;
        this.sheetId = in.readLong();
        this.spreadsheetName = in.readString();
        this.author = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.canCopy ? 1 : 0);
        dest.writeInt(this.sorted ? 1 : 0);
        dest.writeString(this.spreadsheetId);
        dest.writeString(this.sheetName);
        dest.writeInt(this.fontNotesTitleIndex);
        dest.writeInt(notesCount);
        dest.writeLong(this.dataDate == null ? -1 : Converter.dateToTimestamp(dataDate));
        dest.writeLong(this.updateCheck == null ? -1 : Converter.dateToTimestamp(updateCheck));
        dest.writeInt(this.needUpdate ? 1 : 0);
        dest.writeLong(this.sheetId);
        dest.writeString(this.spreadsheetName);
        dest.writeString(this.author);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public @NonNull
    Notebook convertToNotebook() {
        Notebook notebook = new Notebook(this.id, this.name);
        notebook.canCopy = this.canCopy;
        notebook.sorted = this.sorted;
        notebook.spreadsheetId = this.spreadsheetId;
        notebook.sheetName = this.sheetName;
        notebook.fontNotesTitleIndex = this.fontNotesTitleIndex;
        notebook.notesCount = this.notesCount;
        notebook.dataDate = this.dataDate;
        notebook.updateCheck = this.updateCheck;
        notebook.needUpdate = this.needUpdate;
        notebook.sheetId = this.sheetId;
        notebook.spreadsheetName = this.spreadsheetName;
        notebook.author = this.author;
        return notebook;
    }

    @NonNull
    public Bundle intoBundle(@NonNull Bundle bundle) {
        bundle.putParcelable(BundleNames.NOTEBOOK, this);
        return bundle;
    }

    @NonNull
    public Bundle intoNewBundle() {
        return intoBundle(new Bundle());
    }

    @NonNull
    public static Bundle intoNewBundle(@NonNull Notebook notebook) {
        return new NotebookBundle(notebook).intoNewBundle();
    }

    public static void intoBundle(@NonNull Bundle bundle, Notebook notebook) {
        bundle.putParcelable(BundleNames.NOTEBOOK, new NotebookBundle(notebook));
    }

    public static @NonNull
    NotebookBundle fromBundle(@NonNull Bundle bundle) {
        return Objects.requireNonNull(fromBundleOrNull(bundle));
    }

    public static @Nullable
    NotebookBundle fromBundleOrNull(@Nullable Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getParcelable(BundleNames.NOTEBOOK);
    }

    public static @NonNull
    Notebook fromBundleToNotebook(@NonNull Bundle bundle) {
        return fromBundle(bundle).convertToNotebook();
    }

    public static final Creator<NotebookBundle> CREATOR = new Creator<NotebookBundle>() {

        @Override
        public NotebookBundle createFromParcel(Parcel in) {
            return new NotebookBundle(in);
        }

        @Override
        public NotebookBundle[] newArray(int size) {
            return new NotebookBundle[size];
        }
    };
}
