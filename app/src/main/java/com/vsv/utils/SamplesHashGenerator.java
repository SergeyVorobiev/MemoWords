package com.vsv.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.db.entities.Sample;

import java.util.List;

public class SamplesHashGenerator {

    @NonNull
    public static String getSSH(@Nullable List<Sample> samples, @Nullable String email) {
        StringBuilder builder = new StringBuilder();
        if (samples == null || samples.isEmpty()) {
            return "";
        }
        for (Sample sample : samples) {
            builder.append(sample.buildMD5());
        }
        builder.append(email == null ? "" : email);
        return HashGenerator.getSSH(builder.toString());
    }
}
