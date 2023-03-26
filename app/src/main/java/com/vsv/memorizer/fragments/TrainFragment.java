package com.vsv.memorizer.fragments;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.DictionaryScoreUpdater;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.ScoreDay;
import com.vsv.db.entities.Settings;
import com.vsv.db.entities.Tracker;
import com.vsv.dialogs.EndTrainDialog;
import com.vsv.entities.TrainSample;
import com.vsv.memorizer.R;
import com.vsv.memorizer.dataproviders.TrainGenerator;
import com.vsv.models.MainModel;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.speech.RoboVoice;
import com.vsv.speech.SingleSpeaker;
import com.vsv.speech.SupportedLanguages;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.statics.CacheData;
import com.vsv.statics.GlobalData;
import com.vsv.statics.GlobalExecutors;
import com.vsv.statics.TrainMode;
import com.vsv.statics.WeakContext;
import com.vsv.utils.DateUtils;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.StringUtils;
import com.vsv.utils.Timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TrainFragment extends Fragment {

    private final ArrayList<Float> reactionSpeeds = new ArrayList<>();

    private static float passedTime = 0;

    private static class Answer {

        // Sample contains question value.
        public Sample questionSample;

        public boolean isCorrect;

        // Sample with answer bound to specified button.
        public Sample sample;
    }

    private int currentSampleIndex = 0;

    private TextView questionView;

    private TextView questionWordView;

    private static final AtomicBoolean canSpeak = new AtomicBoolean();

    private TextView kindView;

    private TextView correctCounter;

    private TextView wrongCounter;

    private TextView wordCounter;

    private int iCorrectCounter = 0;

    private int iWrongCounter = 0;

    @SuppressWarnings("FieldCanBeLocal")
    private Drawable questionDrawable;

    private Drawable questionDrawableCorrect;

    private Drawable questionDrawableWrong;

    private Drawable questionDrawableNot;

    private TextView[] answerButtons;

    private ArrayList<TrainSample> trains;

    private DictionaryBundle dictionaryBundle;

    private MainModel model;

    private boolean excRemembered = false;

    private boolean reverse = false;

    private ProgressBar questionTimeBar;

    private final AtomicBoolean answered = new AtomicBoolean();

    private TextView correctAnswerButton;

    private int waitTime = 500;

    private static final int DEFAULT_ANSWER_TIME = 5;

    private int ANSWER_TIME = 5; // Seconds.

    private int mode = 0;

    private static int MAX_REMEMBERING_PERCENTAGE = 40;

    private static final int MAX_REMEMBERING_PERCENTAGE_SOUND = 30;

    private Locale locale;

    private Thread prepareQA;

    private long score;

    private String currentQuestionString;

    private boolean navigate = false;

    private ShelfBundle shelfBundle;

    private static final int COUNTER_START = 0;

    private static final int COUNTER_CORRECT = 1;

    private static final int COUNTER_WRONG = 2;

    private Animation waveAnimationCorrect;

    private Animation waveAnimationWrong;

    private Animation waveAnimationText;

    private float startSamplesPercentage = 0;

    private long tapTime;

    private static volatile boolean showAnswers;

    private boolean calculatedStat = false;

    private long startTrainTime;

    private long trainTime = -1;

    private float textSize;

    private float textSizeReplaced;

    private int currentAnsweredCount = 0;

    // Uses to calculate percent of remembering the word.
    private static final AtomicInteger globalPercentage = new AtomicInteger(0);

    // For Tracker
    private static final AtomicInteger globalDefaultPercentage = new AtomicInteger(0);

    private class TimerQuestion implements Runnable {

        private final TrainFragment fragment;

        private final ProgressBar progressBar;

        private final int mode;

        private final Locale locale;

        private String word = null;

        private Thread thread;

        private final Activity activity;

        private boolean isRemembered;

        private boolean first;

        public TimerQuestion(TrainFragment fragment, ProgressBar progressBar, int mode, Locale locale) {
            this.first = true;
            this.activity = fragment.requireActivity();
            this.fragment = fragment;
            this.progressBar = progressBar;
            this.mode = mode;
            this.locale = locale;
        }

        public void setIsRemembered(boolean isRemembered) {
            this.isRemembered = isRemembered;
        }

        public void setSpeakWord(String word) {
            this.word = word;
        }

        public void interrupt() {
            if (thread != null) {
                thread.interrupt();
            }
        }

        @Override
        public void run() {
            thread = Thread.currentThread();
            if (first) {
                try {
                    first = false;
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    return;
                }
            }
            globalPercentage.set(MAX_REMEMBERING_PERCENTAGE);
            globalDefaultPercentage.set(MAX_REMEMBERING_PERCENTAGE);
            long startTime = System.nanoTime();
            float pastTime = 0;
            final int[] percents = new int[1];
            if (word != null) {
                RoboVoice.getInstance().speak(word, locale);
            }
            boolean firstSpeak = true; // To prevent reset timer when a user tap to speak button.
            TrainFragment.showAnswers = false;
            boolean key = false;
            ANSWER_TIME = DEFAULT_ANSWER_TIME - currentAnsweredCount;

            while (pastTime < ANSWER_TIME && !Thread.currentThread().isInterrupted()) {
                try {
                    if (fragment.answered.get()) {
                        break;
                    }
                    // noinspection BusyWait
                    Thread.sleep(16);
                    if (mode == TrainMode.SOUND || mode == TrainMode.HARD_SOUND) {
                        int status = RoboVoice.getInstance().getStatus(locale);
                        if (status != SingleSpeaker.FINISHED_SPEAKING && firstSpeak) {
                            startTime = System.nanoTime();
                            continue;
                        }
                    }
                    firstSpeak = false;
                    pastTime = (System.nanoTime() - startTime) / 1000000000.0f;
                    passedTime = pastTime;
                    float fPercents = pastTime / ANSWER_TIME;
                    float defaultPercents = pastTime / DEFAULT_ANSWER_TIME; // For Tracker.
                    canSpeak.set(fPercents < 0.8f);
                    float leftTimeP = 1 - fPercents;
                    float leftTimeDefaultP = 1 - defaultPercents; // For Tracker.
                    float leftTime = (float) ANSWER_TIME - pastTime;
                    float koef = 1;
                    if ((mode == TrainMode.HARD || mode == TrainMode.HARD_SOUND || isRemembered) && leftTime >= ANSWER_TIME / 2.0f) {
                        koef = 1.5f;
                    }
                    int rememberedPercentage = (int) (MAX_REMEMBERING_PERCENTAGE * koef * leftTimeP);
                    int defaultRememberedPercentage = (int) (MAX_REMEMBERING_PERCENTAGE * koef * leftTimeDefaultP);
                    if (rememberedPercentage < 0) {
                        rememberedPercentage = 0;
                    }
                    if (defaultRememberedPercentage < 0) {
                        defaultRememberedPercentage = 0;
                    }
                    globalPercentage.set(rememberedPercentage);
                    globalDefaultPercentage.set(defaultRememberedPercentage);
                    percents[0] = (int) (fPercents * 100.0f);
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    if (!key && leftTime <= ANSWER_TIME / 2.0f && (mode == TrainMode.HARD || mode == TrainMode.HARD_SOUND || isRemembered)) {
                        key = true;
                        TrainFragment.showAnswers = true;
                    }
                    activity.runOnUiThread(() -> {
                        try {
                            if (TrainFragment.showAnswers) {
                                TrainFragment.showAnswers = false;
                                TrainFragment.this.showAnswers();

                            }
                            progressBar.setProgress(percents[0]);
                        } catch (Throwable th) {
                            //
                        }
                    });
                    if (percents[0] >= 100) {
                        if (this.fragment.blockButtons()) {
                            activity.runOnUiThread(() -> {
                                try {
                                    this.fragment.blockOrEndByTimeout();
                                } catch (Throwable th) {
                                    //
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    pastTime = ANSWER_TIME;
                }
            }
            RoboVoice.stopSpeaking();
        }
    }

    private TimerQuestion timerQuestion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textSize = StaticUtils.getDimension(R.dimen.train_text_size);
        textSizeReplaced = StaticUtils.getDimension(R.dimen.train_text_size_replaced);
        model = StaticUtils.getModel();
        questionDrawable = StaticUtils.getDrawable(R.drawable.bg_question);
        questionDrawableCorrect = StaticUtils.getDrawable(R.drawable.bg_question_correct);
        questionDrawableWrong = StaticUtils.getDrawable(R.drawable.bg_question_wrong);
        questionDrawableNot = StaticUtils.getDrawable(R.drawable.bg_question_not);
        waveAnimationCorrect = AnimationUtils.loadAnimation(WeakContext.getContext(), R.anim.wave);
        waveAnimationWrong = AnimationUtils.loadAnimation(WeakContext.getContext(), R.anim.wave);
        waveAnimationText = AnimationUtils.loadAnimation(WeakContext.getContext(), R.anim.wave_small);
        Bundle args = this.getArguments();
        int wordsPercentage = 100;
        boolean onlyWrong = false;
        if (args != null) {
            excRemembered = args.getBoolean(BundleNames.EXCLUDE_REMEMBERED_OPT);
            onlyWrong = args.getBoolean(BundleNames.ONLY_WRONG_OPT);
            locale = SupportedLanguages.getLocale(args.getString(BundleNames.LANGUAGE));
            dictionaryBundle = args.getParcelable(BundleNames.DICT);
            shelfBundle = ShelfBundle.fromBundle(args);
            wordsPercentage = args.getInt(BundleNames.WORDS_PERCENTAGE, 100);
            mode = args.getInt(BundleNames.TRAIN_MODE, TrainMode.NORMAL);
        }
        reverse = GlobalData.getReverse(dictionaryBundle.id);
        if (trains == null) {
            trains = CacheData.cachedTrains.get(false);
            if (trains == null) {
                trains = TrainGenerator.prepareDataToTrain(CacheData.cachedSamples.get(false), 4, onlyWrong, excRemembered, reverse, wordsPercentage);
                if (mode == TrainMode.SOUND) {
                    RoboVoice.getInstance().stopSpeaking();
                    MAX_REMEMBERING_PERCENTAGE = MAX_REMEMBERING_PERCENTAGE_SOUND;
                } else if (mode == TrainMode.HARD_SOUND) {
                    RoboVoice.getInstance().stopSpeaking();
                }
            }
        }
        startSamplesPercentage = getSamplesPercentage();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_train, container, false);
        // view.setBackground(GlobalData.bg_default);
        // return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        answerButtons = new TextView[4];
        answerButtons[0] = view.findViewById(R.id.answer0);
        answerButtons[1] = view.findViewById(R.id.answer1);
        answerButtons[2] = view.findViewById(R.id.answer2);
        answerButtons[3] = view.findViewById(R.id.answer3);
        correctCounter = view.findViewById(R.id.correctCounter);
        wrongCounter = view.findViewById(R.id.wrongCounter);
        wordCounter = view.findViewById(R.id.wordCounter);
        questionTimeBar = view.findViewById(R.id.questionTime);
        updateStat(COUNTER_START);
        questionView = view.findViewById(R.id.question_string);
        questionWordView = view.findViewById(R.id.question_word);
        ImageView trainSoundImage = view.findViewById(R.id.trainSoundImage);
        trainSoundImage.setOnClickListener(this::speakTheWord);
        kindView = view.findViewById(R.id.kindLabelTrain);
        questionWordView.setVisibility(mode == TrainMode.SENTENCE ? View.VISIBLE : View.GONE);
        if (mode == TrainMode.SOUND || mode == TrainMode.HARD_SOUND) {
            questionView.setVisibility(View.GONE);
            questionWordView.setVisibility(View.GONE);
        } else {
            trainSoundImage.setVisibility(View.GONE);
        }
        setOnClickListeners();
        onCreateOptionsMenu();
        startTrainTime = System.nanoTime();
        trainTime = -1;
    }

    @Override
    public void onStart() {
        WeakContext.getMainActivity().hideTabs();
        super.onStart();
        timerQuestion = new TimerQuestion(this, questionTimeBar, mode, locale);
        prepareQuestionAndAnswers();
    }

    private void speakTheWord(@Nullable View view) {
        float lastTapTime = Timer.nanoTimeDiffFromNowInSeconds(tapTime);
        if (lastTapTime > 1 && canSpeak.get() && currentQuestionString != null) {
            tapTime = System.nanoTime();
            RoboVoice.getInstance().speak(currentQuestionString, locale);
        }
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.setTitle(R.string.train_title);
        OverflowMenu.setupBackButton(this::navigateToSamplesFragment);
        OverflowMenu.hideAccount();
        OverflowMenu.hideMore();
        OverflowMenu.hideSearch();
    }

    private void setOnClickListeners() {
        for (TextView answerButton : answerButtons) {
            answerButton.setOnClickListener(this::blockOrEndByHuman);
        }
    }

    private void updateStat(int state) {
        correctCounter.setText(String.valueOf(iCorrectCounter));
        wrongCounter.setText(String.valueOf(iWrongCounter));
        if (state == COUNTER_CORRECT) {
            correctCounter.startAnimation(waveAnimationCorrect);
        } else if (state == COUNTER_WRONG) {
            wrongCounter.startAnimation(waveAnimationWrong);
        }
    }

    private void prepareQuestionAndAnswers() {
        canSpeak.set(false); // To not allow user tap the speech button.
        TrainSample currentTrainSample = trains.get(currentSampleIndex);
        currentTrainSample.generatePartAnswers2();
        currentQuestionString = currentTrainSample.question;
        ArrayList<Pair<Sample, Boolean>> answerPairs = currentTrainSample.answers;
        String kind = currentTrainSample.sample.getType();
        int percentage = this.reverse ? currentTrainSample.sample.getLeftPercentage() : currentTrainSample.sample.getRightPercentage();
        currentAnsweredCount = currentTrainSample.sample.getPastTime() < Sample.SERIES_TIME ? currentTrainSample.sample.correctSeries : 0;
        boolean isRemembered = percentage >= 100;
        if (isRemembered) {
            currentAnsweredCount = 2;
        }
        if (!kind.isEmpty()) {
            kindView.setText(kind);
        } else {
            kindView.setText("");
        }
        String correctAnswer = null;
        for (int i = 0; i < answerPairs.size(); i++) {
            TextView answerButton = answerButtons[i];
            Pair<Sample, Boolean> answerPair = answerPairs.get(i);
            Sample sample = answerPair.first;
            Answer answer = new Answer();
            answer.sample = sample;
            answer.questionSample = currentTrainSample.sample;
            answer.isCorrect = answerPair.second;
            if (mode == TrainMode.HARD || mode == TrainMode.HARD_SOUND) {
                answerButton.setText(sample.partAnswerString);
            } else {
                answerButton.setText(sample.answerString);
            }
            answerButton.setTag(answer);
            answerButton.setBackground(StaticUtils.getDrawable(R.drawable.bg_question)); // Can not use cached drawable because it will cache inappropriate drawable for night theme.
            if (answer.isCorrect) {
                correctAnswer = answer.sample.answerString;
                correctAnswerButton = answerButton;
            }
        }
        timerQuestion.setIsRemembered(false);
        if (mode == TrainMode.SOUND || mode == TrainMode.HARD_SOUND) {
            timerQuestion.setSpeakWord(currentQuestionString);
            questionView.setText("");
        } else if (mode == TrainMode.SENTENCE) {
            assert correctAnswer != null;
            String hiddenString = StringUtils.generateSameSymbolsString("*", 3);
            String example = currentTrainSample.sample.getExample();

            int index = -1;
            //ArrayList<String> answerWords = buildAnswerWords(correctAnswer);
            example = example.toUpperCase();
            /*
            for (int i = 0; i < answerWords.size(); i++) {
                index = example.indexOf(answerWords.get(i));
                if (index > -1) {
                    break;
                }
            }
            */
            String answer = "*" + currentQuestionString.toUpperCase() + "*";
            String replacedExample = example.replace(correctAnswer.toUpperCase(), answer);
            if (!replacedExample.equals(example)) {
                ANSWER_TIME = 7;
                questionView.setText(replacedExample);
                questionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeReplaced);
                questionWordView.setText(currentQuestionString);
            } else {
                ANSWER_TIME = 5;
                questionWordView.setText(null);
                questionView.setText(currentQuestionString);
                questionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            }
        } else {
            questionView.setText(currentQuestionString);
        }
        currentSampleIndex++;
        String text = String.format(getString(R.string.from), currentSampleIndex, trains.size());
        wordCounter.setText(text);
        answered.set(false);
        timerQuestion.interrupt();
        GlobalExecutors.singleExecutor.execute(timerQuestion);
    }

    private ArrayList<String> buildExampleWords(@Nullable String example) {
        ArrayList<String> result = new ArrayList<>();
        if (example == null || example.isEmpty()) {
            return result;
        }
        String[] exampleWords = example.split("\\s+");
        result.addAll(Arrays.asList(exampleWords));
        return result;
    }

    @NonNull
    private ArrayList<String> buildAnswerWords(String correctAnswer) {
        ArrayList<String> result = new ArrayList<>();
        int index = correctAnswer.indexOf("(");
        if (index == 0) {
            return result;
        }
        if (index > 0) {
            correctAnswer = correctAnswer.substring(0, index);
        }
        String[] answerWords = correctAnswer.split("\\s+");
        for (int i = 0; i < answerWords.length; i++) {
            answerWords[i] = answerWords[i].replace("(", "");
            answerWords[i] = answerWords[i].replace(")", "");
            answerWords[i] = answerWords[i].replace(".", "");
            answerWords[i] = answerWords[i].replace("!", "");
            answerWords[i] = answerWords[i].replace("?", "");
            answerWords[i] = answerWords[i].replace(",", "");
            answerWords[i] = answerWords[i].replace("/", "");
            String string = answerWords[i].trim();
            if (!string.isEmpty()) {
                result.add(string.toUpperCase());
            }
        }
        return result;
    }

    private void waitAndPrepareQuestionAndAnswers() {
        try {
            Thread.sleep(waitTime);
        } catch (Exception e) {
            return;
        }
        this.requireActivity().runOnUiThread(() -> {
            if (currentSampleIndex >= trains.size()) {
                showEndDialog();
            } else {
                questionTimeBar.setProgress(0);
                answered.set(false);
                prepareQuestionAndAnswers();
                for (TextView answerButton : answerButtons) {
                    answerButton.setClickable(true);
                }
            }
        });
    }

    private synchronized boolean blockButtons() {
        if (answered.get()) {
            return false;
        }
        for (View view : answerButtons) {
            view.setClickable(false);
        }
        answered.set(true);
        return true;
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

    private void blockOrEndByTimeout() {
        waitTime = 500;
        iWrongCounter++;
        reactionSpeeds.add(passedTime);
        RoboVoice.stopSpeaking();
        globalPercentage.set(MAX_REMEMBERING_PERCENTAGE);
        globalDefaultPercentage.set(MAX_REMEMBERING_PERCENTAGE);
        correctAnswerButton.setBackground(questionDrawableNot);
        for (TextView button : answerButtons) {
            button.clearAnimation();
        }
        correctAnswerButton.startAnimation(waveAnimationText);
        Answer answer = (Answer) correctAnswerButton.getTag();
        Sample sample = answer.sample;
        correctAnswerButton.setText(sample.answerString);


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
        updateStatAndNext(COUNTER_WRONG);
    }

    private void updateStatAndNext(int state) {
        updateStat(state);
        prepareQA = new Thread(this::waitAndPrepareQuestionAndAnswers);
        prepareQA.setDaemon(true);
        prepareQA.setName("Q&A");
        prepareQA.start();
    }

    private void showEndDialog() {
        updateTrainTime();
        float result = getSamplesPercentage() - startSamplesPercentage;
        long[] scores = new long[]{0, 0};
        long nowScore = 0;
        if (!calculatedStat) {
            calculatedStat = true;
            scores = calculateStat(score, dictionaryBundle, currentSampleIndex);
            nowScore = score;
        }
        float reactionSpeed = 0;
        int size = reactionSpeeds.size();
        for (int i = 0; i < size; i++) {
            reactionSpeed += reactionSpeeds.get(i);
        }
        reactionSpeed = size == 0 ? 0 : reactionSpeed / size;
        reactionSpeed -= GameFragment.REACTION_SPEED_DISCOUNT;
        reactionSpeed = reactionSpeed < 0 ? 0 : reactionSpeed;
        EndTrainDialog endTrainDialog = new EndTrainDialog(dictionaryBundle.id, scores[0], nowScore, (int) scores[1], result, iCorrectCounter, trains.size(), reactionSpeed, trainTime);
        endTrainDialog.getDialog().setOnDismissListener((dialogInterface) -> navigateToSamplesFragment(null));
        endTrainDialog.show();
    }

    private void blockOrEndByHuman(View view) {
        // Time is out.
        if (!blockButtons()) {
            return;
        }
        reactionSpeeds.add(passedTime);
        RoboVoice.stopSpeaking();
        for (TextView button : answerButtons) {
            button.clearAnimation();
        }
        // Highlight correct button anyway.
        correctAnswerButton.setBackground(questionDrawableCorrect);
        correctAnswerButton.startAnimation(waveAnimationText);
        Answer correctAnswer = (Answer) correctAnswerButton.getTag();
        Sample correctSample = correctAnswer.sample;
        correctAnswerButton.setText(correctSample.answerString);

        Answer answer = (Answer) view.getTag();
        boolean isCorrect = answer.isCorrect;
        Sample answeredSample = answer.sample;
        if (!isCorrect) {
            waitTime = 500;
            iWrongCounter++;
            view.setBackground(questionDrawableWrong);
            ((TextView) view).setText(answeredSample.answerString);

            // Reset counters because of answering wrong.
            if (reverse) {
                answeredSample.setLeftAnswered(1);
                answeredSample.setLeftPercentage(0);
                correctSample.setLeftAnswered(1);
                correctSample.setLeftPercentage(0);

            } else {
                answeredSample.setRightAnswered(1);
                answeredSample.setRightPercentage(0);
                correctSample.setRightAnswered(1);
                correctSample.setRightPercentage(0);
            }
            answeredSample.lastCorrect = false;
            answeredSample.correctSeries = 0;
            model.getSamplesRepository().update(answeredSample);
        } else {
            waitTime = 200;
            iCorrectCounter++;
            int percentage = globalPercentage.get();
            int defaultPercentage = globalDefaultPercentage.get(); // For Tracker
            if (percentage < 1) {
                percentage = 1;
            }
            if (defaultPercentage < 1) {
                defaultPercentage = 1;
            }
            score += defaultPercentage;
            if (reverse) {
                correctSample.setLeftAnswered(2);
                int count = correctSample.getLeftPercentage();
                count += percentage;
                if (count > 100) {
                    count = 100;
                }
                correctSample.setLeftPercentage(count);
            } else {
                correctSample.setRightAnswered(2);
                int count = correctSample.getRightPercentage();
                count += percentage;
                if (count > 100) {
                    count = 100;
                }
                correctSample.setRightPercentage(count);
            }
            updateSeries(correctSample);
        }
        correctSample.answeredDate = Calendar.getInstance().getTime();
        model.getSamplesRepository().update(correctSample);
        globalPercentage.set(MAX_REMEMBERING_PERCENTAGE);
        globalDefaultPercentage.set(MAX_REMEMBERING_PERCENTAGE);
        updateStatAndNext(isCorrect ? COUNTER_CORRECT : COUNTER_WRONG);
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

        // To prevent invoking twice because of 'onPause' method.
        if (navigate) {
            return;
        }
        navigate = true;
        updateTrainTime();
        CacheData.clearAll();
        if (timerQuestion != null) {
            timerQuestion.interrupt();
        }
        if (prepareQA != null) {
            prepareQA.interrupt();
            prepareQA = null;
        }
        if (!calculatedStat) {
            calculatedStat = true;
            calculateStat(score, dictionaryBundle, currentSampleIndex);
        }
        Bundle bundle = shelfBundle.toNewBundle();
        boolean result = StaticUtils.navigateSafe(R.id.action_Train_to_Samples, dictionaryBundle.toBundle(bundle));
        if (!result) {
            navigate = false;
        }
    }

    // Total today score, average score
    public static long[] calculateStat(long score, DictionaryBundle dictionary, int answeredSize) {
        MainModel model = StaticUtils.getModelOrNull();
        long[] scores = new long[]{0, 0};
        long timestamp = DateUtils.getTimestampInDays();
        Settings settings = GlobalData.getSettings();
        if (dictionary.timestampForTodayScore != timestamp) {
            dictionary.timestampForTodayScore = timestamp;
            dictionary.todayScore = score;
        } else {
            dictionary.todayScore += score;
        }
        if (settings.timestampForTodayScore != timestamp) {
            settings.timestampForTodayScore = timestamp;
            settings.todayScore = score;
        } else {
            settings.todayScore += score;
        }
        settings.score += score;
        scores[0] = settings.todayScore;
        if (model != null) {
            model.update(settings);
            model.getScoresRepository().addOrUpdate(new ScoreDay(timestamp, score));

            int average = answeredSize == 0 ? 0 : (int) score / answeredSize;
            scores[1] = average;
            model.getTrackerRepository().insert(new Tracker(dictionary.id, average, score, System.currentTimeMillis()));

            ArrayList<DictionaryScoreUpdater> scoreUpdaters = new ArrayList<>();
            scoreUpdaters.add(new DictionaryScoreUpdater(dictionary.id, dictionary.todayScore, dictionary.timestampForTodayScore));
            model.getDictionariesRepository().updateScore(scoreUpdaters);
        }
        return scores;
    }

    @Override
    public void onPause() {
        navigateToSamplesFragment(null);
        super.onPause();
    }
}