package com.vsv.bundle.entities;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.bundle.helpers.BundleNames;
import com.vsv.db.entities.Shelf;

import java.util.Objects;

public class ShelfBundle implements Parcelable {

    public final long id;

    public final String name;

    public boolean sorted;

    public boolean hideRemembered;

    public int fontDictTitleIndex;

    public ShelfBundle(Shelf shelf) {
        this.id = shelf.getId();
        this.name = shelf.getName();
        this.hideRemembered = shelf.hideRemembered;
        this.sorted = shelf.sorted;
        this.fontDictTitleIndex = shelf.fontDictTitleIndex;
    }

    protected ShelfBundle(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.sorted = in.readInt() == 1;
        this.hideRemembered = in.readInt() == 1;
        this.fontDictTitleIndex = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.sorted ? 1 : 0);
        dest.writeInt(this.hideRemembered ? 1 : 0);
        dest.writeInt(this.fontDictTitleIndex);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public @NonNull
    Shelf convertToShelf() {
        Shelf shelf = new Shelf(this.id, this.name);
        shelf.sorted = this.sorted;
        shelf.hideRemembered = this.hideRemembered;
        shelf.fontDictTitleIndex = this.fontDictTitleIndex;
        return shelf;
    }

    @NonNull
    public Bundle toBundle(@NonNull Bundle bundle) {
        bundle.putParcelable(BundleNames.SHELF, this);
        return bundle;
    }

    @NonNull
    public Bundle toNewBundle() {
        return toBundle(new Bundle());
    }

    @NonNull
    public static Bundle toNewBundle(@NonNull Shelf shelf) {
        return new ShelfBundle(shelf).toNewBundle();
    }

    public static void toBundle(@NonNull Bundle bundle, Shelf shelf) {
        bundle.putParcelable(BundleNames.SHELF, new ShelfBundle(shelf));
    }

    public static @NonNull
    ShelfBundle fromBundle(@NonNull Bundle bundle) {
        return Objects.requireNonNull(fromBundleOrNull(bundle));
    }

    public static @Nullable
    ShelfBundle fromBundleOrNull(@Nullable Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return bundle.getParcelable(BundleNames.SHELF);
    }

    public static @NonNull
    Shelf fromBundleToShelf(@NonNull Bundle bundle) {
        return fromBundle(bundle).convertToShelf();
    }

    public static final Creator<ShelfBundle> CREATOR = new Creator<ShelfBundle>() {

        @Override
        public ShelfBundle createFromParcel(Parcel in) {
            return new ShelfBundle(in);
        }

        @Override
        public ShelfBundle[] newArray(int size) {
            return new ShelfBundle[size];
        }
    };
}
