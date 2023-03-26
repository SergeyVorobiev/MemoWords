package com.vsv.dialogs;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Sample;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerMoveToDictionaryAdapter;
import com.vsv.toasts.Toasts;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;
import com.vsv.viewutils.StopVerticalScrollAnimator;

import java.util.ArrayList;
import java.util.Comparator;

public class SendSampleDialog extends SingleCustomDialog {

    private final RecyclerMoveToDictionaryAdapter adapter;

    private final Dictionary fromDictionary;

    private final Sample sample;

    public SendSampleDialog(@NonNull ArrayList<Dictionary> dictionaries, @NonNull Dictionary fromDictionary, @NonNull Sample sample) {
        super(R.layout.dialog_send_sample, false, true);
        this.fromDictionary = fromDictionary;
        this.sample = sample;
        View ok = dialogView.findViewById(R.id.newSampleOk);
        View cancel = dialogView.findViewById(R.id.newSampleCancel);
        ok.setOnClickListener(this::onClickOk);
        cancel.setOnClickListener(this::onClickCancel);
        RecyclerView listDictionaries = dialogView.findViewById(R.id.recycleDictList);
        StopVerticalScrollAnimator.setRecycleViewAnimation(listDictionaries, Spec.MIN_SCROLL_HIT, R.anim.from_bottom2, R.anim.from_top);
        adapter = new RecyclerMoveToDictionaryAdapter();
        listDictionaries.setAdapter(adapter);
        dictionaries.sort(Comparator.comparing(Dictionary::getName));
        adapter.setItems(dictionaries);
    }

    private void onClickOk(View view) {
        Dictionary dictionary = adapter.getCheckedDictionary();
        if (dictionary != null) {
            StaticUtils.getModel().getDictionariesRepository().moveSampleFromTo(fromDictionary, dictionary, sample);
            Toasts.success();
        }
        dialog.cancel();
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {

    }

    @Override
    public void setupViewListeners(View dialogView) {

    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }
}
