package com.vsv.memorizer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Dictionary;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerDictionaryMergeAdapter;
import com.vsv.models.MainModel;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;

public class DictionaryMergeFragment extends Fragment {

    private RecyclerDictionaryMergeAdapter adapter;

    private TextView counterView;

    private final MainModel model;

    public DictionaryMergeFragment() {
        model = StaticUtils.getModel();
    }

    private ShelfBundle shelfBundle;

    private DictionaryBundle dictionaryBundle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            shelfBundle = ShelfBundle.fromBundle(savedInstanceState);
            dictionaryBundle = DictionaryBundle.fromBundle(savedInstanceState);
        }
        loadShelfId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dictionaries, container, false);
        view.setBackground(GlobalData.bg_dict);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton fab = view.findViewById(R.id.addDict);
        fab.setVisibility(View.GONE);
        counterView = view.findViewById(R.id.dictCounter);
        TextView shelfNameView = view.findViewById(R.id.shelfName);
        shelfNameView.setText(shelfBundle.name);
        counterView.setText(String.format(getString(R.string.empty_counter), Spec.MAX_DICTIONARIES));
        RecyclerView recycleList = (RecyclerView) View.inflate(WeakContext.getContext(), R.layout.main_recycler, null);
        adapter = new RecyclerDictionaryMergeAdapter(shelfBundle, dictionaryBundle.id);
        recycleList.setAdapter(adapter);
        ViewGroup container = view.findViewById(R.id.dictContainer);
        container.addView(recycleList, new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        observerSetup();
        onCreateOptionsMenu();
    }

    private void loadShelfId() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            shelfBundle = ShelfBundle.fromBundle(bundle);
            dictionaryBundle = DictionaryBundle.fromBundle(bundle);
        }
    }

    @Override
    public void onStart() {
        WeakContext.getMainActivity().hideTabs();
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        shelfBundle.toBundle(bundle);
        dictionaryBundle.toBundle(bundle);
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.setTitle(R.string.dict_merge_fragment_title);
        OverflowMenu.hideAccount();
        OverflowMenu.hideMore();
        OverflowMenu.setupBackButton(this::navigateToDictionariesFragment);
        OverflowMenu.setupSearchView(this::onQueryTextChange, null);
    }

    private void navigateToDictionariesFragment(View view) {
        StaticUtils.navigateSafe(R.id.action_DictionaryMerge_to_Dictionaries, shelfBundle.toNewBundle());
    }

    private void onQueryTextChange(String query) {
        updateCounter(adapter.applyFilter(query));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateCounter(int size) {
        String counter = getResources().getString(R.string.counter);
        counterView.setText(String.format(counter, size, Spec.MAX_DICTIONARIES));
    }

    private void observerSetup() {
        model.getDictionariesRepository().getAllLiveFromShelf(shelfBundle.id).observe(getViewLifecycleOwner(),
                dictionaries -> updateCounter(adapter.setItems((ArrayList<Dictionary>) dictionaries)));
    }
}