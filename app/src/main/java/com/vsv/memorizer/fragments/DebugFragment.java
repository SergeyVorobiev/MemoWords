package com.vsv.memorizer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vsv.memorizer.R;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;

public class DebugFragment extends Fragment {

    private TextView crashMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debug, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WeakContext.getMainActivity().hideTabs();
        view.findViewById(R.id.clearCrash).setOnClickListener(this::tapClear);
        crashMessage = view.findViewById(R.id.crashMessage);
        String crash = GlobalData.getSettings().lastCrashException;
        if (crash != null) {
            crashMessage.setText(crash);
        }
        onCreateOptionsMenu();
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.setTitle(R.string.debug_screen_header);
        OverflowMenu.hideMore();
        OverflowMenu.hideSearch();
        OverflowMenu.hideAccount();
        OverflowMenu.setupBackButton(this::navigateToShelfFragment);
    }

    private void tapClear(@Nullable View view) {
        crashMessage.setText(R.string.no_crashes);
        GlobalData.getSettings().lastCrashException = null;
        StaticUtils.getModel().update(GlobalData.getSettings());
    }

    private void navigateToShelfFragment(@Nullable View view) {
        StaticUtils.navigateSafe(R.id.action_Debug_to_Shelves);
    }
}
