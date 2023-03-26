package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.Dictionary;
import com.vsv.memorizer.R;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;

public class RecyclerMoveToDictionaryAdapter extends RecyclerView.Adapter<RecyclerMoveToDictionaryAdapter.CardHolder> {

    private ArrayList<Dictionary> items;

    private Dictionary checkedDictionary;

    public static class CardHolder extends RecyclerView.ViewHolder {

        private final Drawable backgroundDictionaryNonChecked;

        private final Drawable backgroundDictionaryChecked;

        private final TextView name;

        private final TextView language;

        private final TextView count;

        private final ImageView checker;

        private final View backgroundView;

        public CardHolder(View view, View.OnClickListener onDickLickListener) {
            super(view);
            name = view.findViewById(R.id.dictName);
            language = view.findViewById(R.id.dictLanguage);
            count = view.findViewById(R.id.count);
            checker = view.findViewById(R.id.checker);
            backgroundView = view.findViewById(R.id.mainDictLayout);
            backgroundView.setOnClickListener(onDickLickListener);
            backgroundDictionaryChecked = StaticUtils.getDrawable(R.drawable.bg_item_dictionary2);
            backgroundDictionaryNonChecked = StaticUtils.getDrawable(R.drawable.bg_item_dictionary);
        }

        public void setBackground(@NonNull Dictionary dictionary) {
            if (dictionary.isChecked) {
                backgroundView.setBackground(backgroundDictionaryChecked);
                checker.setVisibility(View.VISIBLE);
            } else {
                backgroundView.setBackground(backgroundDictionaryNonChecked);
                checker.setVisibility(View.GONE);
            }
        }
    }

    public RecyclerMoveToDictionaryAdapter() {

    }

    public @Nullable Dictionary getCheckedDictionary() {
        return checkedDictionary;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onDictClickListener(@NonNull View view) {
        if (checkedDictionary != null) {
            checkedDictionary.isChecked = false;
        }
        int position = (int) view.getTag();
        Dictionary dictionary = items.get(position);
        dictionary.isChecked = true;
        checkedDictionary = dictionary;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(@NonNull ArrayList<Dictionary> items) {
        this.items = items;
        for (Dictionary dictionary : items) {
            dictionary.isChecked = false;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerMoveToDictionaryAdapter.CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerMoveToDictionaryAdapter.CardHolder(StaticUtils.inflate(R.layout.item_move_to_dictionary, parent), this::onDictClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerMoveToDictionaryAdapter.CardHolder viewHolder, final int position) {
        Dictionary dictionary = items.get(position);
        viewHolder.name.setText(dictionary.getName());
        viewHolder.backgroundView.setTag(position);
        String leftLanguage = dictionary.getLeftLocaleAbb();
        String rightLanguage = dictionary.getRightLocaleAbb();
        String language = leftLanguage + " - " + rightLanguage;
        viewHolder.language.setText(language);
        viewHolder.count.setText(StaticUtils.getString(R.string.counter, dictionary.getCount(), Spec.MAX_SAMPLES));
        viewHolder.setBackground(dictionary);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}
