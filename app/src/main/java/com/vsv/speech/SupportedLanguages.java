package com.vsv.speech;

import android.util.Log;

import androidx.annotation.NonNull;

import com.vsv.memorizer.R;
import com.vsv.utils.StaticUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;


public final class SupportedLanguages {

    public static class LocaleName {
        
        public final Locale locale;
        
        public final int id;
        
        public LocaleName(Locale locale, int id) {
            this.locale = locale;
            this.id = id;
        }
    }
    
    private SupportedLanguages() {

    }

    private static final TreeMap<String, LocaleName> languages = new TreeMap<>();

    private static final String[] languagesArray;

    public static final String noneAbbreviation = "NONE";

    static {
        languages.put(noneAbbreviation, new LocaleName(buildLocale("", ""), R.string.none));
        languages.put("ru", new LocaleName(buildLocale("ru", "RU"), R.string.russian));
        languages.put("hy", new LocaleName(buildLocale("hy", "HY"), R.string.armenian));
        languages.put("ka", new LocaleName(buildLocale("ka", "KA"), R.string.georgian));
        languages.put("en_US", new LocaleName(buildLocale("en", "US"), R.string.english));
        languages.put("en_GB", new LocaleName(buildLocale("en", "GB"), R.string.british_english));
        languages.put("ko_KR", new LocaleName(buildLocale("ko", "KR"), R.string.korean));
        languages.put("mr", new LocaleName(buildLocale("mr", "IN"), R.string.marathi));
        languages.put("zh_TW", new LocaleName(buildLocale("zh", "TW"), R.string.traditional_chinese));
        languages.put("hu", new LocaleName(buildLocale("hu", "HU"), R.string.hungarian));
        languages.put("th", new LocaleName(buildLocale("th", "TH"), R.string.thai));
        languages.put("ur", new LocaleName(buildLocale("ur", "PK"), R.string.urdu));
        languages.put("nb", new LocaleName(buildLocale("nb", "NO"), R.string.norwegian));
        languages.put("da", new LocaleName(buildLocale("da", "DK"), R.string.danish));
        languages.put("tr", new LocaleName(buildLocale("tr", "TR"), R.string.turkish));
        languages.put("et", new LocaleName(buildLocale("et", "EE"), R.string.estonian));
        languages.put("pt_PT", new LocaleName(buildLocale("pt", "PT"), R.string.portuguese));
        languages.put("vi", new LocaleName(buildLocale("vi", "VN"), R.string.vietnamese));
        languages.put("sv", new LocaleName(buildLocale("sv", "SE"), R.string.swedish));
        languages.put("gu", new LocaleName(buildLocale("gu", "IN"), R.string.gujarati));
        languages.put("kn", new LocaleName(buildLocale("kn", "IN"), R.string.kannada));
        languages.put("el", new LocaleName(buildLocale("el", "GR"), R.string.greek));
        languages.put("hi", new LocaleName(buildLocale("hi", "IN"), R.string.hindi));
        languages.put("fi", new LocaleName(buildLocale("fi", "FI"), R.string.finnish));
        languages.put("km", new LocaleName(buildLocale("km", "KH"), R.string.khmer));
        languages.put("bn", new LocaleName(buildLocale("bn", "IN"), R.string.bengali));
        languages.put("fr_FR", new LocaleName(buildLocale("fr", "FR"), R.string.french));
        languages.put("uk", new LocaleName(buildLocale("uk", "UA"), R.string.ukrainian));
        languages.put("pa", new LocaleName(buildLocale("pa", "IN"), R.string.panjabi));
        languages.put("lv", new LocaleName(buildLocale("lv", "LV"), R.string.latvian));
        languages.put("nl", new LocaleName(buildLocale("nl", "NL"), R.string.netherlands));
        //languages.put("pt_BR", new LocaleName(buildLocale("pt", "BR"), R.string.portuguese));
        languages.put("ml", new LocaleName(buildLocale("ml", "IN"), R.string.malayalam));
        languages.put("de_DE", new LocaleName(buildLocale("de", "DE"), R.string.german));
        languages.put("cs", new LocaleName(buildLocale("cs", "CZ"), R.string.czech));
        languages.put("pl", new LocaleName(buildLocale("pl", "PL"), R.string.polish));
        languages.put("sk", new LocaleName(buildLocale("sk", "SK"), R.string.slovak));
        languages.put("it_IT", new LocaleName(buildLocale("it", "IT"), R.string.italian));
        languages.put("ne", new LocaleName(buildLocale("ne", "NP"), R.string.nepali));
        languages.put("ms", new LocaleName(buildLocale("ms", "MY"), R.string.malay));
        languages.put("zh_CN", new LocaleName(buildLocale("zn", "CH"), R.string.chinese));
        languages.put("es_ES", new LocaleName(buildLocale("es", "ES"), R.string.spanish));
        languages.put("ta", new LocaleName(buildLocale("ta", "IN"), R.string.tamil));
        languages.put("ja_JP", new LocaleName(buildLocale("ja", "JP"), R.string.japanese));
        languages.put("bg", new LocaleName(buildLocale("bg", "BG"), R.string.bulgarian));
        languages.put("te", new LocaleName(buildLocale("te", "IN"), R.string.telugu));
        languages.put("ro", new LocaleName(buildLocale("ro", "RO"), R.string.romanian));
        languages.put("la", new LocaleName(buildLocale("la", ""), R.string.latin));
        languages.put("ca", new LocaleName(buildLocale("ca", ""), R.string.catalan));
        languages.put("sq", new LocaleName(buildLocale("sq", ""), R.string.albanian));
        languages.put("cy", new LocaleName(buildLocale("cy", ""), R.string.welsh));
        languages.put("hr", new LocaleName(buildLocale("hr", ""), R.string.croatian));
        languages.put("ku", new LocaleName(buildLocale("ku", ""), R.string.kurdish));
        languages.put("sr", new LocaleName(buildLocale("sr", ""), R.string.serbian));
        languages.put("ar", new LocaleName(buildLocale("ar", ""), R.string.arabic));
        languages.put("bs", new LocaleName(buildLocale("bs", ""), R.string.bosnian));
        languages.put("sw", new LocaleName(buildLocale("sw", ""), R.string.swahili));
        languages.put("si", new LocaleName(buildLocale("si", ""), R.string.sinhala));
        languagesArray = SupportedLanguages.languages.keySet().toArray(new String[0]);
    }

