package com.vsv.dialogs;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.dialogs.listeners.SheetUpdateListener;
import com.vsv.memorizer.R;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;
import com.vsv.utils.GoogleTasksExceptionHandler;

import java.util.ArrayList;

public class UpdateSpreadsheetDialog extends SingleCustomDialog implements AdapterView.OnItemSelectedListener {

    private TextView inputName;

    private TextView inputId;

    private SheetUpdateListener listener;

    private final SpreadSheetInfo item;

    private final ArrayList<SpreadSheetInfo> all;

    private boolean getNameFromServer;

    public UpdateSpreadsheetDialog(SpreadSheetInfo item, ArrayList<SpreadSheetInfo> all) {
        super(R.layout.dialog_new_spreadsheet, true, true);
        this.item = item;
        ((TextView) dialogView.findViewById(R.id.newSpreadsheetDialogHeader)).setText(R.string.spreadsheet_dialog_update_header);
        inputName.setText(item.name);
        inputId.setText(item.spreadSheetId);
        this.all = all;

        Spinner type = dialogView.findViewById(R.id.type);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, GlobalData.TYPES);
        type.setAdapter(typeAdapter);
        type.setSelection(item.type);
        type.setOnItemSelectedListener(this);
    }

    @Override
    public void setupViews(View dialogView) {
        inputName = dialogView.findViewById(R.id.spreadsheetNameInput);
        inputId = dialogView.findViewById(R.id.spreadsheetIdInput);
        SwitchCompat getNameFromServerSwitch = dialogView.findViewById(R.id.nameFromServer);
        getNameFromServerSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            getNameFromServer = isChecked;
            if (isChecked) {
                inputName.setVisibility(View.GONE);
            } else {
                inputName.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.newSpreadsheetOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.newSpreadsheetCancel).setOnClickListener(this::onClickCancel);
    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }

    private void onClickOk(View view) {
        String name = inputName.getText().toString().trim();
        String id = inputId.getText().toString().trim();
        if (id.isEmpty()) {
            Toasts.spreadsheetIdEmpty();
            return;
        }
        boolean exist = all.stream().anyMatch((info) -> id.equals(info.spreadSheetId) && info != item);
        if (exist) {
            Toasts.spreadsheetExists();
            return;
        }
        if (name.isEmpty() && !getNameFromServer) {
            Toasts.spreadsheetNameEmpty();
            return;
        }
        if (getNameFromServer) {
            GoogleSignInAccount account = GlobalData.getAccountOrToast();
            if (account == null) {
                return;
            }
            BackgroundTask<String> task = SheetLoader.buildSpreadsheetTitleTask(account, id);
            task.setRunMainThreadOnFail((e) -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(e)));
            task.setRunMainThreadOnSuccess((title) -> {
                item.spreadSheetId = id;
                item.name = title;
                listener.updateSheet(item);
                dialog.cancel();
            });
            task.buildWaitDialog().showOver();
        } else {
            item.spreadSheetId = id;
            item.name = name;
            listener.updateSheet(item);
            dialog.cancel();
        }
    }

    public void setUpdateSheetListener(SheetUpdateListener listener) {
        this.listener = listener;
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        item.type = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        item.type = 0;
    }
}
