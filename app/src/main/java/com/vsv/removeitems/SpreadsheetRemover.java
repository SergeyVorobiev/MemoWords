package com.vsv.removeitems;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.DeleteSpreadsheetDialog;
import com.vsv.dialogs.SSSendDialog;
import com.vsv.dialogs.SingleWindow;
import com.vsv.memorizer.adapters.RecyclerSpreadsheetsAdapter;
import com.vsv.statics.WeakContext;

public class SpreadsheetRemover extends AbstractRemover {

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getBindingAdapterPosition();
            final RecyclerSpreadsheetsAdapter adapter = (RecyclerSpreadsheetsAdapter) viewHolder.getBindingAdapter();
            RecyclerSpreadsheetsAdapter.CardHolder holder = (RecyclerSpreadsheetsAdapter.CardHolder) viewHolder;
            assert adapter != null;
            SpreadSheetInfo spreadsheet = adapter.getItem(position);
            if (SingleWindow.isShownToast()) {
                adapter.notifyItemChanged(position);
                return;
            }
            if (direction == ItemTouchHelper.LEFT) {
                String name = holder.getName().getText().toString();
                DeleteSpreadsheetDialog dialog = new DeleteSpreadsheetDialog(name);
                dialog.cancelButton.setOnClickListener((v) -> {
                    adapter.notifyItemChanged(position);
                    dialog.cancel();
                });
                dialog.deleteButton.setOnClickListener((v) -> {
                    WeakContext.getMainActivity().getMainModel().getSpreadsheetsRepository().delete(spreadsheet);
                    dialog.cancel();
                });
                dialog.show();
            } else {
                adapter.notifyItemChanged(position);
                new SSSendDialog(spreadsheet).show();
            }
        }
    };

    public SpreadsheetRemover() {
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
    }

    @Override
    public void clearData() {

    }
}
