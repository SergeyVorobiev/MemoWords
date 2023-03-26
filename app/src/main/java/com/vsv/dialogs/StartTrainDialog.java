package com.vsv.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.memorizer.R;
import com.vsv.memorizer.dataproviders.TrainGenerator;
import com.vsv.speech.SupportedLanguages;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.statics.CacheData;
import com.vsv.statics.GlobalData;
import com.vsv.statics.TrainMode;
import com.vsv.toasts.Toasts;
import com.vsv.utils.StartTrainLastSettings;
import com.vsv.utils.StaticUtils;

import java.util.Objects;

public class StartTrainDialog extends SingleCustomDialog {

    private final DictionaryBundle dictionaryBundle;

    private final SwitchCompat excRememberedSwitch;

    private final SwitchCompat onlyWrongSwitch;

    private final TextView wordUsageText;

    private final long rememberedCount;

    private int wordUsageCount = 100;

    private final RadioButton wordMode;

    private final RadioButton sentenceMode;

    private final SwitchCompat gameTypeSwitcher;

    private final SwitchCompat wordTypeSwitcher;

    private final SwitchCompat soundTypeSwitcher;

    private final SwitchCompat sentenceTypeSwitcher;

    private final RadioButton soundMode;

    private final RadioButton repetitionMode;

    private final RadioButton gameMode;

    private final String localeAbb;

    private final String leftLocale;

    private final String rightLocale;

    private final long allCount;

    private final String wordPercentage;

    private final ShelfBundle shelfBundle;

    private final CheckBox enunciate;

