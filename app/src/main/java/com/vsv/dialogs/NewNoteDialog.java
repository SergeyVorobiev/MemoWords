package com.vsv.dialogs;

import android.view.View;
import android.widget.TextView;

import com.vsv.dialogs.listeners.NoteCreateListener;
import com.vsv.memorizer.R;
import com.vsv.toasts.Toasts;
import com.vsv.utils.SheetDataBuilder;

public class NewNoteDialog extends SingleCustomDialog {

    private TextView header;

    private TextView content;

    private final TextView numberView;

    private NoteCreateListener listener;

    public NewNoteDialog(int notesCount) {
        super(R.layout.dialog_new_note, false, true);
        dialogView.findViewById(R.id.newOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.newCancel).setOnClickListener(this::onClickCancel);
        numberView = dialogView.findViewById(R.id.number);
        numberView.setText(String.valueOf(notesCount + 1));
    }

    private void onClickOk(View view) {
        String headerString = header.getText().toString().trim();
        String contentString = content.getText().toString().trim();
        String strNumber = numberView.getText().toString().trim();
        int number = 0;
        if (!strNumber.isEmpty()) {
            number = Integer.parseInt(strNumber);
        }
        if (SheetDataBuilder.isHtmlStringNullOrEmpty(headerString)) {
            Toasts.noteHeaderEmpty();
        } else if (SheetDataBuilder.isHtmlStringNullOrEmpty(contentString)) {
            Toasts.noteContentEmpty();
        } else {
            listener.createNote(headerString, contentString, number);
            dialog.cancel();
        }
    }

    public void setNoteCreateListener(NoteCreateListener listener) {
        this.listener = listener;
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {
        header = dialogView.findViewById(R.id.header);
        content = dialogView.findViewById(R.id.content);

    }

    @Override
    public void setupViewListeners(View dialogView) {

    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }
}
