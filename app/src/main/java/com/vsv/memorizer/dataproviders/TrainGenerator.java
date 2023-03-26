package com.vsv.memorizer.dataproviders;

import android.util.Pair;

import com.vsv.db.entities.Sample;
import com.vsv.entities.TrainSample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

public class TrainGenerator {

    private static final Random random = new Random();

    // Contains pairs of questions with array of pairs of possible variants for each variant the boolean flag represent its correctness.
    public static ArrayList<TrainSample> prepareDataToTrain(ArrayList<Sample> samples, int count, boolean onlyWrong, boolean excludeRemembered, boolean reverse, int percentage) {
        ArrayList<TrainSample> result = new ArrayList<>();
        ArrayList<Sample> arrAnswers = TrainGenerator.prepareAnswers(samples, reverse);
        for (Sample sample : prepareQuestions(samples, reverse, onlyWrong, excludeRemembered, percentage)) {
            String question = reverse ? sample.getRightValue() : sample.getLeftValue();
            ArrayList<Pair<Sample, Boolean>> answers = createAnswers(count, sample.tempIndex, arrAnswers);
            result.add(new TrainSample(sample, question, answers));
        }
        Collections.shuffle(result, random);
        return result;
    }

    private static void indexingSamples(ArrayList<Sample> samples) {
        for (int i = 0; i < samples.size(); i++) {
            samples.get(i).tempIndex = i;
        }
    }

    private static ArrayList<Sample> prepareAnswers(ArrayList<Sample> samples, boolean reverse) {
        ArrayList<Sample> result = new ArrayList<>();
        for (Sample sample : samples) {
            sample.answerString = reverse ? sample.getLeftValue() : sample.getRightValue();
            result.add(sample);
        }
        return result;
    }

    public static int count(ArrayList<Sample> samples, boolean excludeRemembered, int percentage) {
        int size = (int) samples.stream().filter((sample) -> {
            if (excludeRemembered && sample.isRemembered()) {
                return false;
            }
            return !sample.isLocked();
        }).count();
        int newSize;
        float k = percentage / 100.0f;
        newSize = Math.round(size * k);
        if (newSize == 0) {
            newSize = 1;
        }
        return newSize;
    }

    private static ArrayList<Sample> prepareQuestions(ArrayList<Sample> samples, boolean reverse, boolean onlyWrong, boolean excludeRemembered, int percentage) {
        TrainGenerator.indexingSamples(samples);
        ArrayList<Sample> filtered = (ArrayList<Sample>) samples.stream().filter((sample) -> !sample.isLocked()).collect(Collectors.toList());
        if (filtered.isEmpty()) {
            throw new RuntimeException("There is no questions to ask.");
        }
        if (onlyWrong) {
            filtered = (ArrayList<Sample>) filtered.stream().filter((sample) -> {
                if (reverse) {
                    return sample.getLeftAnswered() == 1;
                } else {
                    return sample.getRightAnswered() == 1;
                }
            }).collect(Collectors.toList());
        } else if (excludeRemembered) {
            filtered = (ArrayList<Sample>) filtered.stream().filter((sample) -> !sample.isRemembered()).collect(Collectors.toList());
        }
        if (filtered.isEmpty()) {
            throw new RuntimeException("There is no questions to ask.");
        }
        int size = filtered.size();
        int newSize;
        float k = percentage / 100.0f;
        newSize = Math.round(size * k);
        if (newSize == 0) {
            newSize = 1;
        }
        Collections.shuffle(filtered);
        if (newSize != size) {
            return (ArrayList<Sample>) filtered.stream().limit(newSize).collect(Collectors.toList());
        }
        return filtered;
    }

    private static ArrayList<Pair<Sample, Boolean>> createAnswers(int count, int correctAnswerIndex, ArrayList<Sample> arrAnswers) {
        int size = arrAnswers.size();
        int lastIndex = size - 1;
        ArrayList<Integer> indexes = new ArrayList<>();
        indexes.add(correctAnswerIndex);
        ArrayList<Pair<Sample, Boolean>> result = new ArrayList<>();
        if (correctAnswerIndex > size || correctAnswerIndex < 0) {
            throw new RuntimeException("Wrong correct answer index: " + correctAnswerIndex);
        }
        if (count > size) {
            throw new RuntimeException("Count of answers cannot be more than count of samples.");
        }
        result.add(new Pair<>(arrAnswers.get(correctAnswerIndex), true));
        for (int i = 0; i < count - 1; i++) {
            int index = random.nextInt(size);
            while (indexes.contains(index)) {
                index += 1;
                if (index > lastIndex) {
                    index = 0;
                }
            }
            indexes.add(index);
        }
        for (int i = 1; i < indexes.size(); i++) {
            result.add(new Pair<>(arrAnswers.get(indexes.get(i)), false));
        }
        Collections.shuffle(result, random);
        return result;
    }
}