    public StartTrainDialog(DictionaryBundle dictionaryBundle, ShelfBundle shelfBundle, long wrongSize, long rememberedCount, long allCount) {
        super(R.layout.dialog_start_train, true, true);
        this.leftLocale = dictionaryBundle.leftLocaleAbb;
        this.rightLocale = dictionaryBundle.rightLocaleAbb;
        boolean reverse = GlobalData.getReverse(dictionaryBundle.id);
        if (reverse) {
            this.localeAbb = this.rightLocale;
        } else {
            this.localeAbb = this.leftLocale;
        }
        this.dictionaryBundle = dictionaryBundle;
        this.shelfBundle = shelfBundle;
        this.allCount = allCount;
        this.rememberedCount = rememberedCount;
        View start = dialogView.findViewById(R.id.startTrainButton);
        View cancel = dialogView.findViewById(R.id.cancelTrainButton);
        excRememberedSwitch = dialogView.findViewById(R.id.excRemembered);
        onlyWrongSwitch = dialogView.findViewById(R.id.excWrong);
        enunciate = dialogView.findViewById(R.id.enunciate);
        enunciate.setVisibility(View.GONE);
        if (wrongSize == 0) {
            onlyWrongSwitch.setEnabled(false);
        }
        wordUsageText = dialogView.findViewById(R.id.wordPercentLabel);
        wordPercentage = context.getString(R.string.start_words_percentage_adjust);
        excRememberedSwitch.setOnCheckedChangeListener((button, state) -> {
            int count = TrainGenerator.count(Objects.requireNonNull(CacheData.cachedSamples.get(false)), state, wordUsageCount);
            wordUsageText.setText(String.format(wordPercentage, wordUsageCount, count));
        });
        onlyWrongSwitch.setOnCheckedChangeListener((button, state) -> {
            if (state) {
                excRememberedSwitch.setEnabled(false);
                int count = Math.round(wrongSize * wordUsageCount / 100.0f);
                if (count == 0) {
                    count = 1;
                }
                wordUsageText.setText(String.format(wordPercentage, wordUsageCount, count));
            } else {
                excRememberedSwitch.setEnabled(rememberedCount > 0 && rememberedCount < allCount);
                int count = TrainGenerator.count(Objects.requireNonNull(CacheData.cachedSamples.get(false)), excRememberedSwitch.isChecked(), wordUsageCount);
                wordUsageText.setText(String.format(wordPercentage, wordUsageCount, count));
            }
        });
        SeekBar wordUsageBar = dialogView.findViewById(R.id.wordsUsageBar);
        gameMode = dialogView.findViewById(R.id.gameMode);
        repetitionMode = dialogView.findViewById(R.id.repetitionMode);
        wordMode = dialogView.findViewById(R.id.wordMode);
        sentenceMode = dialogView.findViewById(R.id.sentenceMode);
        soundMode = dialogView.findViewById(R.id.soundMode);
        gameTypeSwitcher = dialogView.findViewById(R.id.gameModeType);
        gameTypeSwitcher.setOnCheckedChangeListener((view, isChecked) ->
                gameTypeSwitcher.setText(isChecked ? R.string.start_mode_type_hard : R.string.start_mode_type_normal));
        gameTypeSwitcher.setVisibility(View.INVISIBLE);
        wordTypeSwitcher = dialogView.findViewById(R.id.wordModeType);
        wordTypeSwitcher.setOnCheckedChangeListener((view, isChecked) ->
                wordTypeSwitcher.setText(isChecked ? R.string.start_mode_type_hard : R.string.start_mode_type_normal));
        wordTypeSwitcher.setVisibility(View.INVISIBLE);
        soundTypeSwitcher = dialogView.findViewById(R.id.soundModeType);
        soundTypeSwitcher.setVisibility(View.INVISIBLE);
        soundTypeSwitcher.setOnCheckedChangeListener((view, isChecked) ->
                soundTypeSwitcher.setText(isChecked ? R.string.start_mode_type_hard : R.string.start_mode_type_normal));
        sentenceTypeSwitcher = dialogView.findViewById(R.id.sentenceModeType);
        sentenceTypeSwitcher.setVisibility(View.INVISIBLE);
        sentenceTypeSwitcher.setOnCheckedChangeListener((view, isChecked) ->
                sentenceTypeSwitcher.setText(isChecked ? R.string.start_mode_sentence_type_hard : R.string.start_mode_sentence_type_normal));

        wordMode.setOnClickListener((view) -> {
            disableAllRadioButtons();
            wordMode.setChecked(true);
            wordTypeSwitcher.setVisibility(View.VISIBLE);
        });

        soundMode.setOnClickListener((view) -> {
            disableAllRadioButtons();
            soundMode.setChecked(true);
            soundTypeSwitcher.setVisibility(View.VISIBLE);
        });

        sentenceMode.setOnClickListener((view) -> {
            disableAllRadioButtons();
            sentenceMode.setChecked(true);
            // sentenceTypeSwitcher.setVisibility(View.VISIBLE);
        });

        gameMode.setOnClickListener((view) -> {
            disableAllRadioButtons();
            gameMode.setChecked(true);
            enunciate.setVisibility(View.VISIBLE);
            gameTypeSwitcher.setVisibility(View.VISIBLE);
        });

        repetitionMode.setOnClickListener((view) -> {
            disableAllRadioButtons();
            repetitionMode.setChecked(true);
        });

        if (StartTrainLastSettings.mode == -1 || StartTrainLastSettings.mode == TrainMode.NORMAL) {
            wordTypeSwitcher.setVisibility(View.VISIBLE);
            wordTypeSwitcher.setChecked(false);
            wordMode.setChecked(true);
        } else if (StartTrainLastSettings.mode == TrainMode.HARD) {
            wordTypeSwitcher.setVisibility(View.VISIBLE);
            wordTypeSwitcher.setChecked(true);
            wordMode.setChecked(true);
        }else if (StartTrainLastSettings.mode == TrainMode.GAME) {
            gameTypeSwitcher.setVisibility(View.VISIBLE);
            gameTypeSwitcher.setChecked(false);
            gameMode.setChecked(true);
            enunciate.setChecked(StartTrainLastSettings.enunciate);
            enunciate.setVisibility(View.VISIBLE);
        } else if (StartTrainLastSettings.mode == TrainMode.GAME_HARD) {
            gameTypeSwitcher.setVisibility(View.VISIBLE);
            gameTypeSwitcher.setChecked(true);
            gameMode.setChecked(true);
            enunciate.setChecked(StartTrainLastSettings.enunciate);
            enunciate.setVisibility(View.VISIBLE);
        } else if (StartTrainLastSettings.mode == TrainMode.SOUND) {
            soundTypeSwitcher.setVisibility(View.VISIBLE);
            soundTypeSwitcher.setChecked(false);
            soundMode.setChecked(true);
        } else if (StartTrainLastSettings.mode == TrainMode.HARD_SOUND) {
            soundTypeSwitcher.setVisibility(View.VISIBLE);
            soundTypeSwitcher.setChecked(true);
            soundMode.setChecked(true);
        } else if (StartTrainLastSettings.mode == TrainMode.REPETITION) {
            repetitionMode.setChecked(true);
        }
        wordUsageBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                wordUsageCount = Math.max(progress, 1);
                int count;
                if (!onlyWrongSwitch.isChecked()) {
                    count = TrainGenerator.count(Objects.requireNonNull(CacheData.cachedSamples.get(false)), excRememberedSwitch.isChecked(), wordUsageCount);
                } else {
                    count = Math.round(wrongSize * wordUsageCount / 100.0f);
                    if (count == 0) {
                        count = 1;
                    }
                    wordUsageText.setText(String.format(wordPercentage, wordUsageCount, count));
                }
                wordUsageText.setText(String.format(wordPercentage, wordUsageCount, count));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        setSwitchState();
        start.setOnClickListener(this::onClickOk);
        cancel.setOnClickListener(this::onClickCancel);
    }

