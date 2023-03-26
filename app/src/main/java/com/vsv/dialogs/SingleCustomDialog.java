package com.vsv.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.vsv.statics.WeakContext;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SingleCustomDialog extends SingleWindow {

    protected final @NonNull
    Dialog dialog;

    protected final @NonNull
    Context context;

    protected final @NonNull
    View dialogView;

    protected final @NonNull
    Window window;

    protected final AtomicBoolean canceled = new AtomicBoolean();

    public SingleCustomDialog(int layout, boolean cancelable, boolean transparent) {
        this(WeakContext.getContext(), layout, cancelable, transparent);
    }

    public SingleCustomDialog(@NonNull Context context, int layout, boolean cancelable, boolean transparent) {
        this.context = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        dialogView = View.inflate(context, layout, null);
        builder.setCancelable(cancelable);
        builder.setView(dialogView);
        dialog = builder.create();
        window = Objects.requireNonNull(dialog.getWindow());
        if (transparent) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        setupViews(dialogView);
        setupViewListeners(dialogView);
        setupViewAdjustments(dialogView);
        this.setOnDismissListener((dialogInterface) -> setShown(false));
    }

    protected void showKeyboardFor(@NonNull View inputField) {
        inputField.requestFocus();
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    protected void closeKeyboardFor(@NonNull View inputField) {
        inputField.clearFocus();
        ((InputMethodManager) inputField.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(inputField.getWindowToken(), 0);
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public abstract void setupViews(View dialogView);

    public abstract void setupViewListeners(View dialogView);

    public abstract void setupViewAdjustments(View dialogView);

    @MainThread
    public final void show() {
        if (isShownToast()) {
            return;
        }
        setShown(true);
        dialog.show();
    }

    public void setOnDismissListener(@NonNull DialogInterface.OnDismissListener listener) {
        dialog.setOnDismissListener((dialogInterface -> {
            setShown(false);
            listener.onDismiss(dialogInterface);
        }));
    }

    public void cancel() {
        dialog.cancel();
    }
}
