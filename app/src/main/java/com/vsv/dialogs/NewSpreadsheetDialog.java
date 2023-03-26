package com.vsv.dialogs;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.dialogs.listeners.SheetCreateListener;
import com.vsv.memorizer.R;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;
import com.vsv.utils.GoogleTasksExceptionHandler;

import java.util.ArrayList;

public class
NewSpreadsheetDialog extends SingleCustomDialog implements AdapterView.OnItemSelectedListener {

    private TextView inputName;

    private EditText inputId;

    private SheetCreateListener listener;

    private final @Nullable
    ArrayList<SpreadSheetInfo> items;

    private int type = 0;

    private boolean getNameFromServer;

    public NewSpreadsheetDialog(@Nullable ArrayList<SpreadSheetInfo> items) {
        super(R.layout.dialog_new_spreadsheet, false, true);
        ((TextView) dialogView.findViewById(R.id.newSpreadsheetDialogHeader)).setText(R.string.spreadsheet_dialog_new_header);
        this.items = items;
        Spinner type = dialogView.findViewById(R.id.type);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, GlobalData.TYPES);
        type.setAdapter(typeAdapter);
        type.setSelection(0);
        type.setOnItemSelectedListener(this);
    }

    @Override
    public void setupViews(View dialogView) {
        inputName = dialogView.findViewById(R.id.spreadsheetNameInput);
        inputId = dialogView.findViewById(R.id.spreadsheetIdInput);
        inputId.setText(GlobalData.inputSpreadsheetId);
        inputId.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                GlobalData.inputSpreadsheetId = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        SwitchCompat getNameFromServerSwitch = dialogView.findViewById(R.id.nameFromServer);
        getNameFromServerSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            getNameFromServer = isChecked;
            if (isChecked) {
                inputName.setVisibility(View.GONE);
            } else {
                inputName.setVisibility(View.VISIBLE);
                inputId.setText(GlobalData.inputSpreadsheetId);
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
        inputName.requestFocus();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void onClickOk(View view) {
        String name = inputName.getText().toString().trim();
        String id = inputId.getText().toString().trim();
        if (id.isEmpty()) {
            Toasts.spreadsheetIdEmpty();
            return;
        }
        if (checkSpreadsheetExists(id)) {
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
                createSheet(id, title);
                GlobalData.inputSpreadsheetId = "";
            });
            task.buildWaitDialog().showOver();
        } else {
            createSheet(id, name);
        }
    }

    private boolean checkSpreadsheetExists(String id) {
        return items != null && items.stream().anyMatch((item) -> id.equals(item.spreadSheetId));
    }

    private void createSheet(String id, String name) {
        listener.createSheet(name, id, type);
        dialog.cancel();
    }

    public void setCreateSheetListener(SheetCreateListener listener) {
        this.listener = listener;
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        type = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        type = 0;
    }
}
