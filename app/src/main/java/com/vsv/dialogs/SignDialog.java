package com.vsv.dialogs;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.util.concurrent.HandlerExecutor;
import com.google.android.gms.tasks.Task;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.ScoreDay;
import com.vsv.graphs.DictionaryGraph;
import com.vsv.memorizer.MainActivity;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerPolicyAdapter;
import com.vsv.models.MainModel;
import com.vsv.statics.GlobalData;
import com.vsv.statics.GlobalExecutors;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.TimeStringConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class SignDialog extends SingleCustomDialog {

    private final String[] ranks = new String[6];

    private SwitchCompat autoLogoutSwitch;

    private SwitchCompat mobileInternetSwitch;

    private Runnable successListener;

    private View logout;

    private View cancel;

    private TextView accountName;

    private TextView accountEmail;

    private TextView accLetterView;

    private ImageView accImageView;

    private TextView dictCountLabelView;

    private TextView samplesCountLabelView;

    private TextView dictCountView;

    private TextView samplesCountView;

    private TextView notesCountLabelView;

    private TextView notesCountView;

    private TextView rememberedWords;

    private ImageView rememberBar;

    private TextView rankView;

    private TextView maxWordsView;

    private SignInButton loginButton;

    private View mainLayout;

    private static final int MAX_WORDS = 200000;

    public SignDialog(int dictionariesCount, int samplesCount, int notesCount) {
        super(R.layout.dialog_sign, true, true);
        ranks[0] = StaticUtils.getString(R.string.rookie);
        ranks[1] = StaticUtils.getString(R.string.beginner);
        ranks[2] = StaticUtils.getString(R.string.seasoned);
        ranks[3] = StaticUtils.getString(R.string.adept);
        ranks[4] = StaticUtils.getString(R.string.master);
        ranks[5] = StaticUtils.getString(R.string.guru);
        this.dictCountLabelView.setText(StaticUtils.getString(R.string.dict_count_label));
        this.dictCountView.setText(String.valueOf(dictionariesCount));
        this.dictCountLabelView.setText(StaticUtils.getString(R.string.dict_count_label));
        dialog.setOnShowListener((dialogInterface) -> calculateRememberedWords());
        this.dictCountView.setText(String.valueOf(dictionariesCount));
        this.notesCountLabelView.setText(StaticUtils.getString(R.string.notes_count_label));
        this.notesCountView.setText(String.valueOf(notesCount));
        this.samplesCountLabelView.setText(StaticUtils.getString(R.string.samples_count_label));
        this.samplesCountView.setText(String.valueOf(samplesCount));
        TextView scoreView = dialogView.findViewById(R.id.scoreValue);
        scoreView.setText(String.valueOf(GlobalData.getSettings().todayScore));
        TextView allScoreView = dialogView.findViewById(R.id.allScoreValue);
        TextView allTrainTimeView = dialogView.findViewById(R.id.allTrainTimeValue);
        allScoreView.setText(String.valueOf(GlobalData.getSettings().score));
        allTrainTimeView.setText(TimeStringConverter.fromSeconds(GlobalData.getSettings().trainTimeInSeconds));
        DictionaryGraph graph = dialogView.findViewById(R.id.graph);
        LiveData<List<ScoreDay>> liveData = StaticUtils.getModel().getScoresRepository().getAllLive();
        liveData.observe(WeakContext.getMainActivity(), scoreDays -> {
            liveData.removeObservers(WeakContext.getMainActivity());
            if (scoreDays == null || scoreDays.isEmpty()) {
                return;
            }
            ArrayList<Float> array = new ArrayList<>();
            scoreDays.sort(Comparator.comparingLong(a -> a.timestamp));
            float[] floats = new float[scoreDays.size()];
            int i = 0;
            for (ScoreDay score : scoreDays) {
                floats[i++] = (float) score.score;
            }
            graph.setupData(floats);
        });
        setOnDismissListener((dialogInterface -> WeakContext.getMainActivity().dialog = null));
        setupAccountLayout();
    }

    public ImageView getAvatar() {
        return accImageView;
    }

    public void setAvatarFromServer(@Nullable Drawable drawable) {
        if (drawable != null) {
            accImageView.setImageDrawable(drawable);
            accLetterView.setText("");
        }
    }

    public void setupAccountLayout() {
        Drawable accountDrawable = GlobalData.googleAccountDrawable.get();
        String signInText = StaticUtils.getString(R.string.sign_in_button_label);
        String signOutText = StaticUtils.getString(R.string.sign_out_button_label);
        String text = GlobalData.account == null ? signInText : signOutText;
        setGooglePlusButtonText(loginButton, text);
        if (GlobalData.account != null) {
            accountEmail.setVisibility(View.VISIBLE);
            accountName.setVisibility(View.VISIBLE);
            String name = GlobalData.account.getDisplayName();
            accountName.setText(name == null ? "" : name);
            String email = GlobalData.account.getEmail();
            accountEmail.setText(email == null ? "" : email);
            if (accountDrawable != null) {
                accImageView.setImageDrawable(GlobalData.googleAccountDrawable.get());
            } else {
                setDefaultImage(email);
            }
        } else {
            accountEmail.setVisibility(View.GONE);
            accountName.setVisibility(View.GONE);
            setDefaultImage(null);
        }
    }

    private void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    private void setDefaultImage(@Nullable String email) {
        accImageView.setImageDrawable(StaticUtils.getDrawable(R.drawable.ic_acc_settings));
        String letter;
        if (email != null && !email.isEmpty()) {
            letter = email.substring(0, 1);
        } else {
            letter = "A";
        }
        accLetterView.setText(letter);
    }

    private int sampleComparator(Sample sample1, Sample sample2) {
        if (sample1.getLeftValue().compareToIgnoreCase(sample2.getLeftValue()) == 0 && sample1.getRightValue().compareToIgnoreCase(sample2.getRightValue()) == 0) {
            return 0;
        } else if (sample1.getLeftValue().compareToIgnoreCase(sample2.getRightValue()) == 0 && sample1.getRightValue().compareToIgnoreCase(sample2.getLeftValue()) == 0) {
            return 0;
        } else {
            String string1 = sample1.getLeftValue() + sample1.getRightValue();
            String string2 = sample2.getLeftValue() + sample2.getRightValue();
            return string1.compareToIgnoreCase(string2);
        }
    }

    private void calculateRememberedWords() {
        MainModel model = StaticUtils.getModelOrNull();
        if (model == null) {
            return;
        }
        Callable<?> callable = () -> {
            TreeSet<Sample> set = new TreeSet<>(this::sampleComparator);
            ArrayList<Long> shelfIds = model.getShelvesRepository().getAllIds().get(10, TimeUnit.SECONDS);
            if (shelfIds == null || !dialog.isShowing()) {
                return null;
            }
            boolean stop = false;
            for (long shelfId : shelfIds) {
                if (stop) {
                    break;
                }
                if (!dialog.isShowing()) {
                    return null;
                }
                ArrayList<Long> dictIds = model.getDictionariesRepository().getAllIds(shelfId).get(10, TimeUnit.SECONDS);
                if (dictIds == null) {
                    continue;
                } else if (!dialog.isShowing()) {
                    return null;
                }
                for (long dictId : dictIds) {
                    if (stop) {
                        break;
                    }
                    if (!dialog.isShowing()) {
                        return null;
                    }
                    ArrayList<Sample> samples = model.getSamplesRepository().getSamples(dictId).get(10, TimeUnit.SECONDS);
                    if (samples == null) {
                        continue;
                    } else if (!dialog.isShowing()) {
                        return null;
                    }
                    for (Sample sample : samples) {
                        if (sample.isRemembered()) {
                            sample.setType(""); // To save memory.
                            sample.setExample("");
                            boolean result = set.add(sample);
                            if (result) {
                                if (set.size() == MAX_WORDS) {
                                    stop = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!dialog.isShowing()) {
                return null;
            }
            final int count = set.size();
            MainActivity activity = WeakContext.getMainActivityOrNull();
            if (activity != null) {
                activity.runOnUiThread(() -> setLevel(count));
            }
            return null;
        };
        GlobalExecutors.modelsExecutor.submit(callable);

    }

    @MainThread
    private void setLevel(int words) {
        float[] rank = getRankNumber(words);
        int level = (int) (words * rank[1]);
        if (level > 10000) {
            level = 10000;
        }
        ((LayerDrawable) rememberBar.getDrawable()).getDrawable(0).setLevel(level);
        ((LayerDrawable) rememberBar.getDrawable()).getDrawable(3).setLevel(level);
        if (words >= MAX_WORDS) {
            rememberedWords.setText(StaticUtils.getString(R.string.more_than, MAX_WORDS));
        } else {
            rememberedWords.setText(String.valueOf(words));
        }
        maxWordsView.setText(String.valueOf((int) rank[2]));
        rankView.setText(ranks[(int) rank[0]]);
        int stars = (int) rank[0] * 2000;
        ((LayerDrawable) rankView.getCompoundDrawablesRelative()[2]).getDrawable(1).setLevel(stars);
    }

    private float[] getRankNumber(int words) {
        if (words < 500) {
            return new float[]{0, 20, 500};
        } else if (words < 2000) {
            return new float[]{1, 5, 2000};
        } else if (words < 5000) {
            return new float[]{2, 2, 5000};
        } else if (words < 10000) {
            return new float[]{3, 1, 10000};
        } else if (words < 20000) {
            return new float[]{4, 0.5f, 20000};
        } else {
            return new float[]{5, 0.5f, 20000};
        }
    }

    @Override
    public void setupViews(View dialogView) {
        logout = dialogView.findViewById(R.id.applyLogout);
        logout.setActivated(true);
        cancel = dialogView.findViewById(R.id.cancelLogout);
        autoLogoutSwitch = dialogView.findViewById(R.id.autoLogout);
        mobileInternetSwitch = dialogView.findViewById(R.id.mobileInternet);
        accountName = dialogView.findViewById(R.id.accountName);
        accLetterView = dialogView.findViewById(R.id.accLetter);
        accImageView = dialogView.findViewById(R.id.accImage);
        accountEmail = dialogView.findViewById(R.id.accountEmail);
        dictCountLabelView = dialogView.findViewById(R.id.dictLabel);
        dictCountView = dialogView.findViewById(R.id.dictCounter);
        notesCountLabelView = dialogView.findViewById(R.id.notesLabel);
        notesCountView = dialogView.findViewById(R.id.notesCounter);
        samplesCountLabelView = dialogView.findViewById(R.id.sampleLabel);
        samplesCountView = dialogView.findViewById(R.id.sampleCounter);
        rememberedWords = dialogView.findViewById(R.id.rememberedWords);
        rememberBar = dialogView.findViewById(R.id.rememberBar);
        rankView = dialogView.findViewById(R.id.rank);
        maxWordsView = dialogView.findViewById(R.id.maxWords);
        loginButton = dialogView.findViewById(R.id.sign_in_button);
        mainLayout = dialogView.findViewById(R.id.main);
        buildPolicy();
    }

    private void buildPolicy() {
        RecyclerView policyRecycler = dialogView.findViewById(R.id.policyContent);
        policyRecycler.setVisibility(View.VISIBLE);
        RecyclerPolicyAdapter policyAdapter = new RecyclerPolicyAdapter();
        policyAdapter.setItems(buildPolicyItems());
        policyRecycler.setAdapter(policyAdapter);
    }

    private ArrayList<RecyclerPolicyAdapter.Item> buildPolicyItems() {
        ArrayList<RecyclerPolicyAdapter.Item> items = new ArrayList<>();
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy2, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy3, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy311, R.dimen.policy_big_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy4, R.dimen.policy_medium_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy5, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy6, R.dimen.policy_medium_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy7, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy8, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy9, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy10, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy11, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy12, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy13, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy14, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy15, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy16, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy17, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy18, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy19, R.dimen.policy_big_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy20, R.dimen.policy_medium_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy21, R.dimen.policy_medium_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy22, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy23, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy24, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy25, R.dimen.policy_medium_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy26, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy27, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy28, R.dimen.policy_big_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy29, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy30, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy31, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy32, R.dimen.policy_big_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy33, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy34, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy35, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy36, R.dimen.policy_big_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy37, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy38, R.dimen.policy_big_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy39, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy40, R.dimen.policy_big_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy41, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy42, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy43, R.dimen.policy_big_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy44, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy45, R.dimen.policy_big_text_size, true));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy46, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(R.string.policy47, R.dimen.policy_small_text_size, false));
        items.add(new RecyclerPolicyAdapter.Item(-1, R.dimen.policy_small_text_size, false));
        return items;
    }

    @Override
    public void setupViewListeners(View dialogView) {
        cancel.setOnClickListener((v) -> this.cancel());
        logout.setOnClickListener(this::logout);
        autoLogoutSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            GlobalData.getSettings().autoLogout = isChecked;
            StaticUtils.updateSettings();
        });
        mobileInternetSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            GlobalData.getSettings().mobileInternet = isChecked;
            StaticUtils.updateSettings();
        });
        loginButton.setOnClickListener(this::login);
    }

    @Override
    public void setupViewAdjustments(View dialogView) {
        autoLogoutSwitch.setChecked(GlobalData.getSettings().autoLogout);
        mobileInternetSwitch.setChecked(GlobalData.getSettings().mobileInternet);
        rememberedWords.setText(StaticUtils.getString(R.string.calculate));
    }

    @SuppressWarnings("deprecation")
    private void login(@Nullable View view) {
        if (GlobalData.account == null) {
            MainActivity context = WeakContext.getMainActivity();
            context.dialog = this;
            Intent signInIntent = GlobalData.getGoogleClient(context).getSignInIntent();
            context.startActivityForResult(signInIntent, MainActivity.RC_SIGN_IN);
        } else {
            logout(view);
        }
    }

    private void logout(@Nullable View view) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            Task<?> task = GlobalData.getGoogleClient(context).signOut();
            task.addOnCompleteListener(new HandlerExecutor(context.getMainLooper()), t -> {
                if (t.isSuccessful()) {
                    GlobalData.account = null;
                    successListener.run();
                }
                setupAccountLayout();
            });
        }
    }

    public void setOnLogoutSuccessListener(@NonNull Runnable successListener) {
        this.successListener = successListener;
    }
}
