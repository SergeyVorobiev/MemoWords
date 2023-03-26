package com.vsv.dialogs;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vsv.db.entities.Note;
import com.vsv.dialogs.listeners.NoteUpdateListener;
import com.vsv.memorizer.R;
import com.vsv.toasts.Toasts;
import com.vsv.utils.SheetDataBuilder;

public class UpdateNoteDialog extends SingleCustomDialog {

    private TextView header;

    private TextView content;

    private TextView numberView;

    private NoteUpdateListener listener;

    private final Note note;

    public UpdateNoteDialog(@NonNull Note note) {
        super(R.layout.dialog_update_note, false, true);
        this.note = note;
        dialogView.findViewById(R.id.newOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.newCancel).setOnClickListener(this::onClickCancel);
        header.setText(note.getName());
        numberView.setText(String.valueOf(note.getNumber()));
        content.setText(note.getContent());
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
            note.setName(headerString);
            note.setContent(contentString);
            note.setNumber(number);
            listener.updateNote(note);
            dialog.cancel();
        }
    }

    public void setNoteUpdateListener(NoteUpdateListener listener) {
        this.listener = listener;
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {
        header = dialogView.findViewById(R.id.header);
        content = dialogView.findViewById(R.id.content);
        numberView = dialogView.findViewById(R.id.number);
    }

    @Override
    public void setupViewListeners(View dialogView) {

    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }
}
