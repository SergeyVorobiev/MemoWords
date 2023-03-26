package com.vsv.dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.vsv.db.entities.Note;
import com.vsv.db.entities.Notebook;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.dialogs.listeners.UpdateNotebookListener;
import com.vsv.memorizer.R;
import com.vsv.repositories.NotesRepository;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.spreadsheet.entities.SSNotebookData;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.DateUtils;
import com.vsv.utils.GoogleTasksExceptionHandler;
import com.vsv.utils.Spec;
import com.vsv.utils.merger.MergeCollection;
import com.vsv.utils.merger.NoteMerger;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UpdateNotebookDialog extends SingleCustomDialog {

    private TextView input;

    private UpdateNotebookListener listener;

    private final Notebook notebook;

    private SwitchCompat updateFromSheetSwitch;

    private boolean updateFromSheetFlag;

    private CheckBox deleteNotesCheckbox;

    private View updateSheetLayout;

    private final ArrayList<Note> notes;

    public UpdateNotebookDialog(@NonNull Notebook notebook, @NonNull ArrayList<Note> notes) {
        super(R.layout.dialog_update_notebook, false, true);
        this.notebook = notebook;
        this.notes = notes;
        input.setText(notebook.getName());
        input.requestFocus();
        showKeyboardFor(input);
        updateFromSheetFlag = notebook.hasOwner();
        setupLayoutState();
        updateFromSheetSwitch.setEnabled(updateFromSheetFlag);
        updateFromSheetSwitch.setChecked(updateFromSheetFlag);
        ((TextView) dialogView.findViewById(R.id.sheetName)).setText(notebook.sheetName);
        ((TextView) dialogView.findViewById(R.id.spreadsheetId)).setText(notebook.spreadsheetId);
        ((TextView) dialogView.findViewById(R.id.spreadsheetName)).setText(notebook.spreadsheetName);
        TextView updatedDate = dialogView.findViewById(R.id.updatedDate);
        updatedDate.setText(notebook.dataDate == null ? null : notebook.dataDate.toString());
        dialogView.findViewById(R.id.spreadsheetItem).setOnClickListener(this::copySpreadsheetId);
    }

    private void copySpreadsheetId(@Nullable View view) {
        ClipboardManager clipboardManager = WeakContext.getContext().getSystemService(ClipboardManager.class);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("spreadsheetId", notebook.spreadsheetId));
        Toasts.spreadsheetIdCopied();
    }

    @Override
    public void setupViews(View dialogView) {
        input = dialogView.findViewById(R.id.newInput);
        deleteNotesCheckbox = dialogView.findViewById(R.id.deleteNotes);
        updateSheetLayout = dialogView.findViewById(R.id.updateSheetLayout);
        updateFromSheetSwitch = dialogView.findViewById(R.id.updateFromSheetSwitch);
    }

    private void setupLayoutState() {
        if (updateFromSheetFlag) {
            input.setVisibility(View.GONE);
            updateSheetLayout.setVisibility(View.VISIBLE);
        } else {
            input.setVisibility(View.VISIBLE);
            updateSheetLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.okButton).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.cancelButton).setOnClickListener(this::onClickCancel);
        updateFromSheetSwitch.setOnCheckedChangeListener((button, state) -> {
            updateFromSheetFlag = state;
            setupLayoutState();
        });
    }

    @Override
    public void setupViewAdjustments(View dialogView) {
    }

    private void onClickOk(View view) {
        String name = input.getText().toString().trim();
        if (updateFromSheetFlag) {
            dialog.cancel();
            updateFromSpreadsheet();
        } else if (name.isEmpty()) {
            Toasts.notebookNameEmpty();
        } else {
            notebook.setName(name);
            listener.updateNotebook(notebook);
            dialog.cancel();
        }
    }

    private void updateFromSpreadsheet() {
        GoogleSignInAccount account = GlobalData.getAccountOrToast();
        if (account == null) {
            return;
        }
        boolean deleteNotes = deleteNotesCheckbox.isChecked();
        Callable<?> mergeTask = () -> {
            NotesRepository repository = StaticUtils.getModel().getNotesRepository();
            SSNotebookData data = SheetLoader.getNotebookData(account, notebook.spreadsheetId,
                    notebook.spreadsheetName, notebook.sheetName, notebook.sheetId, false,
                    true, // We need to bind current spreadsheet because the sheet id or sheet name could be changed.
                    notebook.getName()).call();
            NoteMerger merger = new NoteMerger(notes, data.notes, notebook.getId());
            MergeCollection<Note> mergeCollection = merger.merge();
            if (deleteNotes) {
                repository.deleteSeveral(mergeCollection.uniqueLefts).get(10, TimeUnit.SECONDS);
            }
            repository.updateSeveral(mergeCollection.equal).get(10, TimeUnit.SECONDS);
            int count = (int) repository.countFromNotebook(notebook.getId(), 10);
            int toAddMaxCount = Math.max(0, Spec.MAX_NOTES - count);
            int sum = count + mergeCollection.uniqueRights.size();
            if (sum > Spec.MAX_NOTES) {
                sum = Spec.MAX_NOTES;
            }
            notebook.notesCount = sum;
            notebook.setName(data.notebookData.notebookName);
            notebook.canCopy = data.notebookData.canCopy;
            notebook.sheetId = data.notebookData.sheetId;
            notebook.author = data.notebookData.author;
            notebook.sheetName = data.notebookData.sheetName;
            notebook.needUpdate = false;
            notebook.updateCheck = DateUtils.getCurrentDate();
            if (DateUtils.firstDateNewerTheSecond(data.notebookData.dataDate, notebook.dataDate)) {
                notebook.dataDate = data.notebookData.dataDate;
            } else if (notebook.dataDate == null) {
                notebook.dataDate = DateUtils.getCurrentDate();
            }

            StaticUtils.getModel().getNotebooksRepository().update(notebook);
            if (toAddMaxCount > 0) {
                repository.insertSeveral(mergeCollection.uniqueRights.stream().limit(toAddMaxCount).collect(Collectors.toList()));
            }
            return null;
        };
        BackgroundTask<?> task = new BackgroundTask<>(30, mergeTask);
        task.setRunMainThreadOnSuccess(object -> Toasts.success());
        task.setRunMainThreadOnFail(exception -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(exception)));
        task.buildWaitDialog().showOver();
    }

    public void updateNotebookListener(UpdateNotebookListener listener) {
        this.listener = listener;
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }
}

