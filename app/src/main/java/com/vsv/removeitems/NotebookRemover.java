package com.vsv.removeitems;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.Note;
import com.vsv.db.entities.Notebook;
import com.vsv.dialogs.DeleteNotebookDialog;
import com.vsv.dialogs.SendNotebookDialog;
import com.vsv.dialogs.SingleWindow;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.io.Storage;
import com.vsv.io.StorageCSV;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerNotebooksAdapter;
import com.vsv.spreadsheet.SheetUpdater;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;
import com.vsv.utils.StaticUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class NotebookRemover extends AbstractRemover {

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getBindingAdapterPosition();
            final RecyclerNotebooksAdapter adapter = (RecyclerNotebooksAdapter) viewHolder.getBindingAdapter();
            assert adapter != null;
            Notebook notebook = adapter.getItem(position);
            if (SingleWindow.isShownToast()) {
                adapter.notifyItemChanged(position);
                return;
            }
            if (direction == ItemTouchHelper.LEFT) {
                RecyclerNotebooksAdapter.CardHolder holder = (RecyclerNotebooksAdapter.CardHolder) viewHolder;
                String name = holder.getName().getText().toString();
                DeleteNotebookDialog dialog = new DeleteNotebookDialog(name);
                dialog.setOnDeleteListener(() -> StaticUtils.getModel().getNotebooksRepository().delete(adapter.getItem(position)));
                dialog.setOnCancelListener(() -> adapter.notifyItemChanged(position));
                dialog.show();
            } else {
                int count = (int) StaticUtils.getModel().getNotebooksRepository().countOrDefault(5, -1);
                SendNotebookDialog dialog = new SendNotebookDialog(notebook, count);
                dialog.setOnDismissListener((f) -> adapter.notifyItemChanged(position));
                dialog.setOkListener((mode, sheetId, tabName) -> sendNotebook(mode, notebook, sheetId, tabName));
                dialog.show();
            }
        }

        private void sendNotebook(int mode, @NonNull Notebook notebook, String spreadsheetId, String sheetName) {
            if (mode == SendNotebookDialog.SEND_TO_SHEET) {
                send(notebook, spreadsheetId, sheetName);
            } else if (mode == SendNotebookDialog.COPY) {
                copy(notebook);
            } else if (mode == SendNotebookDialog.SHARE) {
                share(notebook);
            } else if (mode == SendNotebookDialog.SHARE_SHEET) {
                shareNotebookSS(notebook);
            }
        }

        private void shareNotebookSS(@NonNull Notebook notebook) {
            File sendFile = StorageCSV.createTemporaryFile(R.string.toast_cannot_share_notebook, StaticUtils.getString(R.string.share_file_name_notebook));
            boolean result = Storage.getDocumentsStorage().loadNotebookSSIntoFile(notebook, sendFile);
            if (!result) {
                Toasts.cannotShareNotebook();
                return;
            }
            StorageCSV.sendFile(sendFile);
        }

        private void share(@NonNull Notebook notebook) {
            BackgroundTask<ArrayList<Note>> backgroundTask = new BackgroundTask<>(12, () -> StaticUtils.getModel().getNotesRepository().getNotes(notebook.getId()).get(10, TimeUnit.SECONDS));
            backgroundTask.setRunMainThreadOnFail((e) -> {
                Log.e("Share notebook", e.toString());
                Toasts.cannotShareNotebook();
            });
            backgroundTask.setRunMainThreadOnSuccess((notes) -> {
                if (notes == null) {
                    Toasts.cannotShareNotebook();
                    return;
                }
                File sendFile = StorageCSV.createTemporaryFile(R.string.toast_cannot_share_notebook, StaticUtils.getString(R.string.share_file_name_notebook));
                boolean result = Storage.getDocumentsStorage().loadNotebookWithNotesIntoFile(notebook, notes, sendFile);
                if (!result) {
                    Toasts.cannotShareNotebook();
                    return;
                }
                StorageCSV.sendFile(sendFile);
            });
            backgroundTask.buildWaitDialog().showOver();
        }

        private void copy(Notebook notebook) {
            BackgroundTask<ArrayList<Note>> backgroundTask = new BackgroundTask<>(12, () -> StaticUtils.getModel().getNotesRepository().getNotes(notebook.getId()).get(10, TimeUnit.SECONDS));
            backgroundTask.setRunMainThreadOnFail((e) -> {
                Log.e("Copy notebook", e.toString());
                Toasts.readFromNotebookError(notebook.getName());
            });
            backgroundTask.setRunMainThreadOnSuccess((notes) -> {
                ArrayList<Note> copyNotes = new ArrayList<>();
                for (Note note : notes) {
                    copyNotes.add(note.copy());
                }
                StaticUtils.getModel().getNotebookWithNotesRepository().insertWithSamples(notebook.copy(), copyNotes);
                Toasts.success();
            });
            backgroundTask.buildWaitDialog().showOver();
        }

        private void send(Notebook notebook, String spreadsheetId, String sheetName) {
            if (spreadsheetId == null || spreadsheetId.isEmpty()) {
                Toasts.sheetNotSpecified();
                return;
            }
            if (sheetName == null || sheetName.isEmpty()) {
                Toasts.sheetTabNotSpecified();
                return;
            }
            SheetUpdater.writeNotebookToSheet(GlobalData.account, spreadsheetId, sheetName, notebook);
        }
    };

    public NotebookRemover() {
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
    }

    public void clearData() {
        this.itemTouchHelper.attachToRecyclerView(null);
    }
}
