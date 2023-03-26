package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.NotebookBundle;
import com.vsv.db.entities.Note;
import com.vsv.db.entities.Notebook;
import com.vsv.db.entities.Settings;
import com.vsv.dialogs.AuthorPageDialog;
import com.vsv.dialogs.SingleCustomDialog;
import com.vsv.dialogs.SingleWindow;
import com.vsv.dialogs.UpdateNotebookDialog;
import com.vsv.memorizer.R;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.AppLink;
import com.vsv.utils.BadgeCollection;
import com.vsv.utils.LinkBuilder;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticFonts;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Symbols;
import com.vsv.utils.Timer;
import com.vsv.viewutils.StopVerticalScrollAnimator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class RecyclerNotebooksAdapter extends AbstractAdapter<RecyclerNotebooksAdapter.CardHolder> {

    private ArrayList<Notebook> items;

    private ArrayList<Notebook> filteredItems;

    private TreeMap<Long, Integer> countMap;

    private final View.OnClickListener clickCardListener;

    private final View.OnClickListener notebookClickListener;

    private final View.OnClickListener onAuthorListener;

    private final View.OnLongClickListener longClickCardListener;

    private final int badgeSize;

    private int animateItem = -1;

    private CountDownTimer timer;

    public final TreeMap<Integer, RecyclerNotebooksAdapter.CardHolder> holders = new TreeMap<>();

    public static class CardHolder extends RecyclerView.ViewHolder {

        private static final Drawable updateDrawable = StaticUtils.getDrawable(R.drawable.bg_question);

        private static final String updateString = StaticUtils.getString(R.string.update);

        private final View card;

        private final View mainView;

        private final TextView name;

        private final Animation animation;

        public final ImageView authorButton;

        public final TextView count;

        public final TextView spreadsheetIdView;

        public final TextView spreadsheetNameView;

        public final TextView sheetNameView;

        public final TextView update;

        private final View paddingView;

        private long transitionTime;

        private boolean isReverse;

        public final TransitionDrawable iconTransitionDrawable;

        public CardHolder(View view, int badgeSize, View.OnClickListener notebookClickListener,
                          View.OnLongClickListener updateListener, View.OnClickListener onAuthorListener) {
            super(view);
            count = view.findViewById(R.id.count);
            paddingView = view.findViewById(R.id.paddingView);
            mainView = view.findViewById(R.id.notebook);
            name = view.findViewById(R.id.notebookName);
            TextView notebookIcon = view.findViewById(R.id.notebookIcon);
            transitionTime = System.nanoTime();
            //notebookIcon.setBackground(StaticUtils.getDrawable(R.drawable.ic_notebook_transition));
            iconTransitionDrawable = (TransitionDrawable) notebookIcon.getCompoundDrawablesRelative()[0];
            // iconTransitionDrawable.setCrossFadeEnabled(true);
            notebookIcon.setOnClickListener(notebookClickListener);
            card = view;
            update = card.findViewById(R.id.update);
            authorButton = card.findViewById(R.id.btnAuthor);
            authorButton.setOnClickListener(onAuthorListener);
            update.setOnClickListener(updateListener::onLongClick);
            animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.from_bottom);
            count.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, BadgeCollection.createRandomBadge(badgeSize), null);
            spreadsheetIdView = view.findViewById(R.id.spreadsheetId);
            spreadsheetNameView = view.findViewById(R.id.spreadsheetName);
            sheetNameView = view.findViewById(R.id.sheetName);
        }

        public TextView getName() {
            return name;
        }

        public void animate() {
            card.startAnimation(animation);
        }

        public void clearAnimation() {
            card.clearAnimation();
        }

        @SuppressLint("SetTextI18n")
        public void setDeveloperInfo(@NonNull Notebook notebook) {
            card.findViewById(R.id.vis).setVisibility(View.VISIBLE);
            ((TextView) card.findViewById(R.id.textView5)).setText("Updated date: " + (notebook.dataDate == null ? "-1" : notebook.dataDate.toString()));
            ((TextView) card.findViewById(R.id.textView6)).setText("Checked date: " + (notebook.updateCheck == null ? "-1" : notebook.updateCheck.toString()));
            ((TextView) card.findViewById(R.id.textView7)).setText("Need update: " + notebook.needUpdate);
            ((TextView) card.findViewById(R.id.textView8)).setText("Sheet id: " + notebook.sheetId);
        }

        public void showUpdate(@NonNull Notebook notebook, int position) {
            if (notebook.needUpdate) {
                update.setVisibility(View.VISIBLE);
                update.setTag(position);
            } else {
                update.setVisibility(View.INVISIBLE);
            }
        }
    }

    public RecyclerNotebooksAdapter(@NonNull RecyclerView owner) {
        StopVerticalScrollAnimator.setRecycleViewAnimation(owner, Spec.MIN_SCROLL_HIT, R.anim.from_bottom2, R.anim.from_top);
        badgeSize = (int) WeakContext.getContext().getResources().getDimension(R.dimen.badge_size);
        this.clickCardListener = this::onNotebookClick;
        this.longClickCardListener = this::updateClick;
        this.notebookClickListener = this::updateFont;
        this.onAuthorListener = this::onAuthorClick;
    }

    private void onAuthorClick(@NonNull View view) {
        new AuthorPageDialog((AppLink) view.getTag()).show();
    }

    private boolean updateClick(View view) {
        int position = (int) view.getTag();
        if (SingleWindow.isShownToast()) {
            return true;
        }
        ArrayList<Note> notes;
        Notebook notebook = this.getItem(position);
        try {
            notes = StaticUtils.getModel().getNotesRepository().getNotes(notebook.getId()).get(15, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Toasts.readFromNotebookError(notebook.getName());
            return true;
        }
        UpdateNotebookDialog dialog = new UpdateNotebookDialog(notebook, notes == null ? new ArrayList<>() : notes);
        dialog.updateNotebookListener(StaticUtils.getModel().getNotebooksRepository()::update);
        dialog.show();
        return true;
    }

    private int getNotesCount(long shelfId) {
        if (countMap != null) {
            Integer value = countMap.getOrDefault(shelfId, 0);
            return value == null ? 0 : value;
        }
        return 0;
    }

    public void setup() {
        this.timer = createTimer();
        this.timer.start();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        this.animateItem = -1;
        this.items = null;
        this.countMap = null;
        this.filteredItems = null;
        this.holders.clear();
        this.timer.cancel();
        this.notifyDataSetChanged();
    }

    private CountDownTimer createTimer() {
        return new CountDownTimer(Integer.MAX_VALUE, 1000) {

            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onTick(long millisUntilFinished) {
                int size = RecyclerNotebooksAdapter.this.holders.values().size();
                if (size == 0) {
                    return;
                }
                int picked = StaticUtils.random.nextInt(size);
                handler.post(() -> {
                    int i = 0;
                    int duration = 2000;
                    int random = StaticUtils.random.nextInt(2);
                    for (RecyclerNotebooksAdapter.CardHolder holder: RecyclerNotebooksAdapter.this.holders.values()) {
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
    private void updateFont(@NonNull View view) {
        Settings settings = GlobalData.getSettings();
        settings.fontNotebookTitleIndex += 1;
        if (settings.fontNotebookTitleIndex >= StaticFonts.fonts.length) {
            settings.fontNotebookTitleIndex = 0;
        }
        StaticUtils.getModel().updateFontNotebookIndex(settings.id, settings.fontNotebookTitleIndex);
        notifyDataSetChanged();
    }

    private void onNotebookClick(View view) {
        if (SingleCustomDialog.isShownToast()) {
            return;
        }
        int position = (int) view.getTag();
        StaticUtils.navigateSafe(R.id.action_Notebooks_to_Notes, NotebookBundle.intoNewBundle(this.getItem(position)));
    }

    @SuppressLint("NotifyDataSetChanged")
    public int applyFilter(String query) {
        GlobalData.notebookSearchQuery = query.toLowerCase();
        filter();
        notifyDataSetChanged();
        return getItemCount();
    }

    public Notebook getItem(int position) {
        return filteredItems.get(position);
    }

    // Not a deep copy, only new array.
    public @Nullable
    ArrayList<Notebook> getCopyItems() {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return new ArrayList<>(items);
    }

    @SuppressLint("NotifyDataSetChanged")
    public int setItems(ArrayList<Notebook> items) {
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
        String filterQuery = GlobalData.notebookSearchQuery;
        if (items == null) {
            filteredItems = null;
            return;
        } else {
            filteredItems = new ArrayList<>(items);
        }
        if (filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<Notebook>) items.stream()
                    .filter((notebook) -> notebook.getName().toLowerCase()
                            .contains(filterQuery.toLowerCase())).collect(Collectors.toList());
        }
        if (GlobalData.getSettings().sortNotebooks) {
            sort();
        }
    }

    private void sort() {
        if (filteredItems != null && filteredItems.size() > 0) {
            Comparator<Notebook> comparator = Comparator.comparing(Notebook::getName);
            filteredItems.sort(comparator);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    public void updateCountMap(TreeMap<Long, Integer> map) {
        countMap = map;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notebook, parent, false);
        View mainView = v.findViewById(R.id.notebook);
        mainView.setOnClickListener(clickCardListener);
        mainView.setOnLongClickListener(longClickCardListener);
        return new CardHolder(v, badgeSize, notebookClickListener, longClickCardListener, onAuthorListener);
    }

    @Override
    public void onBindViewHolder(CardHolder viewHolder, final int position) {
        Notebook notebook = filteredItems.get(position);
        viewHolder.getName().setText(notebook.getName());
        viewHolder.mainView.setTag(position);
        holders.put(viewHolder.hashCode(), viewHolder);
        if (position == getItemCount() - 1) {
            viewHolder.paddingView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.paddingView.setVisibility(View.GONE);
        }
        String spreadsheetId = null;
        if (notebook.hasOwner()) {
            spreadsheetId = StaticUtils.getString(R.string.spreadsheet_id_format, Symbols.ID, notebook.spreadsheetId);
        }
        AppLink appLink = LinkBuilder.buildLinkFromString(notebook.author);
        viewHolder.authorButton.setTag(appLink);
        if (appLink == null) {
            viewHolder.authorButton.setVisibility(View.GONE);
            viewHolder.authorButton.setImageDrawable(null);
        } else {
            viewHolder.authorButton.setVisibility(View.VISIBLE);
            viewHolder.authorButton.setImageDrawable(appLink.icon);
        }
        viewHolder.spreadsheetIdView.setText(spreadsheetId);
        viewHolder.spreadsheetNameView.setText(notebook.spreadsheetName);
        viewHolder.sheetNameView.setText(notebook.sheetName);
        //viewHolder.setDeveloperInfo(notebook);
        int value = notebook.notesCount; // getNotesCount(notebook.getId());
        viewHolder.count.setText(String.valueOf(value));
        viewHolder.name.setTypeface(StaticFonts.fonts[GlobalData.getSettings().fontNotebookTitleIndex], Typeface.BOLD);
        viewHolder.showUpdate(notebook, position);
        if (animateItem < position) {
            animateItem++;
            viewHolder.animate();
        }
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
