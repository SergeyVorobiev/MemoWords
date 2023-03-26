package com.vsv.utils;

import android.util.Log;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.vsv.memorizer.R;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class GoogleTasksExceptionHandler {

    public static final int NO_INTERNET = 0;

    public static final int UNEXPECTED = -1;

    public static final int NO_SPREADSHEET = 1;

    public static final int BAD_REQUEST = 2;

    public static final int NO_PERMISSION = 3;

    public static int getErrorCode(Throwable e) {
        if (e instanceof UnknownHostException) {
            return NO_INTERNET;
        } else if (e instanceof GoogleJsonResponseException) {
            GoogleJsonResponseException exc = (GoogleJsonResponseException) e;
            Integer code = (Integer) exc.getDetails().get("code");
            if (code == null) {
                return UNEXPECTED;
            } else {
                if (code == 404) {
                    return NO_SPREADSHEET;
                } else if (code == 400) {
                    return BAD_REQUEST;
                } else if (code == 403) {
                    return NO_PERMISSION;
                } else {
                    return UNEXPECTED;
                }
            }
        } else {
            Log.e("SheetError", e.getClass() + "\n" + e.getMessage());
            return UNEXPECTED;
        }
    }

    public static String handle(Throwable e) {
        if (e instanceof ExecutionException) {
            Log.e("SheetError", e.toString());
            e = e.getCause();
        }
        if (e == null) {
            return StaticUtils.getString(R.string.unexpected_error);
        }
        if (e instanceof AppException) {
            return StaticUtils.getString(((AppException) e).getMessageId());
        }
        if (e instanceof TimeoutException) {
            return StaticUtils.getString(R.string.timeout);
        } else if (e instanceof UnknownHostException) {
            return StaticUtils.getString(R.string.no_internet);
        } else if (e instanceof GoogleJsonResponseException) {
            GoogleJsonResponseException exc = (GoogleJsonResponseException) e;
            Integer code = (Integer) exc.getDetails().get("code");
            Object message = exc.getDetails().get("message");
            if (code == null) {
                return StaticUtils.getString(R.string.unexpected_error);
            } else {
                if (code == 404) {
                    return StaticUtils.getString(R.string.no_sheet);
                } else if (code == 403) {
                    return StaticUtils.getString(R.string.no_permission);
                } else {
                    if (message != null) {
                        Log.e("SheetError", message.toString());
                        return message.toString();
                    } else {
                        Log.e("SheetError", exc.getClass() + "\n" + exc.getMessage());
                        return StaticUtils.getString(R.string.unexpected_error);
                    }
                }
            }
        } else {
            Log.e("SheetError", e.getClass() + "\n" + e.getMessage());
            return StaticUtils.getString(R.string.unexpected_error);
        }
    }
}
