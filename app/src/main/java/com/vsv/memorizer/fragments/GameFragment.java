package com.vsv.memorizer.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.Settings;
import com.vsv.dialogs.EndTrainDialog;
import com.vsv.entities.TrainSample;
import com.vsv.game.engine.objects.TextEnemy;
import com.vsv.game.engine.screens.RoboVoiceGameScreen;
import com.vsv.game.engine.worlds.ShootWorld;
import com.vsv.memorizer.R;
import com.vsv.memorizer.dataproviders.TrainGenerator;
import com.vsv.models.MainModel;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.speech.RoboVoice;
import com.vsv.speech.SupportedLanguages;
import com.vsv.statics.CacheData;
import com.vsv.statics.GlobalData;
import com.vsv.statics.TrainMode;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Timer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GameFragment extends Fragment implements Runnable {

    private static final int WAIT = 0;

    private static final int PREPARED = 1;

    private static final int NEED_PREPARE = 2;

    private static final int SHOW_ANSWER = 3;

    private static final int EXIT = 4;

    private static final int INTERRUPT = 5;

    private static final int WAIT_ENEMY = 6;

    public static final float REACTION_SPEED_DISCOUNT = 0.9f;

    private long startTrainTime;

    private long trainTime = -1;

    private long score;

    private boolean navigated;

    private static class Answer {

        // Sample contains question value.
        public Sample questionSample;

        public boolean isCorrect;

        // Sample with answer bound to specified button.
        public Sample sample;

        public int buttonIndex;

        public int questionIndex;
    }

    private ShelfBundle shelfBundle;

    private DictionaryBundle dictionaryBundle;

    private ShootWorld world;

    private TextView answer0;

    private TextView answer1;

    private TextView answer2;

    private TextView answer3;

    private TextView[] answerButtons;

    private TextView counter;

    private TextView kindView;

    private TextView correct;

    private TextView wrong;

    private ProgressBar questionProgress;

    private Thread questionThread;

    private final AtomicBoolean buttonsBlocked = new AtomicBoolean();

    private final AtomicInteger state = new AtomicInteger();

    private long startTime = 0;

    private static final float DEFAULT_ANSWER_TIME = 5.5f;

    private static float max_answer_time = DEFAULT_ANSWER_TIME;

    private boolean excRemembered;

    private final static int WAIT_ANSWER_TIME = 500;

    private int wordsPercentage;

    private ArrayList<TrainSample> trains;

    private final ArrayList<Float> reactionSpeeds = new ArrayList<>();

    private int currentSampleIndex = 0;

    private int correctButtonIndex;

    private Handler handler;

    private TextEnemy currentEnemy;

    private LinearLayout controlPanel;

    private int backgroundColor;

    private final Runnable changeBackgroundColor = () -> controlPanel.setBackgroundColor(backgroundColor);

    private int wrongCounter = 0;

    private int correctCounter = 0;

    private static volatile boolean showAnswers;

    private Animation correctAnimation;

    private Animation waveAnimationText;

    private Animation wrongAnimation;

    private RoboVoiceGameScreen gameScreen;

    private static final int MAX_REMEMBERING_PERCENTAGE = 40;

    private float startSamplesPercentage;

    private boolean calculatedStat = false;

    @SuppressWarnings("FieldCanBeLocal")
    private final int correctColor = 0x4D9DFF92;

    @SuppressWarnings("FieldCanBeLocal")
    private final int tintColorBlack = StaticUtils.getColor(R.color.gameTintColorBlack);

    @SuppressWarnings("FieldCanBeLocal")
    private final int tintColorWhite = 0xFFFFFFFF;

    @SuppressWarnings("FieldCanBeLocal")
    private final int wrongColor = 0x4DFF9292;

    @SuppressWarnings("FieldCanBeLocal")
    private final int notAnsweredColor = 0x51FFE66A;

    private Locale locale;

    private int gameMode;

    private final int gameBlockTextColor = StaticUtils.getColor(R.color.gameBlockTextColor);

    private final int gameUnblockTextColor = StaticUtils.getColor(R.color.gameUnblockTextColor);

    private boolean enunciate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigated = false;
        state.set(NEED_PREPARE);
        handler = new Handler(requireActivity().getMainLooper());
        Bundle bundle = savedInstanceState;
        if (bundle == null) {
            bundle = this.getArguments();
        }
        assert bundle != null;
        this.dictionaryBundle = DictionaryBundle.fromBundle(bundle);
        this.shelfBundle = ShelfBundle.fromBundle(bundle);
        locale = SupportedLanguages.getLocale(bundle.getString(BundleNames.LANGUAGE));
        boolean onlyWrong = bundle.getBoolean(BundleNames.ONLY_WRONG_OPT);
        excRemembered = bundle.getBoolean(BundleNames.EXCLUDE_REMEMBERED_OPT);
        enunciate = bundle.getBoolean(BundleNames.ENUNCIATE);
        gameMode = bundle.getInt(BundleNames.TRAIN_MODE);
        wordsPercentage = bundle.getInt(BundleNames.WORDS_PERCENTAGE, 100);
        trains = TrainGenerator.prepareDataToTrain(CacheData.cachedSamples.get(false), 4,
                onlyWrong, excRemembered, GlobalData.getReverse(dictionaryBundle.id), wordsPercentage);
        ArrayList<String> questions = new ArrayList<>(1000);
        for (TrainSample sample : trains) {
            // sample.generatePartAnswers2();
            questions.add(sample.question);
        }
        world = new ShootWorld(questions);
        startSamplesPercentage = getSamplesPercentage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    private boolean cannotWait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }

    @Override
    public void run() {
        showAnswers = false;
        if (currentSampleIndex >= trains.size()) {
            state.set(EXIT);
        } else {
            world.setMainEnemy(currentSampleIndex);
            questionProgress.setProgress(0);
            TrainSample currentTrainSample = trains.get(currentSampleIndex);
            currentTrainSample.generatePartAnswers2();
            counter.setText(StaticUtils.getString(R.string.from, currentSampleIndex + 1, trains.size()));
            ArrayList<Pair<Sample, Boolean>> answerPairs = currentTrainSample.answers;
            kindView.setText(currentTrainSample.sample.getType());
            Sample curSample = currentTrainSample.sample;
            int series = curSample.getPastTime() < Sample.SERIES_TIME ? curSample.correctSeries : 0;
            if (curSample.isRemembered()) {
                series = 2;
            }
            max_answer_time = DEFAULT_ANSWER_TIME - series;
            setupDefaultTintForButtons();
            for (int i = 0; i < answerPairs.size(); i++) {
                TextView answerButton = answerButtons[i];
                Pair<Sample, Boolean> answerPair = answerPairs.get(i);
                Sample sample = answerPair.first;
                Answer answer = new Answer();
                answer.sample = sample;
                answer.questionSample = currentTrainSample.sample;
                answer.isCorrect = answerPair.second;
                answer.questionIndex = currentSampleIndex;
                answer.buttonIndex = i;
                if (answer.isCorrect) {
                    correctButtonIndex = i;
                }
                if (gameMode == TrainMode.GAME_HARD) {
                    answerButton.setText(sample.partAnswerString);
                } else {
                    answerButton.setText(sample.answerString);
                }
                answerButton.setTag(answer);
                // answerButton.setBackground(questionDrawable);
            }
            currentSampleIndex++;
            state.set(PREPARED);
            startTime = System.nanoTime();
            if (enunciate) {
                RoboVoice.getInstance().stopSpeaking();
                String string = GlobalData.getReverse(dictionaryBundle.id) ? curSample.getRightValue() : curSample.getLeftValue();
                RoboVoice.getInstance().speak(string, locale);
            }
            unblockButtons();
        }
    }

    private void prepareQuestion() {
        for (int i = 0; i < 10; i++) {
            boolean result = handler.post(this);
            if (result) {
                return;
            } else {
                Log.e("PrepareQ", "Cannot post question");
            }
        }
        state.set(INTERRUPT);
        navigateToSamplesFragment(null);
    }

    private void checkAnswered() {
        handler.post(() -> {
            float pastTime = Timer.nanoTimeDiffFromNowInSeconds(startTime);
            if (buttonsBlocked.get()) { // User answered.
                state.set(SHOW_ANSWER);
                reactionSpeeds.add(pastTime);
            } else {
                if (gameMode == TrainMode.GAME_HARD && !showAnswers && pastTime >= max_answer_time / 2f) {
                    showAnswers = false;
                    showAnswers();
                }
                int progress = (int) Math.min((pastTime / max_answer_time) * 100.0f, 100.0f);
                questionProgress.setProgress(progress);
                if (progress == 100) {
                    reactionSpeeds.add(pastTime);
                    notAnswered();
                    state.set(SHOW_ANSWER);
                }
            }
        });
    }

    private void showAnswers() {
        TrainSample currentTrainSample = trains.get(currentSampleIndex - 1);
        ArrayList<Pair<Sample, Boolean>> answerPairs = currentTrainSample.answers;
        for (int i = 0; i < answerButtons.length; i++) {
            Pair<Sample, Boolean> answerPair = answerPairs.get(i);
            Sample sample = answerPair.first;
            answerButtons[i].setText(sample.answerString);
        }
    }

    private void questionLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            if (cannotWait(16)) {
                break;
            }
            int curState = state.get();
            if (curState == WAIT) {

                //noinspection UnnecessaryContinue
                continue;
            } else if (curState == NEED_PREPARE) {
                if (!world.isReady()) {
                    continue;
                }
                state.set(WAIT);
                prepareQuestion();
            } else if (curState == PREPARED) {
                checkAnswered();
            } else if (curState == SHOW_ANSWER) {
                if (cannotWait(WAIT_ANSWER_TIME)) {
                    break;
                }
                if (currentEnemy.interact()) {
                    state.set(WAIT_ENEMY);
                } else {
                    state.set(NEED_PREPARE);
                }
            } else if (curState == EXIT) {
                if (!cannotWait(WAIT_ANSWER_TIME)) {
                    showEndDialog();
                }
                break;
            } else if (curState == INTERRUPT) {
                return;
            } else if (curState == WAIT_ENEMY) {
                if (!currentEnemy.interact()) {
                    state.set(NEED_PREPARE);
                }
            }
        }
    }

    private void showEndDialog() {
        handler.post(() -> {
            updateTrainTime();
            float result = getSamplesPercentage() - startSamplesPercentage;
            long[] scores = new long[] {0, 0};
            long nowScore = 0;
            if (!calculatedStat) {
                calculatedStat = true;
                scores = TrainFragment.calculateStat(score, dictionaryBundle, currentSampleIndex);
                nowScore = score;
            }
            float reactionSpeed = 0;
            int size = reactionSpeeds.size();
            for (int i = 0; i < size; i++) {
                reactionSpeed += reactionSpeeds.get(i);
            }
            reactionSpeed = size == 0 ? 0 : reactionSpeed / size;
            reactionSpeed -= REACTION_SPEED_DISCOUNT;
            reactionSpeed = reactionSpeed < 0 ? 0 : reactionSpeed;
            EndTrainDialog endTrainDialog = new EndTrainDialog(dictionaryBundle.id, scores[0], nowScore, (int) scores[1], result, correctCounter, trains.size(), reactionSpeed, trainTime);
            endTrainDialog.getDialog().setOnDismissListener((dialogInterface) -> navigateToSamplesFragment(null));
            endTrainDialog.show();
        });
    }

    private float getSamplesPercentage() {
        ArrayList<Sample> samples = Objects.requireNonNull(CacheData.cachedSamples.get(false));
        float allPercentage = samples.size() * 200;
        float currentPercentage = 0;
        for (Sample sample : samples) {
            currentPercentage += (sample.getLeftPercentage() + sample.getRightPercentage());
        }
        return (currentPercentage / allPercentage) * 100;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        answer0 = view.findViewById(R.id.answer0);
        answer0.setTag(0);
        answer0.setOnClickListener(this::clickAnswer);
        answer1 = view.findViewById(R.id.answer1);
        answer1.setTag(1);
        answer1.setOnClickListener(this::clickAnswer);
        answer2 = view.findViewById(R.id.answer2);
        answer2.setTag(2);
        answer2.setOnClickListener(this::clickAnswer);
        answer3 = view.findViewById(R.id.answer3);
        answer3.setTag(3);
        answer3.setOnClickListener(this::clickAnswer);
        //View gllayout = view.findViewById(R.id.glLayout);
        controlPanel = view.findViewById(R.id.controlPanel); // FF001F37
        counter = view.findViewById(R.id.counter);
        correct = view.findViewById(R.id.correct);
        correct.setText(String.valueOf(correctCounter));
        wrongAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.wave);
        correctAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.wave);
        waveAnimationText = AnimationUtils.loadAnimation(getContext(), R.anim.wave_small);
        wrong = view.findViewById(R.id.wrong);
        wrong.setText(String.valueOf(wrongCounter));
        answerButtons = new TextView[]{answer0, answer1, answer2, answer3};
        setupDefaultTintForButtons();
        kindView = view.findViewById(R.id.kind);
        questionProgress = view.findViewById(R.id.questionProgress);
        gameScreen = view.findViewById(R.id.gameGLScreen);
        gameScreen.setWorld(world);
        gameScreen.setLocale(locale);
        gameScreen.setLoopCallback(() -> {
            backgroundColor = world.getBackgroundColor();
            handler.post(changeBackgroundColor);
        }, 1);
        gameScreen.setOnEmergencyExitCallback(this::onEmergencyExit);
        onCreateOptionsMenu();
        blockButtons();
        questionThread = new Thread(this::questionLoop);
        questionThread.setName("QLoop");
        questionThread.setDaemon(true);
        questionThread.start();
        startTrainTime = System.nanoTime();
        trainTime = -1;
    }

    public void setupDefaultTintForButtons() {
        int color = StaticUtils.isNightMode() ? tintColorBlack : tintColorWhite;
        for (TextView button : answerButtons) {
            button.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    public void clickAnswer(@NonNull View view) {
        boolean result;
        result = blockButtons();
        if (!result) {
            return;
        }
        Answer answer = (Answer) view.getTag();
        TextView answerButton = answerButtons[correctButtonIndex];
        answerButton.setText(((Answer) answerButton.getTag()).sample.answerString);
        answerButton.setBackgroundTintList(ColorStateList.valueOf(correctColor));
        for (TextView button : answerButtons) {
            button.clearAnimation();
        }
        answerButton.startAnimation(waveAnimationText);
        if (!answer.isCorrect) {
            writeWrongAnswer(answer);
            answerButtons[answer.buttonIndex].setText(((Answer) answerButtons[answer.buttonIndex].getTag()).sample.answerString);
            answerButtons[answer.buttonIndex].setBackgroundTintList(ColorStateList.valueOf(wrongColor));
            currentEnemy = world.commandShoot(correctButtonIndex, answer.questionIndex);
            wrongCounter++;
            wrong.setText(String.valueOf(wrongCounter));
            wrong.startAnimation(wrongAnimation);
        } else {
            writeCorrectAnswer(answer, Timer.nanoTimeDiffFromNowInSeconds(startTime));
            currentEnemy = world.commandShootAll(answer.questionIndex);
            correctCounter++;
            correct.setText(String.valueOf(correctCounter));
            correct.startAnimation(correctAnimation);
        }
    }

    private void updateSeries(Sample correctSample) {

        // Count series to check for cool-down.
        if (correctSample.lastCorrect) {
            if (correctSample.getPastTime() < Sample.SERIES_TIME) {
                int series = correctSample.correctSeries;
                if (series < 3) {
                    series += 1;
                    correctSample.correctSeries = series;
                }
            } else {
                correctSample.correctSeries = 1;
            }
        } else {
            correctSample.lastCorrect = true;
            correctSample.correctSeries = 1;
        }
    }

    private void writeCorrectAnswer(Answer answer, float pastTime) {
        MainModel model = StaticUtils.getModelOrNull();
        if (model != null) {
            boolean reverse = GlobalData.getReverse(dictionaryBundle.id);
            Sample answeredSample = answer.sample;
            int[] percentages = calculatePercentage(pastTime);
            int percentage = percentages[0];
            int defaultPercentage = percentages[1];
            score += defaultPercentage;
            if (reverse) {
                answeredSample.setLeftAnswered(2);
                int count = answeredSample.getLeftPercentage();
                count += percentage;
                if (count > 100) {
                    count = 100;
                }
                answeredSample.setLeftPercentage(count);
            } else {
                answeredSample.setRightAnswered(2);
                int count = answeredSample.getRightPercentage();
                count += percentage;
                if (count > 100) {
                    count = 100;
                }
                answeredSample.setRightPercentage(count);
            }
            updateSeries(answeredSample);
            answeredSample.answeredDate = Calendar.getInstance().getTime();
            model.getSamplesRepository().update(answeredSample);
        }
    }

    private void writeWrongAnswer(Answer answer) {
        MainModel model = StaticUtils.getModelOrNull();
        if (model != null) {
            boolean reverse = GlobalData.getReverse(dictionaryBundle.id);
            Sample answeredSample = answer.sample;
            Sample questionSample = answer.questionSample;

            // Reset counters because of answering wrong.
            if (reverse) {
                answeredSample.setLeftAnswered(1);
                answeredSample.setLeftPercentage(0);
                questionSample.setLeftAnswered(1);
                questionSample.setLeftPercentage(0);

            } else {
                answeredSample.setRightAnswered(1);
                answeredSample.setRightPercentage(0);
                questionSample.setRightAnswered(1);
                questionSample.setRightPercentage(0);
            }
            answeredSample.answeredDate = Calendar.getInstance().getTime();
            answeredSample.lastCorrect = false;
            answeredSample.correctSeries = 0;
            model.getSamplesRepository().update(answeredSample);
            model.getSamplesRepository().update(questionSample);
        }
    }

    private void writeNotAnswer(Answer answer) {
        MainModel model = StaticUtils.getModelOrNull();
        if (model != null) {
            Sample sample = answer.sample;
            boolean reverse = GlobalData.getReverse(dictionaryBundle.id);

            // Reset counters because of not answering.
            if (reverse) {
                sample.setLeftPercentage(0);
                sample.setLeftAnswered(1);
            } else {
                sample.setRightPercentage(0);
                sample.setRightAnswered(1);
            }
            sample.answeredDate = Calendar.getInstance().getTime();
            sample.correctSeries = 0;
            sample.lastCorrect = false;
            model.getSamplesRepository().update(sample);
        }
    }

    @Override
    public void onStart() {
        WeakContext.getMainActivity().hideTabs();
        super.onStart();
    }

    private void unblockButtons() {
        buttonsBlocked.set(false);
        answer0.setEnabled(true);
        answer1.setEnabled(true);
        answer2.setEnabled(true);
        answer3.setEnabled(true);
        answer0.setTextColor(gameUnblockTextColor);
        answer1.setTextColor(gameUnblockTextColor);
        answer2.setTextColor(gameUnblockTextColor);
        answer3.setTextColor(gameUnblockTextColor);
    }

    private synchronized boolean blockButtons() {
        if (buttonsBlocked.get()) {
            return false;
        }
        buttonsBlocked.set(true);
        answer0.setEnabled(false);
        answer1.setEnabled(false);
        answer2.setEnabled(false);
        answer3.setEnabled(false);
        answer0.setTextColor(gameBlockTextColor);
        answer1.setTextColor(gameBlockTextColor);
        answer2.setTextColor(gameBlockTextColor);
        answer3.setTextColor(gameBlockTextColor);
        RoboVoice.getInstance().stopSpeaking();
        return true;
    }

    private int[] calculatePercentage(float pastTime) {
        float fPercents = pastTime / max_answer_time;
        float fDefaultPercentage = pastTime / DEFAULT_ANSWER_TIME; // For Tracker
        float leftTime = 1 - fPercents;
        float leftDefaultTime = 1 - fDefaultPercentage; // For Tracker
        float rememberedPercentage = (MAX_REMEMBERING_PERCENTAGE * leftTime);
        float rememberedDefaultPercentage = (MAX_REMEMBERING_PERCENTAGE * leftDefaultTime); // For Tracker
        if (pastTime < max_answer_time / 2f && gameMode == TrainMode.GAME_HARD) {
            rememberedPercentage *= 1.5f;
            rememberedDefaultPercentage *= 1.5f;
        }
        if (rememberedDefaultPercentage < 1f) {
            rememberedDefaultPercentage = 1f;
        }
        if (rememberedPercentage < 1f) {
            rememberedPercentage = 1f;
        }
        return new int[]{(int) rememberedPercentage, (int) rememberedDefaultPercentage};
    }

    public void notAnswered() {
        blockButtons();
        Answer answer = (Answer) answerButtons[correctButtonIndex].getTag();
        writeNotAnswer(answer);
        answerButtons[correctButtonIndex].setBackgroundTintList(ColorStateList.valueOf(notAnsweredColor));
        for (TextView button : answerButtons) {
            button.clearAnimation();
        }
        answerButtons[correctButtonIndex].startAnimation(waveAnimationText);
        currentEnemy = world.commandShoot(correctButtonIndex, answer.questionIndex);
        wrongCounter++;
        wrong.setText(String.valueOf(wrongCounter));
        wrong.startAnimation(wrongAnimation);
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.setTitle(R.string.game_title);
        OverflowMenu.setupBackButton(this::navigateToSamplesFragment);
        OverflowMenu.hideAccount();
        OverflowMenu.hideMore();
        OverflowMenu.hideSearch();
    }

    @Override
    public void onPause() {
        super.onPause();
        navigateToSamplesFragment(null);
    }

    private void onEmergencyExit() {
        requireActivity().runOnUiThread(() -> navigateToSamplesFragment(null));
    }

    private void updateTrainTime() {
        if (trainTime > -1) {
            return;
        }
        trainTime = (long) Timer.nanoTimeDiffFromNowInSeconds(startTrainTime);
        MainModel model = StaticUtils.getModelOrNull();
        if (model != null) {
            Settings settings = GlobalData.getSettings();
            settings.trainTimeInSeconds += trainTime;
            model.update(settings);
        }
    }

    public void navigateToSamplesFragment(@Nullable View view) {
        if (navigated) {
            return;
        }
        updateTrainTime();
        RoboVoice.getInstance().stopSpeaking();
        if (questionThread != null) {
            questionThread.interrupt();
            state.set(INTERRUPT);
        }
        if (gameScreen != null) {
            gameScreen.destroy();
        }
        if (!calculatedStat) {
            calculatedStat = true;
            TrainFragment.calculateStat(score, dictionaryBundle, currentSampleIndex);
        }
        CacheData.clearAll();
        Bundle bundle = shelfBundle.toNewBundle();
        RoboVoice.stopSpeaking();
        navigated = StaticUtils.navigateSafe(R.id.action_Game_to_Samples, dictionaryBundle.toBundle(bundle));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        dictionaryBundle.toBundle(bundle);
        shelfBundle.toBundle(bundle);
        bundle.putBoolean(BundleNames.EXCLUDE_REMEMBERED_OPT, excRemembered);
        bundle.putInt(BundleNames.WORDS_PERCENTAGE, wordsPercentage);
    }

    @Override
    public void onDetach() {
        if (questionThread != null) {
            questionThread.interrupt();
        }
        super.onDetach();
    }
}
