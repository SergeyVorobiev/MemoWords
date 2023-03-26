package com.vsv.removeitems;

import android.app.AlertDialog;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Sample;
import com.vsv.dialogs.SendSampleDialog;
import com.vsv.dialogs.SingleWindow;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerSamplesAdapter;
import com.vsv.models.MainModel;
import com.vsv.toasts.Toasts;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SampleRemover extends AbstractRemover {

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getBindingAdapterPosition();
            final RecyclerSamplesAdapter adapter = (RecyclerSamplesAdapter) viewHolder.getBindingAdapter();
            assert adapter != null;
            if (SingleWindow.isShownToast()) {
                adapter.notifyItemChanged(position);
                return;
            }
            Sample sample = adapter.getSample(position);
            if (direction == ItemTouchHelper.LEFT) {
                AlertDialog.Builder builder = new AlertDialog.Builder(viewHolder.itemView.getContext());
                builder.setMessage(StaticUtils.getString(R.string.delete_sample_header));
                builder.setPositiveButton(StaticUtils.getString(R.string.general_dialog_button_delete), (dialog, which) -> StaticUtils.getModel().getSamplesRepository()
                                .delete(sample))
                        .setNegativeButton(StaticUtils.getString(R.string.general_dialog_button_cancel), (dialog, which) -> adapter.notifyItemChanged(position))
                        .show();
            } else {
                Pair<ArrayList<Dictionary>, Dictionary> pair = getDictionaries(sample.getDictionaryId());
                ArrayList<Dictionary> dictionaries = pair.first;
                if (dictionaries.isEmpty()) {
                    adapter.notifyItemChanged(position);
                    Toasts.noDictionariesToMove();
                    return;
                }
                SendSampleDialog dialog = new SendSampleDialog(dictionaries, pair.second, sample);
                dialog.setOnDismissListener((f) -> adapter.notifyItemChanged(position));
                dialog.show();
            }
        }
    };

    private @NonNull Pair<ArrayList<Dictionary>, Dictionary> getDictionaries(long sampleDictionaryId) {
        ArrayList<Dictionary> dictionaries = new ArrayList<>();
        MainModel model = StaticUtils.getModelOrNull();
        if (model == null) {
            return new Pair<> (dictionaries, null);
        }
        Dictionary[] fromDictionary = new Dictionary[1];
        try {
            dictionaries = model.getDictionariesRepository().getAllDictionaries(shelfBundle.id).get(5, TimeUnit.SECONDS);
            dictionaries.removeIf((dictionary -> {
                if (dictionary.getId() == sampleDictionaryId) {
                    fromDictionary[0] = dictionary;
                    return true;
                } else return dictionary.getCount() == Spec.MAX_SAMPLES;
            }));
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return new Pair<> (dictionaries, null);
        }
        return new Pair<> (dictionaries, fromDictionary[0]);
    }

    private ShelfBundle shelfBundle;

    public SampleRemover() {
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
    }

    public void setData(@NonNull ShelfBundle shelfBundle) {
        this.shelfBundle = shelfBundle;
    }

    @Override
    public void clearData() {
        this.shelfBundle = null;
        this.itemTouchHelper.attachToRecyclerView(null);
    }
}
