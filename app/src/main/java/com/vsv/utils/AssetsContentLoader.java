package com.vsv.utils;

import android.util.Log;

import com.vsv.statics.WeakContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AssetsContentLoader {

    public static String readFileAsString(String fileName) {
        StringBuilder contentBuilder = new StringBuilder();
        // String string = new String(new byte[] {1}, StandardCharsets.UTF_8);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(WeakContext.getMainActivity().getAssets().open(fileName)))) {
            contentBuilder.append(br.readLine());
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append("\n");
                contentBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public static ArrayList<String> readFileAsStrings(String fileName, boolean skipEmptyAndSlashComments) {
        ArrayList<String> strings = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(WeakContext.getMainActivity().getAssets().open(fileName)))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (skipEmptyAndSlashComments) {
                    if (line.isEmpty() || line.startsWith("//")) {
                        continue;
                    }
                }
                strings.add(line);
            }
        } catch (IOException e) {
            Log.e("Read Assets", "Can not open: " + fileName);
            Log.e("Read Assets", e.toString());
            return strings;
        }
        return strings;
    }

    public static String[] readPresets(String fileName) {
        String[] strings = new String[2];
        try (BufferedReader br = new BufferedReader(new InputStreamReader(WeakContext.getMainActivity().getAssets().open(fileName)))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                strings[i++] = line;
                if (i == 2) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }
}
