package com.vsv.dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Sample;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.dialogs.entities.LanguageChooser;
import com.vsv.dialogs.listeners.UpdateDictionaryListener;
import com.vsv.memorizer.R;
import com.vsv.repositories.SamplesRepository;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.spreadsheet.entities.SSDictData;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.DateUtils;
import com.vsv.utils.GoogleTasksExceptionHandler;
import com.vsv.utils.Spec;
import com.vsv.utils.merger.MergeCollection;
import com.vsv.utils.merger.SampleSheetMerger;
import com.vsv.utils.SheetDataBuilder;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UpdateDictionaryDialog extends SingleCustomDialog {

    private final TextView input;

    private UpdateDictionaryListener listener;

    private final Dictionary dictionary;

    private final LanguageChooser languageChooser;

    private SwitchCompat updateFromSheetSwitch;

    private View updateNameLayout;

    private ViewGroup updateSheetLayout;

    private boolean updateFromSheetFlag;

    private TextView sheetNameTextView;

    private TextView spreadsheetIdTextView;

    private TextView spreadsheetNameTextView;

    private CheckBox deleteSamplesCheckbox;

    private CheckBox replaceExamplesCheckbox;

    private final ArrayList<Sample> samples;

    public UpdateDictionaryDialog(@NonNull Dictionary dictionary, @NonNull ArrayList<Sample> samples) {
        super(R.layout.dialog_update_dictionary, false, true);
        this.samples = samples;
        this.dictionary = dictionary;
        View ok = dialogView.findViewById(R.id.updateDictionaryOk);
        View cancel = dialogView.findViewById(R.id.updateDictionaryCancel);
        View spreadsheetIdView = dialogView.findViewById(R.id.spreadsheetItem);
        TextView updatedDate = dialogView.findViewById(R.id.updatedDate);
        updatedDate.setText(dictionary.dataDate == null ? null : dictionary.dataDate.toString());
        spreadsheetIdView.setOnClickListener(this::copySpreadsheetId);
        input = dialogView.findViewById(R.id.updateNameDictionaryInput);
        languageChooser = new LanguageChooser(context, dialogView, dictionary.getLeftLocaleAbb(), dictionary.getRightLocaleAbb());
        input.setText(dictionary.getName());
        ok.setOnClickListener(this::onClickOk);
        cancel.setOnClickListener(this::onClickCancel);
        updateFromSheetFlag = dictionary.hasOwner();
        setupLayoutState();
        updateFromSheetSwitch.setEnabled(updateFromSheetFlag);
        updateFromSheetSwitch.setChecked(updateFromSheetFlag);
        loadItemsIntoLayout(dictionary);
    }

    private void copySpreadsheetId(@Nullable View view) {
        ClipboardManager clipboardManager = WeakContext.getContext().getSystemService(ClipboardManager.class);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("spreadsheetId", dictionary.spreadsheetId));
        Toasts.spreadsheetIdCopied();
    }

    private void loadItemsIntoLayout(Dictionary dictionary) {
        sheetNameTextView.setText(dictionary.sheetName);
        spreadsheetIdTextView.setText(dictionary.spreadsheetId);
        spreadsheetNameTextView.setText(dictionary.spreadsheetName);
    }

    private void onClickOk(View view) {
        String name = input.getText().toString().trim();
        if (updateFromSheetFlag) {
            dialog.cancel();
            updateFromSpreadsheet();
        } else {
            if (name.isEmpty()) {
                Toasts.dictionaryNameEmpty();
            } else {
                dictionary.setName(name);
                dictionary.setLeftLocaleAbb(languageChooser.getLeftLanguageAbb());
                dictionary.setRightLocaleAbb(languageChooser.getRightLanguageAbb());
                listener.updateDictionary(dictionary);
                dialog.cancel();
            }
        }
    }

    private void updateFromSpreadsheet() {
        GoogleSignInAccount account = GlobalData.getAccountOrToast();
        if (account == null) {
            return;
        }
        boolean deleteSamples = deleteSamplesCheckbox.isChecked();
        Callable<?> mergeTask = () -> {
            SSDictData data = SheetLoader.getDictionaryData(account, dictionary.spreadsheetId, dictionary.sheetName, dictionary.sheetId, dictionary.getName(), false, false).call();
            SheetDataBuilder.DictData dict = data.dictionaryData;
            assert dict != null;
            SamplesRepository samplesRepository = StaticUtils.getModel().getSamplesRepository();
            dictionary.setName(dict.dictName);
            dictionary.setLeftLocaleAbb(dict.leftLanguage);
            dictionary.setRightLocaleAbb(dict.rightLanguage);
            dictionary.canCopy = dict.canCopy;
            dictionary.sheetName = dict.sheetName;
            dictionary.sheetId = dict.sheetId;
            dictionary.needUpdate = false;
            dictionary.successfulUpdateCheck = DateUtils.getCurrentDate();
            dictionary.author = dict.author;
            if (DateUtils.firstDateNewerTheSecond(data.dictionaryData.dataDate, dictionary.dataDate)) {
                dictionary.dataDate = data.dictionaryData.dataDate;
            } else if (dictionary.dataDate == null) {
                dictionary.dataDate = DateUtils.getCurrentDate();
            }
            SampleSheetMerger merger = new SampleSheetMerger(samples, data.samples, dictionary.getId(), replaceExamplesCheckbox.isChecked());
            MergeCollection<Sample> mergeCollection = merger.merge();
            if (deleteSamples) {
                samplesRepository.deleteSeveralWaiting(mergeCollection.uniqueLefts, 10);
            }
            samplesRepository.updateSeveralWaiting(mergeCollection.equal, 10);
            int count = samplesRepository.countInDictionary(dictionary.getId()).get(10, TimeUnit.SECONDS).intValue();
            int toAddMaxCount = Math.max(0, Spec.MAX_SAMPLES - count);
            if (toAddMaxCount > 0) {
                samplesRepository.insertSeveralWaiting(mergeCollection.uniqueRights.stream().limit(toAddMaxCount).collect(Collectors.toList()), 10);
            }
            ArrayList<Sample> samples = StaticUtils.getModel().getSamplesRepository().getSamplesWithTimeout(dictionary.getId(), 10);
            dictionary.setPassedPercentage(Dictionary.calculatePercentage(samples));
            dictionary.setRememberedCount(samples == null ? 0 : (int) samples.stream().filter(Sample::isRemembered).count());
            dictionary.setCount(samples == null ? 0 : samples.size());
            StaticUtils.getModel().getDictionariesRepository().update(dictionary);
            return null;
        };
        BackgroundTask<?> task = new BackgroundTask<>(30, mergeTask);
        task.setRunMainThreadOnSuccess(object -> Toasts.success());
        task.setRunMainThreadOnFail(exception -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(exception)));
        task.buildWaitDialog().showOver();
    }

    public void updateDictionaryListener(UpdateDictionaryListener listener) {
        this.listener = listener;
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {
        updateFromSheetSwitch = dialogView.findViewById(R.id.updateFromSheetSwitch);
        updateNameLayout = dialogView.findViewById(R.id.updateNameLayout);
        updateSheetLayout = dialogView.findViewById(R.id.updateSheetLayout);
        sheetNameTextView = dialogView.findViewById(R.id.sheetName);
        spreadsheetIdTextView = dialogView.findViewById(R.id.spreadsheetId);
        spreadsheetNameTextView = dialogView.findViewById(R.id.spreadsheetName);
        deleteSamplesCheckbox = dialogView.findViewById(R.id.deleteSamples);
        replaceExamplesCheckbox = dialogView.findViewById(R.id.updateExamples);
        replaceExamplesCheckbox.setChecked(true);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        updateFromSheetSwitch.setOnCheckedChangeListener((button, state) -> {
            updateFromSheetFlag = state;
            setupLayoutState();
        });
    }

    private void setupLayoutState() {
        if (updateFromSheetFlag) {
            updateNameLayout.setVisibility(View.GONE);
            updateSheetLayout.setVisibility(View.VISIBLE);
        } else {
            updateNameLayout.setVisibility(View.VISIBLE);
            updateSheetLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }
}

