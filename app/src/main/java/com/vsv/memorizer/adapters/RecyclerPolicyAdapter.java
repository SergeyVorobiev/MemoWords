package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.memorizer.R;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;

public class RecyclerPolicyAdapter extends RecyclerView.Adapter<RecyclerPolicyAdapter.PolicyHolder> {

    public static class Item {

        public int textId;

        public boolean bold;

        public int sizeId;

        public Item(int textId, int sizeId, boolean bold) {
            this.textId = textId;
            this.sizeId = sizeId;
            this.bold = bold;
        }
    }

    public static class PolicyHolder extends RecyclerView.ViewHolder {

        public final TextView text;

        public PolicyHolder(View view) {
            super(view);
            text = view.findViewById(R.id.policyText);
        }
    }

    private ArrayList<Item> items;

    @Nullable
    public ArrayList<Item> getItems() {
        return items;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerPolicyAdapter.PolicyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerPolicyAdapter.PolicyHolder(StaticUtils.inflate(R.layout.item_policy, parent));
    }

    @Override
    public void onBindViewHolder(RecyclerPolicyAdapter.PolicyHolder viewHolder, final int position) {
        Item item = items.get(position);
        viewHolder.itemView.setTag(position);
        float size = StaticUtils.getDimension(item.sizeId);
        viewHolder.text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        viewHolder.text.setTypeface(Typeface.SERIF, item.bold ? Typeface.BOLD : Typeface.NORMAL);
        if (item.textId == -1) {
            viewHolder.text.setText("");
        } else {
            Spanned textSpan = HtmlCompat.fromHtml(StaticUtils.getString(item.textId), HtmlCompat.FROM_HTML_MODE_COMPACT);
            viewHolder.text.setText(textSpan);
            viewHolder.text.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}

