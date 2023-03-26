package com.vsv.removeitems;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.Sample;
import com.vsv.dialogs.DeleteDictDialog;
import com.vsv.dialogs.SendDictionaryDialog;
import com.vsv.dialogs.SingleWindow;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.io.Storage;
import com.vsv.io.StorageCSV;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerDictionariesAdapter;
import com.vsv.memorizer.fragments.AddMoveCopyFragment;
import com.vsv.models.MainModel;
import com.vsv.spreadsheet.SheetUpdater;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.StaticUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DictionaryRemover extends AbstractRemover {

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final RecyclerDictionariesAdapter adapter = (RecyclerDictionariesAdapter) viewHolder.getBindingAdapter();
            final int position = viewHolder.getBindingAdapterPosition();
            assert adapter != null;
            Dictionary dictionary = adapter.getItem(position);
            if (SingleWindow.isShownToast()) {
                adapter.notifyItemChanged(position);
                return;
            }
            if (direction == ItemTouchHelper.LEFT) {
                RecyclerDictionariesAdapter.ViewHolder holder = (RecyclerDictionariesAdapter.ViewHolder) viewHolder;
                String name = holder.getDictName().getText().toString();
                DeleteDictDialog dialog = new DeleteDictDialog(viewHolder.itemView.getContext(), name);
                dialog.setOnDeleteListener(() -> model.getDictionariesRepository().delete(adapter.getItem(position)));
                dialog.setOnCancelListener(() -> adapter.notifyItemChanged(position));
                dialog.show();
            } else {
                SendDictionaryDialog dialog = new SendDictionaryDialog(viewHolder.itemView.getContext(), dictionary);
                dialog.setOnDismissListener((f) -> adapter.notifyItemChanged(position));
                dialog.setOkListener((mode, spreadsheetName, spreadsheetId, sheetName, sheetId, saveProgress) -> sendDictionary(mode,
                        dictionary, spreadsheetName, spreadsheetId, sheetName, sheetId, saveProgress));
                dialog.show();
            }
        }

        private void sendDictionary(int mode, @NonNull Dictionary dictionary, String spreadsheetName, String spreadsheetId,
                                    String sheetName, long sheetId, boolean saveProgress) {
            if (mode == SendDictionaryDialog.SEND_TO_SHEET) {
                sendToSheet(dictionary, spreadsheetId, sheetName, saveProgress);
            } else if (mode == SendDictionaryDialog.COPY) {
                copy(dictionary);
            } else if (mode == SendDictionaryDialog.MOVE) {
                move(dictionary);
            } else if (mode == SendDictionaryDialog.SHARE) {
                share(dictionary);
            } else if (mode == SendDictionaryDialog.SHARE_SHEET) {
                shareSpreadsheet(dictionary);
            } else if (mode == SendDictionaryDialog.MERGE) {
                merge(dictionary);
            } else if (mode == SendDictionaryDialog.BIND_SPREADSHEET) {
                bind(dictionary, spreadsheetName, spreadsheetId, sheetName, sheetId);
            }
        }

        private void bind(@NonNull Dictionary dictionary, @Nullable String spreadsheetName, @Nullable String spreadsheetId,
                          @Nullable String sheetName, long sheetId) {
            if (dictionary.hasOwner(spreadsheetId, sheetName)) { // Same data
                return;
            }
            if (spreadsheetId != null && !spreadsheetId.isEmpty() && sheetName != null && !sheetName.isEmpty()) {
                if (spreadsheetName == null || spreadsheetName.isEmpty()) {
                    spreadsheetName = StaticUtils.getString(R.string.default_spreadsheet_name);
                }
                dictionary.spreadsheetName = spreadsheetName;
                dictionary.sheetId = sheetId;
                dictionary.spreadsheetId = spreadsheetId;
                dictionary.sheetName = sheetName;
            } else {
                dictionary.spreadsheetName = null;
                dictionary.sheetId = -1;
                dictionary.spreadsheetId = null;
                dictionary.sheetName = null;
            }
            dictionary.author = null;
            dictionary.needUpdate = false;
            dictionary.dataDate = null;
            dictionary.successfulUpdateCheck = null;
            StaticUtils.getModel().getDictionariesRepository().update(dictionary);
        }

        private void merge(Dictionary dictionary) {
            Bundle bundle = shelfBundle.toNewBundle();
            DictionaryBundle.toBundle(bundle, dictionary);
            StaticUtils.navigateSafe(R.id.action_Dictionaries_to_DictionaryMerge, bundle);
        }

        private void copy(Dictionary dictionary) {
            Bundle bundle = shelfBundle.toNewBundle();
            DictionaryBundle.toBundle(bundle, dictionary);
            bundle.putInt(BundleNames.MOVE_TYPE, AddMoveCopyFragment.COPY);
            StaticUtils.navigateSafe(R.id.action_Dictionaries_to_MoveOrCopy, bundle);
        }

        private void sendToSheet(Dictionary dictionary, String spreadsheetId,
                                 String sheetName, boolean saveProgress) {
            if (spreadsheetId == null || spreadsheetId.isEmpty()) {
                Toasts.sheetNotSpecified();
                return;
            }
            if (sheetName == null || sheetName.isEmpty()) {
                Toasts.sheetTabNotSpecified();
                return;
            }
            SheetUpdater.writeToSheet(GlobalData.account, spreadsheetId, sheetName,
                    dictionary, saveProgress);
        }

        private void move(Dictionary dictionary) {
            Bundle bundle = shelfBundle.toNewBundle();
            DictionaryBundle.toBundle(bundle, dictionary);
            bundle.putInt(BundleNames.MOVE_TYPE, AddMoveCopyFragment.MOVE);
            StaticUtils.navigateSafe(R.id.action_Dictionaries_to_MoveOrCopy, bundle);
        }

        private void share(Dictionary dictionary) {
            DictionaryWithSamples dictWithSamples = new DictionaryWithSamples();
            dictWithSamples.dictionary = dictionary;
            BackgroundTask<List<Sample>> backgroundTask = new BackgroundTask<>(12, () -> WeakContext.getMainActivity().getMainModel().getSamplesRepository().getSamples(dictionary.getId()).get(8, TimeUnit.SECONDS));
            backgroundTask.setRunMainThreadOnFail((e) -> {
                Log.e("Share dictionary", e.toString());
                Toasts.cannotShareDictionary();
            });
            backgroundTask.setRunMainThreadOnSuccess((samples) -> {
                if (samples == null) {
                    Toasts.cannotShareDictionary();
                    return;
                }
                dictWithSamples.samples = samples;
                File sendFile = StorageCSV.createTemporaryFile(R.string.toast_cannot_share_dictionary, StaticUtils.getString(R.string.share_file_name_dict));
                boolean result = Storage.getDocumentsStorage().loadDictionaryWithSamplesIntoFile(dictWithSamples, sendFile);
                if (!result) {
                    Toasts.cannotShareDictionary();
                    return;
                }
                StorageCSV.sendFile(sendFile);
                //context.getContentResolver().delete(deleteFileUri, null, null);
            });
            backgroundTask.buildWaitDialog(WeakContext.getContext()).showOver();
        }

        private void shareSpreadsheet(@NonNull Dictionary dictionary) {
            File sendFile = StorageCSV.createTemporaryFile(R.string.toast_cannot_share_dictionary, StaticUtils.getString(R.string.share_file_name_dict));
            boolean result = Storage.getDocumentsStorage().loadDictionaryIntoFile(dictionary, sendFile);
            if (!result) {
                Toasts.cannotShareDictionary();
                return;
            }
            StorageCSV.sendFile(sendFile);
        }
    };

    private MainModel model;

    private ShelfBundle shelfBundle;

    public DictionaryRemover() {
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
    }

    public void setData(@NonNull ShelfBundle shelfBundle) {
        this.model = StaticUtils.getModel();
        this.shelfBundle = shelfBundle;
    }

    public void clearData() {
        this.model = null;
        this.shelfBundle = null;
        this.itemTouchHelper.attachToRecyclerView(null);
    }
}
