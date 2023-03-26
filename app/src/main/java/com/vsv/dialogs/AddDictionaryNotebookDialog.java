package com.vsv.dialogs;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vsv.memorizer.R;
import com.vsv.toasts.Toasts;

public class AddDictionaryNotebookDialog extends SingleCustomDialog {

    public interface OkListener {
        void clickOk(@NonNull String dictionaryName);
    }

    public static final int DICTIONARY = 0;

    public static final int NOTEBOOK = 1;

    public static final int SPREADSHEET = 2;

    private OkListener okListener;

    private TextView nameView;

    private View cancel;

    public AddDictionaryNotebookDialog(String name, int type) {
        super(R.layout.dialog_add_dictionary_notebook, false, true);
        ((TextView) dialogView.findViewById(R.id.addInput)).setText(name);
        if (type == NOTEBOOK) {
            ((TextView) dialogView.findViewById(R.id.addHeader)).setText(R.string.dialog_add_notebook_header);
        } else if (type == SPREADSHEET) {
            ((TextView) dialogView.findViewById(R.id.addHeader)).setText(R.string.dialog_add_spreadsheet_header);
        }
    }

    @Override
    public void setupViews(View dialogView) {
        nameView = dialogView.findViewById(R.id.addInput);
        cancel = dialogView.findViewById(R.id.addCancel);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.addOk).setOnClickListener(this::clickOk);
        cancel.setOnClickListener((view) -> this.cancel());
    }

    private void clickOk(View view) {
        String name = nameView.getText().toString().trim();
        if (name.isEmpty()) {
            Toasts.nameEmpty();
        } else {
            this.cancel();
            okListener.clickOk(name);
        }
    }

    public void setCancelListener(Runnable listener) {
        cancel.setOnClickListener((view) -> {
            this.cancel();
            listener.run();
        });
    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }

    public void setOkListener(@NonNull OkListener listener) {
        this.okListener = listener;
    }
}
