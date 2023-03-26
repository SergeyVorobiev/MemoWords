package com.vsv.dialogs;

import android.view.View;
import android.widget.TextView;

import com.vsv.db.entities.Shelf;
import com.vsv.dialogs.listeners.UpdateShelfListener;
import com.vsv.memorizer.R;
import com.vsv.toasts.Toasts;

public class UpdateShelfDialog extends SingleCustomDialog {

    private TextView input;

    private UpdateShelfListener listener;

    private final Shelf shelf;

    public UpdateShelfDialog(Shelf shelf) {
        super(R.layout.dialog_update_shelf, false, true);
        this.shelf = shelf;
        input.setText(shelf.getName());
        input.requestFocus();
        showKeyboardFor(input);
    }

    @Override
    public void setupViews(View dialogView) {
        input = dialogView.findViewById(R.id.newShelfInput);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.newShelfOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.newShelfCancel).setOnClickListener(this::onClickCancel);
    }

    @Override
    public void setupViewAdjustments(View dialogView) {
        TextView header = dialogView.findViewById(R.id.newShelfDialogHeader);
        header.setText(R.string.update_shelf_dialog_header);
    }

    private void onClickOk(View view) {
        String name = input.getText().toString().trim();
        if (name.isEmpty()) {
            Toasts.shelfNameEmpty();
        } else {
            shelf.setName(name);
            listener.updateShelf(shelf);
            dialog.cancel();
        }
    }

    public void updateShelfListener(UpdateShelfListener listener) {
        this.listener = listener;
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }
}

