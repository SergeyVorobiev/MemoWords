package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.TreeMap;

public class RecyclerHelpAdapter extends RecyclerView.Adapter<RecyclerHelpAdapter.EntryHolder> implements GifDrawableReadyNotifier {

    private final TreeMap<Integer, GifDrawable> dynamicDrawables = new TreeMap<>();

    private static final int textColor = 0xFF111111;

    public static class HelpEntry {

        final String topText;

        final String bottomText;

        final String topImageHeader;

        final String bottomImageHeader;

        public Drawable image;

        public HelpEntry(String topText, String bottomText, String topImageHeader, String bottomImageHeader, Drawable drawable) {
            this.topText = topText;
            this.bottomText = bottomText;
            this.topImageHeader = topImageHeader;
            this.bottomImageHeader = bottomImageHeader;
            this.image = drawable;
        }

        public static HelpEntry createEmptyString() {
            return new HelpEntry("", null, null, null, null);
        }

        public static HelpEntry createAnimatedDrawable(int gifId, int itemIndex, GifDrawableReadyNotifier notifier) {
            Glide.with(WeakContext.getContext())
                    .load(gifId).asGif()
                    .into(new SimpleTarget<GifDrawable>() {
                        @Override
                        public void onResourceReady(GifDrawable resource, GlideAnimation<? super GifDrawable> glideAnimation) {
                            notifier.resourceReady(resource, itemIndex);
                        }
                    });
            return new HelpEntry("", null, null, null, null);
        }

        public static HelpEntry createTopText(int stringId) {
            return new HelpEntry(StaticUtils.getString(stringId), null, null, null, null);
        }

        public static HelpEntry createTopTextWithEmptyStringAtTheBottom(int stringId) {
            return new HelpEntry(StaticUtils.getString(stringId) + "\n", null, null, null, null);
        }

        public static HelpEntry createTopTextWithImage(int stringId, int drawableId) {
            return new HelpEntry(StaticUtils.getString(stringId), null, null, null, StaticUtils.getDrawable(drawableId));
        }

        public static HelpEntry createImage(int drawableId) {
            return new HelpEntry(null, null, null, null, StaticUtils.getDrawable(drawableId));
        }

        public static HelpEntry createTopText(String text) {
            return new HelpEntry(text, null, null, null, null);
        }
    }

    private ArrayList<HelpEntry> items;

    public static class EntryHolder extends RecyclerView.ViewHolder {

        public final TextView topTextView;

        private final TextView bottomTextView;

        private final TextView topImageHeader;

        private final TextView bottomImageHeader;

        private final ImageView image;

        public EntryHolder(View view) {
            super(view);
            topTextView = view.findViewById(R.id.topText);
            topImageHeader = view.findViewById(R.id.topImageHeader);
            image = view.findViewById(R.id.image);
            bottomImageHeader = view.findViewById(R.id.bottomImageHeader);
            bottomTextView = view.findViewById(R.id.bottomText);
        }

        public void setData(HelpEntry entry) {
            setDataOrInvisible(topTextView, entry.topText);
            setDataOrInvisible(bottomTextView, entry.bottomText);
            setDataOrInvisible(topImageHeader, entry.topImageHeader);
            setDataOrInvisible(bottomImageHeader, entry.bottomImageHeader);
            setDataOrInvisible(image, entry.image);
        }

        private void setDataOrInvisible(View view, Object data) {
            if (data == null) {
                view.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
                if (view instanceof ImageView) {
                    //ViewGroup.LayoutParams params = view.getLayoutParams();
                    //params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    //params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    ImageView imageView = (ImageView) view;
                    imageView.setAdjustViewBounds(true);
                    if (data instanceof GifDrawable) {
                        ((GifDrawable) data).start();
                    }
                    imageView.setImageDrawable((Drawable) data);
                    //imageView.setImageDrawable((Drawable) data);
                } else {
                    //ViewGroup.LayoutParams params = view.getLayoutParams();
                    //params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    //params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    TextView textView = (TextView) view;
                    textView.setTextColor(textColor);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    Spanned textSpan = HtmlCompat.fromHtml((String) data, HtmlCompat.FROM_HTML_MODE_COMPACT);
                    textView.setText(textSpan);
                }
            }
        }
    }

    @Override
    public void resourceReady(GifDrawable drawable, int itemIndex) {
        new Handler(Looper.getMainLooper()).post(() -> {
            this.dynamicDrawables.put(itemIndex, drawable);
            if (items != null && items.size() >= itemIndex) {
                notifyItemChanged(itemIndex);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<HelpEntry> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerHelpAdapter.EntryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerHelpAdapter.EntryHolder(StaticUtils.inflate(R.layout.item_help, parent));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerHelpAdapter.EntryHolder viewHolder, final int position) {
        Drawable drawable = dynamicDrawables.get(position);
        if (drawable != null) {
            items.get(position).image = drawable;
        }
        viewHolder.setData(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}