    private void disableAllRadioButtons() {
        gameTypeSwitcher.setVisibility(View.INVISIBLE);
        wordTypeSwitcher.setVisibility(View.INVISIBLE);
        soundTypeSwitcher.setVisibility(View.INVISIBLE);
        sentenceTypeSwitcher.setVisibility(View.INVISIBLE);
        enunciate.setVisibility(View.GONE);
        wordMode.setChecked(false);
        soundMode.setChecked(false);
        sentenceMode.setChecked(false);
        gameMode.setChecked(false);
        repetitionMode.setChecked(false);
    }

    private int getMode() {
        if (wordMode.isChecked()) {
            return wordTypeSwitcher.isChecked() ? TrainMode.HARD : TrainMode.NORMAL;
        } else if (soundMode.isChecked()) {
            return soundTypeSwitcher.isChecked() ? TrainMode.HARD_SOUND : TrainMode.SOUND;
        } else if (sentenceMode.isChecked()) {
            return sentenceTypeSwitcher.isChecked() ? TrainMode.SENTENCE_REVERSE : TrainMode.SENTENCE;
        } else if (repetitionMode.isChecked()) {
            return TrainMode.REPETITION;
        } else if (gameMode.isChecked()) {
            return gameTypeSwitcher.isChecked() ? TrainMode.GAME_HARD : TrainMode.GAME;
        } else {
            return TrainMode.NORMAL;
        }
    }

    private void setSwitchState() {
        excRememberedSwitch.setEnabled(rememberedCount > 0 && rememberedCount < allCount);
        if (excRememberedSwitch.isEnabled()) {
            excRememberedSwitch.setChecked(true);
        }
        int count = TrainGenerator.count(Objects.requireNonNull(CacheData.cachedSamples.get(false)), excRememberedSwitch.isChecked(), wordUsageCount);
        wordUsageText.setText(String.format(wordPercentage, wordUsageCount, count));
    }

    private void onClickOk(View view) {
        dialog.cancel();
        int mode = getMode();
        Bundle bundle = new Bundle();
        shelfBundle.toBundle(bundle);
        dictionaryBundle.toBundle(bundle);
        boolean excCorrect = excRememberedSwitch.isChecked();
        bundle.putBoolean(BundleNames.EXCLUDE_REMEMBERED_OPT, excCorrect);
        bundle.putBoolean(BundleNames.ONLY_WRONG_OPT, onlyWrongSwitch.isChecked());
        bundle.putInt(BundleNames.WORDS_PERCENTAGE, wordUsageCount);
        StartTrainLastSettings.mode = mode;
        StartTrainLastSettings.enunciate = enunciate.isChecked();
        if (mode == TrainMode.GAME || mode == TrainMode.GAME_HARD) {
            bundle.putBoolean(BundleNames.ENUNCIATE, enunciate.isChecked());
            bundle.putString(BundleNames.LANGUAGE, localeAbb);
            bundle.putInt(BundleNames.TRAIN_MODE, mode);
            StaticUtils.navigateSafe(R.id.action_Samples_to_Game, bundle);
        } else if (mode == TrainMode.REPETITION) {
            if (SupportedLanguages.isNotSupport(leftLocale) || SupportedLanguages.isNotSupport(rightLocale)) {
                Toasts.languageNotSpecified();
                return;
            }
            StaticUtils.navigateSafe(R.id.action_Samples_to_Repetition, bundle);
        } else {
            if (mode == TrainMode.SOUND || mode == TrainMode.HARD_SOUND) {
                if (SupportedLanguages.isNotSupport(localeAbb)) {
                    Toasts.languageNotSpecified();
                    return;
                }
            }
            bundle.putInt(BundleNames.TRAIN_MODE, mode);
            bundle.putString(BundleNames.LANGUAGE, localeAbb);
            StaticUtils.navigateSafe(R.id.action_Samples_to_Train, bundle);
        }
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {

    }

    @Override
    public void setupViewListeners(View dialogView) {

    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }
}
