package com.vsv.overflowmenu;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.widget.SearchView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.vsv.dialogs.SignDialog;
import com.vsv.dialogs.SingleCustomDialog;
import com.vsv.dialogs.SingleWindow;
import com.vsv.memorizer.MainActivity;
import com.vsv.memorizer.R;
import com.vsv.memorizer.fragments.ShelvesFragment;
import com.vsv.statics.GlobalData;
import com.vsv.statics.GlobalExecutors;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Timer;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class OverflowMenu extends SingleWindow {

    @SuppressLint("StaticFieldLeak")
    private static ImageView loginButton;

    private static int clickNumbers;

    private static final int MAX_CLICK_TIME = 200; // Milliseconds

    private static long lastClickTime;

    @SuppressLint("StaticFieldLeak")
    private static ImageView moreButton;

    @SuppressLint("StaticFieldLeak")
    private static ImageView notebookButton;

    @SuppressLint("StaticFieldLeak")
    private static Toolbar toolbar;

    @SuppressLint("StaticFieldLeak")
    private static View actionsLayout;

    private static class SearchListener implements SearchView.OnQueryTextListener {

        private final SearchView searchView;

        private final Consumer<String> consumer;

        public SearchListener(SearchView searchView, Consumer<String> consumer) {
            this.searchView = searchView;
            this.consumer = consumer;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            searchView.clearFocus();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            consumer.accept(newText);
            return false;
        }
    }

    public static void dispose() {
        loginButton = null;
        moreButton = null;
        toolbar = null;
        actionsLayout = null;
        notebookButton = null;
    }

    private final static ArrayList<Pair<View, View.OnClickListener>> items = new ArrayList<>();

    @MainThread
    public static void setup() {
        toolbar = WeakContext.getMainActivity().findViewById(R.id.toolbar);
        actionsLayout = toolbar.getMenu().findItem(R.id.action_more).getActionView();
        assert actionsLayout != null;
        loginButton = actionsLayout.findViewById(R.id.account);
        loginButton.setOnClickListener(OverflowMenu::signInOrOut);
        loginButton.setImageDrawable(GlobalData.getAccountDrawable());
        moreButton = actionsLayout.findViewById(R.id.more);
        moreButton.setOnClickListener(OverflowMenu::open);
    }

    private static void openNotebook(@Nullable View view) {
        if (isShownToast()) {
            return;
        }
        StaticUtils.navigateSafe(R.id.action_Shelves_to_Notebooks);
    }

    @MainThread
    public static void open(@NonNull View anchor) {
        if (isShownToast()) {
            return;
        }
        if (items.isEmpty()) {
            return;
        }
        PopupWindow window = new PopupWindow(anchor.getContext());
        View menu = View.inflate(anchor.getContext(), R.layout.menu_options, null);
        LinearLayout layout = menu.findViewById(R.id.overflowPopup);
        for (Pair<View, View.OnClickListener> item : items) {
            View view = item.first;
            view.setOnClickListener((v) -> {
                window.dismiss();
                item.second.onClick(v);
            });
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            layout.addView(view);
        }
        window.setBackgroundDrawable(StaticUtils.getDrawable(R.drawable.bg_item_dictionary));
        window.setOnDismissListener(() -> setShown(false));
        window.setFocusable(true);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setOutsideTouchable(true);
        window.setOverlapAnchor(true);
        window.setContentView(menu);
        setShown(true);
        int[] location = new int[2];
        anchor.getLocationInWindow(location);
        menu.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int anchorWidth = WeakContext.buildDisplayMetrics().widthPixels - location[0];
        window.showAsDropDown(anchor, -(menu.getMeasuredWidthAndState() - anchorWidth), 0, Gravity.START);
    }

    @MainThread
    public static void addMenuItem(int id, View.OnClickListener listener) {
        FrameLayout view = (FrameLayout) View.inflate(WeakContext.getContext(), R.layout.item_menu, null);
        TextView textView = view.findViewById(R.id.menuItemName);
        textView.setText(StaticUtils.getString(id));
        items.add(new Pair<>(view, listener));
    }

    @MainThread
    public static void hideBackButton() {
        toolbar.setNavigationIcon(null);
    }

    @MainThread
    public static void clear() {
        // hideNotebook();
        SearchView searchView = getSearchView();
        searchView.setOnQueryTextListener(null);
        searchView.setQuery("", true);
        searchView.clearFocus();
        searchView.setFocusable(false);
        searchView.setIconified(true);
        toolbar.setNavigationOnClickListener(null);
        items.clear();
    }

    @MainThread
    public static void setupBackButton(@NonNull View.OnClickListener listener) {
        toolbar.setNavigationIcon(StaticUtils.getDrawable(R.drawable.btn_back));
        toolbar.setNavigationContentDescription(R.string.back_button_description);
        toolbar.setNavigationOnClickListener((v) -> {
            if (!SingleCustomDialog.isShownToast()) {
                listener.onClick(v);
            }
        });
    }

    @MainThread
    public static void setTitle(int id) {
        toolbar.setTitle(id);
        toolbar.setOnClickListener((v) -> {
            long clickTime = Timer.nanoTimeDiffFromNowInMilliseconds(lastClickTime);
            if (clickTime > MAX_CLICK_TIME) {
                clickNumbers = 0;
            }
            clickNumbers += 1;
            if (clickNumbers >= 10) {
                clickNumbers = 0;
                navigateToDebugScreen();
            }
            lastClickTime = System.nanoTime();
        });
    }

    private static SearchView getSearchView() {
        return (SearchView) getSearchMenu().getActionView();
    }

    private static MenuItem getSearchMenu() {
        return toolbar.getMenu().findItem(R.id.action_search);
    }

    private static void navigateToDebugScreen() {
        Log.d("Hello", "Hello");
        if (StaticUtils.isFragmentVisible(ShelvesFragment.class)) {
            StaticUtils.navigateSafe(R.id.action_Shelves_to_Debug);
        }
    }

    @MainThread
    public static void setupSearchView(@NonNull Consumer<String> onTextChanged, @Nullable String cachedQuery) {
        MenuItem menuItem = getSearchMenu().setVisible(true);
        SearchView searchView = (SearchView) menuItem.getActionView();
        if (cachedQuery == null) {
            cachedQuery = "";
        }
        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchListener(searchView, onTextChanged));
        searchView.setOnCloseListener(() -> {
            actionsLayout.setVisibility(View.VISIBLE);
            return false;
        });
        searchView.setOnSearchClickListener((v) -> actionsLayout.setVisibility(View.GONE));
        if (!cachedQuery.isEmpty()) {
            searchView.setFocusable(false);
            searchView.setQuery(cachedQuery, true);
            searchView.setIconified(false);
            searchView.clearFocus();
        }
    }

    public static void hideAccount() {
        loginButton.setVisibility(View.GONE);
    }

    public static void hideSearch() {
        getSearchMenu().setVisible(false);
    }

    public static void hideMore() {
        moreButton.setVisibility(View.GONE);
    }

    public static void hideNotebook() {
        notebookButton.setVisibility(View.GONE);
    }

    public static void showNotebook() {
        notebookButton.setVisibility(View.VISIBLE);
    }

    public static void showMore() {
        moreButton.setVisibility(View.VISIBLE);
    }

    public static void showAccount() {
        loginButton.setVisibility(View.VISIBLE);
    }

    private static void signInOrOut(@Nullable View menuItem) {
        if (isShownToast()) {
            return;
        }
        int dictionariesCount = (int) StaticUtils.getModel().getDictionariesRepository().countOrDefault(5, 0);
        int samplesCount = (int) StaticUtils.getModel().getSamplesRepository().countOrZero(5);
        int notesCount = (int) StaticUtils.getModel().getNotesRepository().countOrZero(5);
        SignDialog dialog = new SignDialog(dictionariesCount, samplesCount, notesCount);
        dialog.setOnLogoutSuccessListener(() -> OverflowMenu.setAccountDrawable(null));
        dialog.show();
    }

    @SuppressWarnings("SameParameterValue")
    private static void loadImage(int size) {
        RoundedBitmapDrawable drawable = null;
        String imgurl = null;
        GoogleSignInAccount account = GlobalData.account;
        Uri uri = account == null ? null : account.getPhotoUrl();
        if (uri != null) {
            imgurl = uri.toString();

            // Specify avatar size
            imgurl = imgurl.substring(0, imgurl.indexOf("="));
        }
        if (imgurl != null) {
            try {
                Bitmap bitmap = Glide.with(WeakContext.getContext()).load(imgurl).asBitmap().into(size, size).get(10, TimeUnit.SECONDS);
                if (bitmap != null) {
                    drawable = RoundedBitmapDrawableFactory.create(WeakContext.getContext().getResources(), bitmap);
                    drawable.setCircular(true);
                    GlobalData.googleAccountDrawable.set(drawable);
                }
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                //
            }
        }
        if (drawable == null) {
            GlobalData.googleAccountDrawable.set(null);
        }
    }

    @MainThread
    public static void loadGoogleAccountDrawable(@Nullable Consumer<Drawable> consumer) {
        GlobalExecutors.modelsExecutor.execute(() -> {
            loadImage(256);
            MainActivity activity = WeakContext.getMainActivityOrNull();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    Drawable drawable = GlobalData.googleAccountDrawable.get();
                    if (consumer != null) {
                        consumer.accept(drawable);
                    }
                });
            }
        });
    }

    public static void setAccountDrawable(@Nullable Consumer<Drawable> consumer) {
        loginButton.setImageDrawable(GlobalData.getAccountDrawable());
        loadGoogleAccountDrawable(consumer);
    }
}
