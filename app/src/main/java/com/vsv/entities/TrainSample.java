package com.vsv.entities;

import android.util.Pair;

import com.vsv.db.entities.Sample;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TrainSample {

    public final Sample sample;

    public final ArrayList<Pair<Sample, Boolean>> answers;

    public final String question;

    public TrainSample(Sample sample, String question, ArrayList<Pair<Sample, Boolean>> answers) {
        this.sample = sample;
        this.question = question;
        this.answers = answers;
    }

    public static class PartSample implements Comparable<PartSample> {

        public String value;

        public String part = "";

        public boolean exclude;

        @Override
        public int compareTo(PartSample o) {
            return part.compareTo(o.part);
        }
    }

    private static final ArrayList<PartSample> list = new ArrayList<>();

    public void generatePartAnswers2() {
        for (Pair<Sample, Boolean> pair : answers) {
            PartSample partSample = new PartSample();
            partSample.value = pair.first.answerString;
            list.add(partSample);
        }
        int length = 1;
        int excludes = 0;
        while (excludes < list.size()) {
            for (PartSample part : list) {
                if (part.exclude) {
                    excludes += 1;
                } else if (length > part.value.length()) {
                    part.exclude = true;
                    excludes += 1;
                } else {
                    part.part = part.value.substring(0, length);
                }
            }
            if (excludes == list.size()) {
                break;
            }
            for (int i = 0; i < list.size(); i++) {
                checkEqual(i);
            }
            length += 1;
            excludes = 0;
        }
        for (int i = 0; i < answers.size(); i++) {
            PartSample partSample = list.get(i);
            if (partSample.part.length() == 1) {
                if (partSample.value.length() > 1) {
                    partSample.part = partSample.value.substring(0, 2);
                }
            }
            if (partSample.part.length() < partSample.value.length()) {
                partSample.part = partSample.part + "…";
            }
            answers.get(i).first.partAnswerString = partSample.part;
        }
        list.clear();
    }

    public boolean checkEqual(ArrayList<String> parts) {
        for (int i = 0; i < parts.size(); i++) {
            String value1 = parts.get(i);
            for (int j = 0; j < parts.size(); j++) {
                if (j == i) {
                    continue;
                }
                String value2 = parts.get(j);
                if (value1.equals(value2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkEqual(int checkedIndex) {
        String value = list.get(checkedIndex).part;
        for (int i = 0; i < list.size(); i++) {
            if (i == checkedIndex) {
                continue;
            }
            String value2 = list.get(i).part;
            if (value.equals(value2)) {
                return;
            }
        }
        list.get(checkedIndex).exclude = true;
    }

    @Deprecated
    public void generatePartAnswers() {
        PartSample longest = null;
        ArrayList<PartSample> partSamples = new ArrayList<>();
        for (Pair<Sample, Boolean> pair : answers) {
            PartSample partSample = new PartSample();
            partSample.value = pair.first.answerString;
            int currentSize = pair.first.answerString.length();
            if (longest == null || longest.value.length() < currentSize) {
                longest = partSample;
            }
            partSamples.add(partSample);
        }
        assert longest != null;
        int curLength = longest.value.length();
        int cutLength = curLength;
        for (int i = 1; i < curLength; i++) {

            // Nothing to compare
            if (partSamples.size() < 2) {
                cutLength = i;
                break;
            } else {

                // Cut all words.
                for (PartSample sample : partSamples) {
                    if (i < sample.value.length()) {
                        sample.part = sample.value.substring(0, i);
                    } else {
                        sample.exclude = true;
                        sample.part = sample.value;
                    }
                }
                partSamples.sort(PartSample::compareTo);

                // All word parts are different now.
                if (!checkDuplicates(partSamples)) {
                    cutLength = i;
                    break;
                }
                partSamples = (ArrayList<PartSample>) partSamples.stream().filter((partSample -> !partSample.exclude)).collect(Collectors.toList());
            }
        }
        for (Pair<Sample, Boolean> answer : answers) {
            String first = answer.first.answerString;
            if (first.length() > cutLength) {
                first = first.substring(0, cutLength);
            }
            answer.first.partAnswerString = first;
        }
    }

    private boolean checkDuplicates(ArrayList<PartSample> samples) {
        for (int i = 0; i < samples.size() - 1; i++) {
            String first = samples.get(i).part;
            String second = samples.get(i + 1).part;
            if (first.equals(second)) {
                return true;
            }
        }
        return false;
    }
}
