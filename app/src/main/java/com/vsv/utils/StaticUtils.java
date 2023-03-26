package com.vsv.utils;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import com.vsv.db.entities.Settings;
import com.vsv.memorizer.MainActivity;
import com.vsv.memorizer.R;
import com.vsv.memorizer.fragments.GameFragment;
import com.vsv.memorizer.fragments.RepetitionFragment;
import com.vsv.memorizer.fragments.TrainFragment;
import com.vsv.models.MainModel;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class StaticUtils {

    public static final Random random = new Random();

    public static void updateSettings() {
        Settings settings = GlobalData.getSettings();
        if (settings.id != -1) {
            WeakContext.getMainActivity().getMainModel().update(settings);
        }
    }

    public static boolean isNightMode() {
        int nightModeFlags = WeakContext.getContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        return Configuration.UI_MODE_NIGHT_YES == nightModeFlags;
    }

    public static boolean isFragmentVisible(Class<?> needClazz) {
        Fragment host = WeakContext.getMainActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (host != null) {
            ArrayList<Fragment> fragments = (ArrayList<Fragment>) host.getChildFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                Class<?> clazz = fragment.getClass();
                if (clazz == needClazz && fragment.isVisible()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getColor(int resColor) {
        return WeakContext.getContext().getResources().getColor(resColor, WeakContext.getContext().getTheme());
    }

    public static int getRandomInt(@NonNull int[] array) {
        return array[random.nextInt(array.length)];
    }

    public static boolean isMainActivityResumed() {
        MainActivity activity = WeakContext.getMainActivityOrNull();
        return activity != null && activity.isResumed;
    }

    public static int getRandom(int minValue, int maxValue) {
        if (minValue == maxValue) {
            return minValue;
        }
        return random.nextInt(maxValue - minValue) + minValue;
    }

    public static Bitmap getBitmap(int resource, int width, int height, Bitmap.Config config) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = width;
        options.inScaled = false;
        options.outHeight = height;
        options.outConfig = config;
        options.inMutable = false;
        Bitmap bitmap = BitmapFactory.decodeResource(WeakContext.getContext().getResources(), resource, options);
        if (width > 0 && height > 0) {
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        }
        return bitmap;
    }

    public static Bitmap getBitmap(int resource, Bitmap.Config config) {
        return getBitmap(resource, 0, 0, config);
    }

    @NonNull
    public static MainModel getModel() {
        return WeakContext.getMainActivity().getMainModel();
    }

    @Nullable
    public static MainModel getModelOrNull() {
        MainActivity activity = WeakContext.getMainActivityOrNull();
        return activity == null ? null : activity.getMainModel();
    }

    @NonNull
    public static Drawable getDrawable(int drawable) {
        return Objects.requireNonNull(AppCompatResources.getDrawable(WeakContext.getContext(), drawable));
    }

    @NonNull // It creates new object per method invoking.
    public static DisplayMetrics getScreenMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WeakContext.getMainActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public static float convertDpToPixels(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, value, WeakContext.getContext().getResources().getDisplayMetrics());
    }

    public static float getDimension(int id) {
        return WeakContext.getContext().getResources().getDimension(id);
    }

    public static int getDimensionInPixels(int id) {
        return (int) convertDpToPixels(getDimension(id));
    }

    @NonNull
    public static Typeface getFont(int font) {
        return Objects.requireNonNull(ResourcesCompat.getFont(WeakContext.getContext(), font));
    }

    @NonNull
    public static View inflate(int layoutId, @NonNull ViewGroup parent) {
        return LayoutInflater.from(WeakContext.getContext()).inflate(layoutId, parent, false);
    }

    @NonNull
    public static View inflate(int layoutId) {
        return View.inflate(WeakContext.getContext(), layoutId, null);
    }

    @AnyThread
    public static void navigateSafePost(@Nullable NavController controller, int action, @Nullable Bundle bundle) {
        new Handler(Looper.getMainLooper()).post(() -> navigateSafe(controller, action, bundle));
    }

    @MainThread
    public static boolean navigateSafe(@Nullable NavController controller, int action, @Nullable Bundle bundle) {
        if (controller == null) {
            return false;
        }
        try {
            controller.navigate(action, bundle);
        } catch (Throwable e) {
            Log.e("Navigate", e.toString());
            return false;
        }
        return true;
    }

    @MainThread
    public static boolean navigateSafe(int action, @Nullable Bundle bundle) {
        return navigateSafe(WeakContext.getMainActivity().getNavController(), action, bundle);
    }

    @MainThread
    public static boolean navigateSafe(int action) {
        return navigateSafe(WeakContext.getMainActivity().getNavController(), action, null);
    }

    @NonNull
    public static String getString(int id) {
        return WeakContext.getContext().getString(id);
    }

    @NonNull
    public static String getString(int id, Object... objects) {
        return WeakContext.getContext().getString(id, objects);
    }
}
