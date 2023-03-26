package com.vsv.removeitems;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.vsv.dialogs.DeleteSheetDialog;
import com.vsv.dialogs.SingleWindow;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.memorizer.adapters.RecyclerSheetsAdapter;
import com.vsv.spreadsheet.SheetUpdater;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;

import java.util.function.Consumer;

public class SheetRemover {

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getBindingAdapterPosition();
            final RecyclerSheetsAdapter adapter = (RecyclerSheetsAdapter) viewHolder.getBindingAdapter();
            assert adapter != null;
            if (SingleWindow.isShownToast()) {
                adapter.notifyItemChanged(position);
                return;
            }
            SheetTab tab = adapter.getItem(position);
            DeleteSheetDialog deleteDialog = new DeleteSheetDialog(viewHolder.itemView.getContext(), tab.getTitle());
            deleteDialog.setOnDeleteListener(() -> {
                if (GlobalData.account == null) {
                    Toasts.needLogin();
                    adapter.notifyItemChanged(position);
                } else {
                    deleteTab(tab.getId(), GlobalData.account, position);
                }
            });
            deleteDialog.setOnCancelListener(() -> adapter.notifyItemChanged(position));
            deleteDialog.show();
        }

        private void deleteTab(int tabId, @NonNull GoogleSignInAccount account, int position) {
            BackgroundTask<BatchUpdateSpreadsheetResponse> task = SheetUpdater.deleteTabTask(account, sheetId, tabId);
            task.setExtraData(position);
            deleteConsumer.accept(task);
        }
    };

    private final String sheetId;

    private final Consumer<BackgroundTask<BatchUpdateSpreadsheetResponse>> deleteConsumer;

    public SheetRemover(RecyclerView view, String sheetId, Consumer<BackgroundTask<BatchUpdateSpreadsheetResponse>> deleteConsumer) {
        this.deleteConsumer = deleteConsumer;
        this.sheetId = sheetId;
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(view);
    }
}
