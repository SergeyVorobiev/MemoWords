package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.TransitionDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.Note;
import com.vsv.dialogs.SingleCustomDialog;
import com.vsv.dialogs.UpdateNoteDialog;
import com.vsv.memorizer.R;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Timer;
import com.vsv.viewutils.StopVerticalScrollAnimator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RecyclerNotesAdapter extends AbstractAdapter<RecyclerNotesAdapter.CardHolder> {

    private ArrayList<Note> items;

    private ArrayList<Note> filteredItems;

    private final View.OnClickListener clickCardListener;

    private final View.OnLongClickListener longClickCardListener;

    private int animateItem = -1;

    private boolean canEdit;

    private String filterQuery;

    private boolean floatButtonIsPresent;

    private CountDownTimer timer;

    public final TreeMap<Integer, RecyclerNotesAdapter.CardHolder> holders = new TreeMap<>();

    public static class CardHolder extends RecyclerView.ViewHolder {

        public final View card;

        private final View mainView;

        public final TextView header;

        public final TextView content;

        public final TextView count;

        public final Animation animation;

        public final View contentLayout;

        public final View paddingView;

        public final TransitionDrawable iconTransitionDrawable;

        public long transitionTime;

        public boolean isReverse;

        public CardHolder(View view) {
            super(view);
            header = view.findViewById(R.id.header);
            content = view.findViewById(R.id.content);
            mainView = view.findViewById(R.id.note);
            count = view.findViewById(R.id.count);
            card = view;
            iconTransitionDrawable = (TransitionDrawable) header.getCompoundDrawablesRelative()[0];
            // iconTransitionDrawable.setCrossFadeEnabled(true);
            transitionTime = System.nanoTime();
            animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.from_bottom);
            paddingView = view.findViewById(R.id.paddingView);
            contentLayout = view.findViewById(R.id.contentLayout);
        }

        public void animate() {
            card.startAnimation(animation);
        }

        public void clearAnimation() {
            card.clearAnimation();
        }

    }

    public RecyclerNotesAdapter(@NonNull RecyclerView owner) {
        StopVerticalScrollAnimator.setRecycleViewAnimation(owner, Spec.MIN_SCROLL_HIT, R.anim.from_bottom2, R.anim.from_top);
        this.clickCardListener = this::onNoteClick;
        this.longClickCardListener = this::updateClick;
    }

    public void setCanEditItem(boolean canEdit) {
        this.canEdit = canEdit;
    }

    private boolean updateClick(@NonNull View view) {
        if (!canEdit) {
            Toasts.cannotEdit();
            return true;
        }
        UpdateNoteDialog dialog = new UpdateNoteDialog(this.getItem((int) view.getTag()));
        dialog.setNoteUpdateListener(StaticUtils.getModel().getNotesRepository()::update);
        dialog.show();
        return true;
    }

    public void setup(boolean floatButtonIsPresent) {
        this.floatButtonIsPresent = floatButtonIsPresent;
        this.timer = createTimer();
        this.timer.start();
    }

    private CountDownTimer createTimer() {
        return new CountDownTimer(Integer.MAX_VALUE, 1000) {

            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onTick(long millisUntilFinished) {
                int size = RecyclerNotesAdapter.this.holders.values().size();
                if (size == 0) {
                    return;
                }
                int picked = StaticUtils.random.nextInt(size);
                handler.post(() -> {
                    int i = 0;
                    int duration = 3000;
                    // int random = StaticUtils.random.nextInt(2);
                    for (RecyclerNotesAdapter.CardHolder holder: RecyclerNotesAdapter.this.holders.values()) {
                        int elapsedTime = (int) Timer.nanoTimeDiffFromNowInMilliseconds(holder.transitionTime);
                        TransitionDrawable drawable = holder.iconTransitionDrawable;
                        if (i == picked /* && random == 0 */) {
                            if (holder.isReverse && elapsedTime >= duration) {
                                holder.transitionTime = System.nanoTime();
                                drawable.reverseTransition(duration);
                                holder.isReverse = false;
                            } else if (!holder.isReverse && elapsedTime >= duration) {
                                holder.transitionTime = System.nanoTime();
                                drawable.startTransition(duration);
                                holder.isReverse = true;
                            }
                        } else {
                            if (holder.isReverse && elapsedTime >= duration) {
                                holder.transitionTime = System.nanoTime();
                                drawable.reverseTransition(duration);
                                holder.isReverse = false;
                            }
                        }
                        i++;
                    }
                });
            }

            @Override
            public void onFinish() {

            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        this.timer.cancel();
        this.holders.clear();
        this.animateItem = -1;
        filterQuery = null;
        this.items = null;
        this.filteredItems = null;
        this.notifyDataSetChanged();
    }

    private void onNoteClick(View view) {
        if (SingleCustomDialog.isShownToast()) {
            return;
        }
        int position = (int) view.getTag();
        filteredItems.get(position).isOpen = !filteredItems.get(position).isOpen;
        view.findViewById(R.id.contentLayout).setVisibility(filteredItems.get(position).isOpen ? View.VISIBLE : View.GONE);
    }

    @SuppressLint("NotifyDataSetChanged")
    public int applyFilter(String query) {
        GlobalData.noteSearchQuery = query.toLowerCase();
        filter();
        notifyDataSetChanged();
        return getItemCount();
    }

    public Note getItem(int position) {
        return filteredItems.get(position);
    }

    // Not a deep copy, only new array.
    public @Nullable
    ArrayList<Note> getCopyItems() {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return new ArrayList<>(items);
    }

    @SuppressLint("NotifyDataSetChanged")
    public int setItems(ArrayList<Note> items) {
        this.items = items;
        filter();
        notifyDataSetChanged();
        return getItemCount();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update() {
        filter();
        notifyDataSetChanged();
    }

    private void filter() {
        filterQuery = GlobalData.noteSearchQuery;
        if (items == null) {
            filteredItems = null;
            return;
        } else {
            filteredItems = new ArrayList<>(items);
        }
        if (filterQuery != null && !filterQuery.isEmpty()) {
            filterQuery = filterQuery.toLowerCase();
            filteredItems = (ArrayList<Note>) items.stream()
                    .filter((note) -> {
                        String content = note.getContent() == null ? "" : note.getContent();
                        content = content.replace("\n", "<br>");
                        String header = note.getName() == null ? "" : note.getName();
                        header = header.replace("\n", "<br>");
                        Spanned headerSpan = HtmlCompat.fromHtml(header, HtmlCompat.FROM_HTML_MODE_COMPACT);
                        Spanned contentSpan = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT);
                        String headerString = headerSpan.toString().toLowerCase();
                        String contentString = contentSpan.toString().toLowerCase();
                        return headerString.contains(filterQuery) || contentString.contains(filterQuery);
                    }).collect(Collectors.toList());
        }
        sort();
    }

    private void sort() {
        if (filteredItems != null && filteredItems.size() > 0) {
            Comparator<Note> comparator = Comparator.comparing(Note::getNumber);
            filteredItems.sort(comparator);
        }
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        View mainView = v.findViewById(R.id.note);
        mainView.setOnClickListener(clickCardListener);
        mainView.setOnLongClickListener(longClickCardListener);
        return new CardHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardHolder viewHolder, final int position) {
        Note note = filteredItems.get(position);
        holders.put(viewHolder.hashCode(), viewHolder);
        if (position == getItemCount() - 1 && floatButtonIsPresent) {
            viewHolder.paddingView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.paddingView.setVisibility(View.GONE);
        }
        String content = note.getContent() == null ? "" : note.getContent();
        content = content.replace("\n", "<br>");
        String header = note.getName() == null ? "" : note.getName();
        header = header.replace("\n", "<br>");
        Spanned headerSpan = HtmlCompat.fromHtml(header, HtmlCompat.FROM_HTML_MODE_COMPACT);
        Spanned contentSpan = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_COMPACT);
        String contentString = contentSpan.toString();
        if (filterQuery != null && !filterQuery.isEmpty()) {
            contentString = contentString.toLowerCase();
            int index = contentString.indexOf(filterQuery);
            if (index > -1) {
                ArrayList<Integer> array = new ArrayList<>();
                int length = filterQuery.length();
                while (index > -1) {
                    array.add(index);
                    index = contentString.indexOf(filterQuery, index + length);
                }
                for (int ind : array) {
                    ((Spannable) contentSpan).setSpan(new ForegroundColorSpan(0xFFDDC700), ind, ind + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        viewHolder.header.setText(headerSpan);
        viewHolder.content.setText(contentSpan);
        viewHolder.mainView.setTag(position);
        viewHolder.count.setText(String.valueOf(position + 1));
        if (animateItem < position) {
            animateItem++;
            viewHolder.animate();
        }
        viewHolder.contentLayout.setVisibility(note.isOpen ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewDetachedFromWindow(CardHolder viewHolder) {
        viewHolder.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return filteredItems == null ? 0 : filteredItems.size();
    }

    public int getAllCount() {
        return items == null ? 0 : items.size();
    }
}
