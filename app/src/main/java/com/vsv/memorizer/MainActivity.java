package com.vsv.memorizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.vsv.db.entities.Settings;
import com.vsv.dialogs.SignDialog;
import com.vsv.dialogs.SingleWindow;
import com.vsv.io.StorageCSV;
import com.vsv.memorizer.fragments.GameFragment;
import com.vsv.memorizer.fragments.RepetitionFragment;
import com.vsv.memorizer.fragments.TrainFragment;
import com.vsv.models.MainModel;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.recyclerentities.DictionariesRecycleList;
import com.vsv.recyclerentities.NotebooksRecycleList;
import com.vsv.recyclerentities.NotesRecycleList;
import com.vsv.recyclerentities.SamplesRecycleList;
import com.vsv.recyclerentities.ShelvesRecycleList;
import com.vsv.recyclerentities.SpreadsheetRecyclerList;
import com.vsv.speech.RoboVoice;
import com.vsv.spreadsheet.SheetsBuilder;
import com.vsv.statics.CacheData;
import com.vsv.statics.GlobalData;
import com.vsv.statics.SheetDataUpdater;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.AssetsContentLoader;
import com.vsv.utils.DateUtils;
import com.vsv.utils.StaticUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    public static final int SHELVES_TAB = 0;

    public static final int NOTEBOOKS_TAB = 1;

    public static final int SPREADSHEETS_TAB = 2;

    public static final int RC_SIGN_IN = 153;

    private View tabs;

    private int currentTabIndex = -1;

    private ToggleButton shelvesTab;

    private ToggleButton notebooksTab;

    private ToggleButton spreadsheetsTab;

    private ToggleButton[] tabButtons;

    private MainModel model;

    private FragmentContainerView fragmentContainerView;

    public MainActivity() {
        super();
    }

    public volatile boolean isResumed = false;

    private static String oauthToken = null;

    private static final String oauthTokenFileName = "oauth" + File.separator + "tokens.txt";

    private TextView topCounter;

    private static final int TOKEN_FILE_INDEX = 0;

    // Only main thread
    public SignDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent();
        setupGlobal();
        ShelvesRecycleList.buildCache();
        setContentView(R.layout.activity_main2);
        DictionariesRecycleList.buildCache();
        SpreadsheetRecyclerList.buildCache();
        SamplesRecycleList.buildCache();
        NotebooksRecycleList.buildCache();
        NotesRecycleList.buildCache();
        CacheData.clearAll();
        OverflowMenu.setup();
        SheetDataUpdater.run();
        fragmentContainerView = findViewById(R.id.nav_host_fragment);
        topCounter = findViewById(R.id.topCounter);
        Fragment fragment = fragmentContainerView.getFragment();
        NavHostFragment.findNavController(fragment).addOnDestinationChangedListener((c, d, b) -> {
            SingleWindow.setShown(false);
            OverflowMenu.clear();
        });
        shelvesTab = findViewById(R.id.shelvesTab);
        notebooksTab = findViewById(R.id.notebooksTab);
        spreadsheetsTab = findViewById(R.id.spreadsheetsTab);
        tabButtons = new ToggleButton[]{shelvesTab, notebooksTab, spreadsheetsTab};
        tabs = findViewById(R.id.tabs);
        disableTabs();
        //registerNetwork();
    }

    public void setTopCount(int count, int max) {
        topCounter.setText(StaticUtils.getString(R.string.counter, count, max));
    }

    public void setTab(int index) {
        if (SingleWindow.isShownToast()) {
            return;
        }
        showTabs();
        currentTabIndex = index;
        for (ToggleButton button : tabButtons) {
            button.setBackground(StaticUtils.getDrawable(R.drawable.bg_tab_hidden));
            button.setEnabled(true);
        }
        int centerMargin = StaticUtils.getDimensionInPixels(R.dimen.tab_center_margin);
        int edgeMargin = StaticUtils.getDimensionInPixels(R.dimen.tab_edge_margin);
        int topMargin = StaticUtils.getDimensionInPixels(R.dimen.tab_top_margin);
        if (index == NOTEBOOKS_TAB) {
            notebooksTab.setOnClickListener(null);
            shelvesTab.setOnClickListener((v) -> StaticUtils.navigateSafe(R.id.action_Notebooks_to_Shelves, null));
            spreadsheetsTab.setOnClickListener((v) -> StaticUtils.navigateSafe(R.id.action_Notebooks_to_Spreadsheets, null));
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) notebooksTab.getLayoutParams();
            layoutParams.setMargins(centerMargin, 0, centerMargin, 0);
            layoutParams = (LinearLayout.LayoutParams) shelvesTab.getLayoutParams();
            layoutParams.setMargins(edgeMargin, topMargin, centerMargin, 0);
            layoutParams = (LinearLayout.LayoutParams) spreadsheetsTab.getLayoutParams();
            layoutParams.setMargins(centerMargin, topMargin, edgeMargin, 0);
        } else if (index == SPREADSHEETS_TAB) {
            shelvesTab.setOnClickListener((v) -> StaticUtils.navigateSafe(R.id.action_Spreadsheets_to_Shelves, null));
            notebooksTab.setOnClickListener((v) -> StaticUtils.navigateSafe(R.id.action_Spreadsheets_to_Notebooks));
            spreadsheetsTab.setOnClickListener(null);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) notebooksTab.getLayoutParams();
            layoutParams.setMargins(centerMargin, topMargin, centerMargin, 0);
            layoutParams = (LinearLayout.LayoutParams) shelvesTab.getLayoutParams();
            layoutParams.setMargins(edgeMargin, topMargin, centerMargin, 0);
            layoutParams = (LinearLayout.LayoutParams) spreadsheetsTab.getLayoutParams();
            layoutParams.setMargins(centerMargin, 0, edgeMargin, 0);
        } else {
            shelvesTab.setOnClickListener(null);
            spreadsheetsTab.setOnClickListener((v) -> StaticUtils.navigateSafe(R.id.action_Shelves_to_Spreadsheets, null));
            notebooksTab.setOnClickListener((v) -> StaticUtils.navigateSafe(R.id.action_Shelves_to_Notebooks));
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) notebooksTab.getLayoutParams();
            layoutParams.setMargins(centerMargin, topMargin, centerMargin, 0);
            layoutParams = (LinearLayout.LayoutParams) shelvesTab.getLayoutParams();
            layoutParams.setMargins(edgeMargin, 0, centerMargin, 0);
            layoutParams = (LinearLayout.LayoutParams) spreadsheetsTab.getLayoutParams();
            layoutParams.setMargins(centerMargin, topMargin, edgeMargin, 0);
        }
        tabs.requestLayout();
        tabButtons[index].setBackground(StaticUtils.getDrawable(R.drawable.bg_tab));
    }

    public void disableTabs() {
        currentTabIndex = -1;
        for (ToggleButton button : tabButtons) {
            button.setBackground(StaticUtils.getDrawable(R.drawable.bg_tab_hidden));
            button.setEnabled(false);
        }
    }

    public void hideTabs() {
        tabs.setVisibility(View.GONE);
        topCounter.setVisibility(View.GONE);
    }

    public void showTabs() {
        tabs.setVisibility(View.VISIBLE);
        topCounter.setVisibility(View.VISIBLE);
    }

    @MainThread
    public @Nullable
    NavController getNavController() {
        Fragment fragment;
        try {
            fragment = fragmentContainerView.getFragment();
            if (fragment != null) {
                return NavHostFragment.findNavController(fragment);
            }
        } catch (Throwable th) {
            return null;
        }
        return null;
    }

    private void setupGlobal() {
        WeakContext.setupContext(this);
        model = new ViewModelProvider(this).get(MainModel.class);
        loadSettings();
        setLocale(GlobalData.getSettings().appLocale);
        GlobalData.init();
        GlobalData.bg_dict = StaticUtils.getDrawable(R.drawable.bg_dict);
        // GlobalData.bg_sample = StaticUtils.getDrawable(R.drawable.bg_sample);
        GlobalData.bg_shelf = StaticUtils.getDrawable(R.drawable.bg_shelf);
        GlobalData.bg_notebook = StaticUtils.getDrawable(R.drawable.bg_notebooks);
        // GlobalData.bg_default = StaticUtils.getDrawable(R.drawable.bg_default);
        GlobalData.bg_spreadsheet = StaticUtils.getDrawable(R.drawable.bg_spreadsheet);
    }

    private void setLocale(String languageCode) {
        Locale locale;
        if (languageCode == null || languageCode.isEmpty()) {
            locale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
        } else {
            locale = new Locale(languageCode);
        }
        assert locale != null;
        Locale.setDefault(locale);
        Resources resources = this.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        //noinspection deprecation
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void loadSettings() {
        Settings settings;
        long timestamp = DateUtils.getTimestampInDays();
        try {
            settings = model.getSingleSettings().get();
            if (settings == null) {
                // Toasts.cannotLoadSettings();
                settings = new Settings();
                settings.autoLogout = true;
                settings.timestampForTodayScore = timestamp;
                settings.id = model.insert(settings);
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Can not load settings");
        }
        if (settings.timestampForTodayScore != timestamp) {
            settings.timestampForTodayScore = timestamp;
            settings.todayScore = 0;
            model.update(settings);
        }
        GlobalData.setSettings(settings);
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> GlobalData.getSettings().lastCrashException = thread.getName() + ": " + throwable.getMessage());
        if (settings.lastCrashException != null) {
            Log.e("MEMO_CRASH", settings.lastCrashException);
        }
        String[] presets = AssetsContentLoader.readPresets("presets");
        GlobalData.presetsSpreadsheetId = presets[0];
        GlobalData.presetsSheetName = presets[1];
        googleSignInSetup();
        GlobalData.account = GoogleSignIn.getLastSignedInAccount(this);
        if (settings.autoLogout) {
            signOut();
        }
    }

    public @NonNull
    MainModel getModel() {
        return model;
    }

    @Override
    protected void onStart() {
        StorageCSV.buildOrCleanDocFolderSilence();
        RoboVoice.init();
        // permissionTest();
        if (GlobalData.account != null && GlobalData.googleAccountDrawable.get() == null) {
            OverflowMenu.loadGoogleAccountDrawable(null);
        }
        super.onStart();
    }

    private void registerNetwork() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        ConnectivityManager connectivityManager =
                getSystemService(ConnectivityManager.class);
        connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.e("NETWORK", "onAvailable: " + network);
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.e("NETWORK", "onLost: " + network);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                final boolean notMetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
                boolean result = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                Log.e("NETWORK", "capabilities: " + result);
            }
        });
        /*
        connMgr.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Log.e("NETWORK", "The default network is now: " + network);
            }

            @Override
            public void onLost(Network network) {
                Log.e("NETWORK", "The application no longer has a default network. The last default network was " + network);
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                Log.e("NETWORK", "The default network changed capabilities: " + networkCapabilities);
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                Log.e("NETWORK", "The default network changed link properties: " + linkProperties);
            }
        });*/
    }

    public void permissionTest() {
        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String[] permissions = packageInfo.requestedPermissions;
            int[] permissionsFlags = packageInfo.requestedPermissionsFlags;
            for (String permission : permissions) {
                int value = this.checkSelfPermission(permission);
                Log.d("Permissions", permission + ": " + value);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean tryingToSelfOpen() {
        Uri referrer = this.getReferrer();
        if (referrer != null) {
            String pack = referrer.getAuthority();
            return this.getPackageName().equals(pack);
        }
        return false;
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String type = intent.getType();
            String data = intent.getData() == null ? null : intent.getData().toString();
            if (checkIntentTypeIsCsv(type) && data != null) {
                if (tryingToSelfOpen()) {
                    CacheData.cachedContentPath.set(data);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    this.finishAndRemoveTask();
                }
            }
        }
    }

    private boolean checkIntentTypeIsCsv(@Nullable String type) {
        if (type == null) {
            return false;
        }
        return type.equals("text/comma-separated-values") || type.equals("text/csv") ||
                type.equals("text/x-csv") || type.equals("text/x-comma-separated-values");
    }

    private void signOut() {
        if (GlobalData.account != null) {
            GlobalData.account = null;
            GlobalData.getGoogleClient(this).signOut()
                    .addOnCompleteListener(this, task -> {
                        // ...
                    });
        }
    }

    public MainModel getMainModel() {
        return model;
    }

    private void googleSignInSetup() {
        if (oauthToken == null) {
            ArrayList<String> strings = AssetsContentLoader.readFileAsStrings(oauthTokenFileName, true);
            if (strings.isEmpty()) {
                Log.e("OAuth: ", "Please specify request token in '" + oauthTokenFileName + "'.");
            } else {
                oauthToken = strings.get(TOKEN_FILE_INDEX);
            }
        }
        GlobalData.gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(oauthToken)
                .requestScopes(new Scope(SheetsBuilder.SHEETS_SCOPE))
                //.requestServerAuthCode(clientId)
                .requestEmail()
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GlobalData.account = task.getResult(ApiException.class);
                Handler mainLooper = new Handler(this.getMainLooper());
                mainLooper.post(() -> {
                    Consumer<Drawable> consumer = null;
                    if (dialog != null) {
                        dialog.setupAccountLayout();
                        consumer = dialog::setAvatarFromServer;
                    }
                    OverflowMenu.setAccountDrawable(consumer);
                });
            } catch (ApiException e) {
                Toasts.somethingWentWrongWithAuth(e.getMessage());
                Log.e("MainActivity", "signInResult:failed code=" + e.getMessage());
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment host = this.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (host != null) {
            ArrayList<Fragment> fragments = (ArrayList<Fragment>) host.getChildFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                Class<?> clazz = fragment.getClass();
                if (clazz != TrainFragment.class && clazz != GameFragment.class && clazz != RepetitionFragment.class && fragment.isVisible()) {
                    if (GlobalData.getSettings().autoLogout) {
                        signOut();
                    }
                    this.finish();
                    break;
                }
                if (clazz == GameFragment.class && fragment.isVisible()) {
                    ((GameFragment) fragment).navigateToSamplesFragment(null);
                    break;
                }
                if (clazz == RepetitionFragment.class && fragment.isVisible()) {
                    ((RepetitionFragment) fragment).navigateToSamplesFragment(null);
                    break;
                }
                if (clazz == TrainFragment.class && fragment.isVisible()) {
                    ((TrainFragment) fragment).navigateToSamplesFragment(null);
                }
            }
        }
    }

    @Override
    public void onStop() {
        if (isFinishing()) {
            SheetDataUpdater.shutdown();
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        isResumed = false;
        RoboVoice.stopSpeaking();
        if (isFinishing()) {
            if (GlobalData.getSettings().autoLogout) {
                signOut();
            }
            RoboVoice.shutdown();
            OverflowMenu.dispose();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        isResumed = true;
        super.onResume();
    }
}