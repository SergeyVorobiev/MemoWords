package com.vsv.statics;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.vsv.db.entities.Settings;
import com.vsv.memorizer.R;
import com.vsv.toasts.Toasts;
import com.vsv.utils.StaticUtils;

import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

public final class GlobalData {

    private GlobalData() {

    }

    public static String inputSpreadsheetId;

    public static String[] TYPES = new String[3];

    public static String presetsSpreadsheetId = "";

    public static String presetsSheetName = "";

    public static TreeMap<Long, String> dictSearchQuery;

    public static TreeMap<Long, String> sampleSearchQuery;

    public static TreeMap<Long, Boolean> reverse;

    public static Drawable bg_dict;

    public static Drawable bg_default;

    public static Drawable bg_sample;

    public static Drawable bg_notebook;

    public static Drawable bg_shelf;

    public static Drawable bg_spreadsheet;

    public static String shelfSearchQuery;

    public static String notebookSearchQuery;

    public static String noteSearchQuery;

    public static String spreadsheetQuery;

    public static String spreadsheetTabQuery;

    public static GoogleSignInAccount account;

    public static GoogleSignInOptions gso;

    public static int lastFragmentId = 0;

    private static Settings settings;

    public static AtomicReference<Drawable> googleAccountDrawable;

    public static void init() {
        TYPES[0] = StaticUtils.getString(R.string.spreadsheet_type0);
        TYPES[1] = StaticUtils.getString(R.string.spreadsheet_type1);
        TYPES[2] = StaticUtils.getString(R.string.spreadsheet_type2);
        dictSearchQuery = new TreeMap<>();
        sampleSearchQuery = new TreeMap<>();
        reverse = new TreeMap<>();
        googleAccountDrawable = new AtomicReference<>();
    }

    public static boolean getReverse(long dictionaryId) {
        Boolean result = reverse.getOrDefault(dictionaryId, false);
        if (result == null) {
            return false;
        }
        return result;
    }

    public static GoogleSignInClient getGoogleClient(Context context) {
        return GoogleSignIn.getClient(context, gso);
    }

    public static int getAccountDrawableId() {
        return account == null ? R.drawable.ic_acc : R.drawable.ic_acc_g;
    }

    public static Drawable getAccountDrawable() {
        return StaticUtils.getDrawable(getAccountDrawableId());
    }

    public static @Nullable
    GoogleSignInAccount getAccountOrToast() {
        if (account == null) {
            Toasts.needLogin();
        }
        return account;
    }

    @MainThread
    public static @NonNull
    Settings getSettings() {
        return settings;
    }

    @MainThread
    public static void setSettings(@NonNull Settings settings) {
        GlobalData.settings = settings;
    }
}
