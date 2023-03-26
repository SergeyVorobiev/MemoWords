package com.vsv.memorizer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.vsv.utils.DateUtils;
import com.vsv.utils.HashGenerator;
import com.vsv.utils.Timer;
import com.vsv.utils.merger.TwoFastComparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.vsv.memorizer", appContext.getPackageName());
    }

    @Test
    public void dateTest() {
        float value = 1000 * 60 * 60 * 24;
        Date date = new Date(122, 11, 28);
        Date date1 = new Date(122, 11, 28);
        Date date2 = new Date(122, 11, 29);
        Date date3 = new Date(122, 12, 29);
        System.out.println((int) (date.getTime() / value));
        System.out.println((int) (date1.getTime() / value));
        System.out.println((int) (date2.getTime() / value));
        System.out.println((int) (date3.getTime() / value));
    }

    @Test
    public void colorTest() {
        int color = 0x1DFF0000;
        float[] argb = new float[] {Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color)};
    }

    @Test
    public void tt() {
        StringBuilder builder = new StringBuilder();
        Rect rect = new Rect();
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
        String text = "Hello world and I love you\n    And now we can get this   ";
        builder.append(text);
        String[] strings = text.split("\n", MAX_LINES);
        int size = Math.min(MAX_LINES, strings.length);
        for (int i = 0; i < size; i++) {
            String line = strings[i].trim();
            builder.setLength(0);
            builder.append(line);
            addLines(builder, textPaint, rect);
            if (linesCount == MAX_LINES) {
                break;
            }
        }
        for (int i = 0; i < linesCount; i++) {
            System.out.println(lines[i]);
        }
    }

    private static int MAX_WIDTH = 200;

    private static int MAX_LINES = 4;

    private static String[] lines = new String[MAX_LINES];

    private static int linesCount = 0;

    private static String endSymbol = "…";

    private void addLines(StringBuilder stringBuilder, Paint textPaint, Rect temp) {
        char[] chars = new char[200];
        int begin = 0;
        int count = 0;
        int i;
        for (i = 0; i < stringBuilder.length(); i++) {
            int end = i + 1;
            count += 1;
            stringBuilder.getChars(begin, end, chars, 0);
            textPaint.getTextBounds(chars, 0, count, temp);
            if (temp.width() > MAX_WIDTH) {
                count = 0;
                String toAdd = stringBuilder.substring(begin, i).trim();
                lines[linesCount++] = toAdd;
                begin = i;
                if (linesCount == MAX_LINES) {
                    if (!endSymbol.isEmpty()) {
                        lines[lines.length - 1] = toAdd + endSymbol;
                    }
                    break;
                }
            }
        }
        if (linesCount < MAX_LINES) {
            String string = stringBuilder.substring(begin, i);
            if (!string.isEmpty()) {
                lines[linesCount++] = stringBuilder.substring(begin, i);
            }
        }
    }

    @Test
    public void testTttest() throws Exception {
        String input = "helloasdf;asjdf;asdfjas;ldfjsa;dfljsa;dfljsa;fjlsd" + "asdf;jasd;fljasfl;djsadfl;jasfd;jasfdl";
        Timer timer = Timer.newStart();
        for (int i = 0; i < 10000; i++) {
            HashGenerator.getMD5(input);
        }
        System.out.println(HashGenerator.getMD5(input) + " " + timer.stopInSeconds());
        timer.start();
        System.out.println(HashGenerator.getSSH(input) + " " + timer.stopInSeconds());
        System.out.println(Arrays.toString(HashGenerator.createSalt()));
    }

    @Test
    public void comparatorTest() {
        //List<Integer> list = Arrays.asList(1, 2, 5, 9, 3, 3, 4, 10);
        //List<Integer> list2 = Arrays.asList(1, 7, 5, 4, 8, 6, 3);
        //CompareObject c1 = new CompareObject(1, "a");
        CompareObject c2 = new CompareObject(2, "b");
        CompareObject c3 = new CompareObject(3, "c");
        CompareObject c4 = new CompareObject(4, "d");

        //CompareObject c5 = new CompareObject(1, "e");
        CompareObject c6 = new CompareObject(2, "f");
        CompareObject c7 = new CompareObject(4, "g");
        //CompareObject c8 = new CompareObject(6, "h");

        List<CompareObject> list1 = Arrays.asList(c2, c3, c4);
        List<CompareObject> list2 = Arrays.asList(c6, c7);
        Comparator<CompareObject> comparatorObject = (left, right) -> {
            if (left == null && right == null) {
                return 0;
            } else if (left != null && right != null) {
                return Integer.compare(left.index, right.index);
            } else if (left != null) {
                return 1;
            } else {
                return -1;
            }
        };
        TwoFastComparator.LeftRightComparator<CompareObject, CompareObject> lrComparator = (CompareObject left, CompareObject right) -> {
            if (left == null && right == null) {
                return 0;
            } else if (left != null && right != null) {
                return Integer.compare(left.index, right.index);
            } else if (left != null) {
                return 1;
            } else {
                return -1;
            }
        };
        TwoFastComparator.CompareProcess<CompareObject, CompareObject> process = new TwoFastComparator.CompareProcess<CompareObject, CompareObject>() {

            @Override
            public boolean leftBigger(@Nullable @org.jetbrains.annotations.Nullable CompareObject left, @Nullable @org.jetbrains.annotations.Nullable CompareObject right, int leftIndex, int rightIndex) {
                Log.d("Compare", "leftMoreThanRight: " + left + " - " + right + "; indexes: " + leftIndex + " - " + rightIndex);
                return true;
            }

            @Override
            public boolean rightBigger(@Nullable @org.jetbrains.annotations.Nullable CompareObject left, @Nullable @org.jetbrains.annotations.Nullable CompareObject right, int leftIndex, int rightIndex) {
                Log.d("Compare", "rightMoreThanLeft: " + left + " - " + right + "; indexes: " + leftIndex + " - " + rightIndex);
                return true;
            }

            @Override
            public boolean equal(@Nullable @org.jetbrains.annotations.Nullable CompareObject left, @Nullable @org.jetbrains.annotations.Nullable CompareObject right, int leftIndex, int rightIndex) {
                Log.d("Compare", "leftEqualToRight: " + left + " - " + right + "; indexes: " + leftIndex + " - " + rightIndex);
                return true;
            }

            @Override
            public boolean left(@Nullable @org.jetbrains.annotations.Nullable CompareObject left, int leftIndex) {
                Log.d("Compare", "onlyLeft: " + left + "; index: " + leftIndex);
                return true;
            }

            @Override
            public boolean right(@Nullable @org.jetbrains.annotations.Nullable CompareObject right, int rightIndex) {
                Log.d("Compare", "onlyRight: " + right + "; index: " + rightIndex);
                return true;
            }
        };
        TwoFastComparator<CompareObject, CompareObject> fastComparator = new TwoFastComparator<>(list1, list2, comparatorObject, comparatorObject, lrComparator, process);
        fastComparator.compare();
    }

    @Test
    public void simpleTest() {
        List<String> ranges = Arrays.asList(
                //Range names ...
        );
        String spreadsheetId2 = "https://docs.google.com/spreadsheets/d/1WStECPaEaNYCqpj_GrwSdqF9ppZ2YyzdtDPB6IF13bI/edit?usp=sharing";
        String spreadsheetId = "1WStECPaEaNYCqpj_GrwSdqF9ppZ2YyzdtDPB6IF13bI";
        String accessToken = "";
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        //getString(R.string.app_name);
        String appName = "appName";
        // GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(null, Arrays.asList(SheetsScopes.SPREADSHEETS));
        //Sheets.Spreadsheets.Get result = new Sheets.Spreadsheets.Get(spreadsheetId);
        Sheets sheets = new Sheets.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).setApplicationName(appName).build();
        try {
            ValueRange result = sheets.spreadsheets().values().get(spreadsheetId, "A2:A10").execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //BatchGetValuesResponse result = service.spreadsheets().values().batchGet(spreadsheetId)
        //        .setRanges(ranges).execute();
        //System.out.printf("%d ranges retrieved.", result.getValueRanges().size());
    }

    @Test
    public void simpleTest3() {
        //Sheets.Spreadsheets.Get result = new Sheets.Spreadsheets.Get("1WStECPaEaNYCqpj_GrwSdqF9ppZ2YyzdtDPB6IF13bI");
    }

    @Test
    public void simpleTest2() {
        String ref4 = "https://docs.google.com/spreadsheets/d/1WStECPaEaNYCqpj_GrwSdqF9ppZ2YyzdtDPB6IF13bI/Dictionary/export?format=csv";
        String ref2 = "https://docs.google.com/spreadsheets/d/1WStECPaEaNYCqpj_GrwSdqF9ppZ2YyzdtDPB6IF13bI/export?format=csv";
        String ref = "https://docs.google.com/spreadsheets/d/1WStECPaEaNYCqpj_GrwSdqF9ppZ2YyzdtDPB6IF13bI/edit?usp=sharing/export?format=csv";
        String ref3 = "https://docs.google.com/spreadsheets/d/1WStECPaEaNYCqpj_GrwSdqF9ppZ2YyzdtDPB6IF13bI/edit#gid=1514438211/export?format=csv";
        URL oracle = null;
        try {
            oracle = new URL(ref);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (oracle == null) {
            return;
        }
        BufferedReader in = null;
        String result = "";
        try {
            URLConnection yc = oracle.openConnection();
            in = new BufferedReader(new InputStreamReader(
                    yc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result += inputLine + "\n";
                Log.d("Memorizer app", inputLine);
            }
        } catch (Exception e) {
            Log.d("Memorizer error", e.toString());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e1) {
                    Log.d("Memorizer error", e1.toString());
                }
            }
        }
    }
}