    public static String getLanguageAbb(int index) {
        return languagesArray[index];
    }
    
    public static String[] getLanguages() {
        String[] result = new String[languages.size()];
        int i = 0;
        for (Map.Entry<String, LocaleName> entry : languages.entrySet()) {
            String abb = entry.getKey();
            String language = StaticUtils.getString(entry.getValue().id);
            result[i++] = abb + " - " + language;
        }
        return result;
    }

    public static int getIndex(@Nullable String abbreviation) {
        for (int i = 0; i < languagesArray.length; i++) {
            if (languagesArray[i].equals(abbreviation)) {
                return i;
            }
        }
        return 0;
    }

    public static @NonNull
    String convertToCorrect(@Nullable String abbreviation) {
        if (abbreviation == null || abbreviation.isEmpty()) {
            return noneAbbreviation;
        }
        LocaleName locale = languages.getOrDefault(abbreviation, null);
        if (locale != null) {
            return abbreviation;
        } else {
            return noneAbbreviation;
        }
    }

    public static boolean isNotSupport(String abbreviation) {
        if (noneAbbreviation.equals(abbreviation)) {
            return true;
        }
        return languages.getOrDefault(abbreviation, null) == null;
    }

    public static @NonNull
    String checkCorrect(@Nullable String abbreviation) {
        if (abbreviation == null) {
            return noneAbbreviation;
        }
        if (languages.getOrDefault(abbreviation, null) == null) {
            return noneAbbreviation;
        }
        return abbreviation;
    }

    public static boolean isNotSpecified(@Nullable String abbreviation) {
        if (abbreviation == null) {
            return true;
        }
        return isNotSupport(abbreviation);
    }

    public static @Nullable
    Locale getLocale(@Nullable String abbreviation) {
        LocaleName localeName = languages.getOrDefault(abbreviation, null);
        return localeName == null ? null : localeName.locale;
    }

    private static @Nullable
    Locale buildLocale(@Nullable String language, @Nullable String country) {
        if (language == null || language.isEmpty()) {
            return null;
        }
        if (country == null || country.isEmpty()) {
            try {
                return Locale.forLanguageTag(language);
            } catch (Exception e) {
                Log.e("BuildSamplesLocale", e.toString());
                return null;
            }
        }
        try {
            return new Locale(language, country);
        } catch (Exception e) {
            Log.e("BuildSamplesLocale", e.toString());
            return null;
        }
    }
}
