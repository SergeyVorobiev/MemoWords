package com.vsv.memorizer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Sample;
import com.vsv.entities.TrainSample;
import com.vsv.memorizer.R;
import com.vsv.memorizer.dataproviders.TrainGenerator;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.speech.RoboVoice;
import com.vsv.speech.TwoSpeakers;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.statics.CacheData;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Timer;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class RepetitionFragment extends Fragment {

    private boolean excRemembered;

    private Locale leftLocale;

    private Locale rightLocale;

    private boolean reverse;

    private ArrayList<TrainSample> trains;

    private TextView wordCounter;

    private TextView questionView;

    private TextView answerView;

    private TextView exampleView;

    private TextView kindView;

    private Thread speakThread;

    private final AtomicBoolean replay = new AtomicBoolean(false);

    private boolean navigate = false;

    private volatile boolean run = true;

    private ShelfBundle shelfBundle;

    private DictionaryBundle dictionaryBundle;

    private Animation rotateAnimation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboVoice.stopSpeaking();
        Bundle args = this.getArguments();
        rotateAnimation = AnimationUtils.loadAnimation(WeakContext.getContext(), R.anim.rotate360);
        run = true;
        int wordsPercentage = 100;
        boolean onlyWrong = false;
        if (args != null) {
            onlyWrong = args.getBoolean(BundleNames.ONLY_WRONG_OPT);
            excRemembered = args.getBoolean(BundleNames.EXCLUDE_REMEMBERED_OPT);
            dictionaryBundle = Objects.requireNonNull(args.getParcelable(BundleNames.DICT));
            leftLocale = dictionaryBundle.getLeftLocale();
            rightLocale = dictionaryBundle.getRightLocale();
            shelfBundle = ShelfBundle.fromBundle(args);
            wordsPercentage = args.getInt(BundleNames.WORDS_PERCENTAGE, 100);
        }
        reverse = GlobalData.getReverse(dictionaryBundle.id);
        if (trains == null) {
            trains = CacheData.cachedTrains.get(false);
            if (trains == null) {
                trains = TrainGenerator.prepareDataToTrain(CacheData.cachedSamples.get(false), 4, onlyWrong, excRemembered, reverse, wordsPercentage);
            }
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        View mainView = inflater.inflate(R.layout.fragment_repetition, container, false);
        wordCounter = mainView.findViewById(R.id.wordCounter);
        questionView = mainView.findViewById(R.id.question_string);
        answerView = mainView.findViewById(R.id.answer_string);
        kindView = mainView.findViewById(R.id.kindLabelTrain);
        exampleView = mainView.findViewById(R.id.example);
        mainView.findViewById(R.id.replay).setOnClickListener(this::clickOnReplay);
        // mainView.setBackground(GlobalData.bg_default);
        return mainView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCreateOptionsMenu();
        speakThread = new Thread(this::speakQuestionAndAnswer);
        speakThread.setDaemon(true);
        speakThread.setName("Q&ASpeak");
        speakThread.start();
    }

    private void clickOnReplay(View view) {
        replay.set(!replay.get());
        view.startAnimation(rotateAnimation);
        if (replay.get()) {
            ((FloatingActionButton) view).setImageDrawable(ResourcesCompat
                    .getDrawable(getResources(), R.drawable.btn_repeat_on,
                            requireContext().getTheme()));
        } else {
            ((FloatingActionButton) view).setImageDrawable(ResourcesCompat
                    .getDrawable(getResources(), R.drawable.btn_repeat_off,
                            requireContext().getTheme()));
        }
    }

    private void speakQuestionAndAnswer() {
        boolean isEnd = false;
        int size = trains.size();
        int index = 0;
        Activity activity = this.requireActivity();
        TwoSpeakers voice = RoboVoice.getInstance();
        while (!isEnd && !Thread.currentThread().isInterrupted() && run) {
            if (index == size) {
                if (replay.get()) {
                    index = 0;
                } else {
                    this.requireActivity().runOnUiThread(() -> navigateToSamplesFragment(null));
                    break;
                }
            }
            try {
                Sample sample = trains.get(index++).sample;
                final int number = index;
                String question = reverse ? sample.getRightValue() : sample.getLeftValue();
                String answer = reverse ? sample.getLeftValue() : sample.getRightValue();
                activity.runOnUiThread(() -> setupText(sample, number));

                // noinspection BusyWait
                Thread.sleep(100);
                Locale locale = reverse ? rightLocale : leftLocale;
                voice.speak(question, locale);
                boolean endSpeak = voice.waitFinishSpeaking(locale, 100);
                if (!endSpeak) { // Means interruption.
                    isEnd = true;
                } else {
                    locale = reverse ? leftLocale : rightLocale;
                    long time = System.nanoTime();
                    voice.speak(answer, locale);
                    endSpeak = voice.waitFinishSpeaking(locale, 100);
                    float pastTime = Timer.nanoTimeDiffFromNowInSeconds(time);
                    if (!endSpeak) { // Means interruption.
                        isEnd = true;
                    } else {
                        activity.runOnUiThread(this::clearAnswer);

                        // noinspection BusyWait
                        Thread.sleep(100);
                        voice.speak(question, reverse ? rightLocale : leftLocale);
                        endSpeak = voice.waitFinishSpeaking(reverse ? rightLocale : leftLocale, 100);
                        if (!endSpeak) { // Means interruption.
                            isEnd = true;
                        } else {

                            // noinspection BusyWait
                            Thread.sleep((int) (pastTime * 1000.0f));
                        }
                    }
                }
            } catch (InterruptedException e) {
                isEnd = true;
            }
        }
    }

    private void setupText(Sample sample, int number) {
        String question = reverse ? sample.getRightValue() : sample.getLeftValue();
        String answer = reverse ? sample.getLeftValue() : sample.getRightValue();
        String kind = sample.getType();
        String example = sample.getExample();
        exampleView.setText(example);
        questionView.setText(question);
        answerView.setText(answer);
        kindView.setText(kind);
        String text = String.format(getString(R.string.from), number, trains.size());
        wordCounter.setText(text);
    }

    private void clearAnswer() {
        answerView.setText("");
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.setTitle(R.string.repetition_title);
        OverflowMenu.setupBackButton(this::navigateToSamplesFragment);
        OverflowMenu.hideAccount();
        OverflowMenu.hideMore();
        OverflowMenu.hideSearch();
    }

    public void navigateToSamplesFragment(@Nullable View view) {

        // To prevent invoking twice because of 'onPause' method.
        if (navigate) {
            return;
        }
        navigate = true;
        CacheData.clearAll();
        if (speakThread != null) {
            RoboVoice.getInstance().stopSpeaking();
            speakThread.interrupt();
            speakThread = null;
        }
        Bundle bundle = shelfBundle.toNewBundle();
        bundle.putParcelable(BundleNames.DICT, dictionaryBundle);
        StaticUtils.navigateSafe(R.id.action_Repetition_to_Samples, bundle);
    }

    @Override
    public void onStart() {
        WeakContext.getMainActivity().hideTabs();
        super.onStart();
    }

    @Override
    public void onPause() {
        run = false;
        navigateToSamplesFragment(null);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
