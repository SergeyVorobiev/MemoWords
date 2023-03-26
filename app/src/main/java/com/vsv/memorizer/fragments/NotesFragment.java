package com.vsv.memorizer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vsv.bundle.entities.NotebookBundle;
import com.vsv.db.entities.Note;
import com.vsv.dialogs.NewNoteDialog;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerNotesAdapter;
import com.vsv.models.MainModel;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.recyclerentities.NotesRecycleList;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;

public class NotesFragment extends Fragment {

    private final MainModel model;

    private RecyclerNotesAdapter adapter;

    private TextView counterView;

    private NotesRecycleList recycleList;

    public NotesFragment() {
        model = StaticUtils.getModel();
    }

    private NotebookBundle notebookBundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notebookBundle = NotebookBundle.fromBundleOrNull(this.getArguments());
        if (notebookBundle == null) {
            notebookBundle = NotebookBundle.fromBundle(savedInstanceState);
        }
        WeakContext.getMainActivity().hideTabs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton fab = view.findViewById(R.id.addNotes);
        fab.setOnClickListener(this::showNewNoteDialog);
        if (notebookBundle.canCopy) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        counterView = view.findViewById(R.id.notesCounter);
        TextView nameView = view.findViewById(R.id.name);
        nameView.setText(notebookBundle.name);
        counterView.setText(String.format(getString(R.string.empty_counter), Spec.MAX_NOTES));
        recycleList = NotesRecycleList.getList();
        recycleList.attachTo(view.findViewById(R.id.notesContainer));
        adapter = recycleList.getAdapter();
        adapter.setup(notebookBundle.canCopy);
        adapter.setCanEditItem(notebookBundle.canCopy);
        observerSetup();
        onCreateOptionsMenu();
    }

    @Override
    public void onStart() {
        WeakContext.getMainActivity().hideTabs();
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        notebookBundle.intoBundle(bundle);
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.setTitle(R.string.notes_title);
        OverflowMenu.setupBackButton(this::navigateToNotebooksFragment);
        OverflowMenu.hideAccount();
        OverflowMenu.hideMore();
        OverflowMenu.setupSearchView(this::onQueryTextChange, GlobalData.noteSearchQuery);
    }

    private void navigateToNotebooksFragment(@Nullable View view) {
        StaticUtils.navigateSafe(R.id.action_Notes_to_Notebooks);
    }

    private void onQueryTextChange(String query) {
        updateCounter(adapter.applyFilter(query));
    }

    @Override
    public void onDetach() {
        recycleList.detach();
        recycleList.clearData();
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateCounter(int size) {
        String counter = getResources().getString(R.string.counter);
        counterView.setText(String.format(counter, size, Spec.MAX_NOTES));
    }

    private void showNewNoteDialog(@Nullable View view) {
        int count = adapter.getAllCount();
        if (count >= Spec.MAX_NOTES) {
            Toasts.maxItemsMessage(StaticUtils.getString(R.string.notes_item_name));
        } else {
            NewNoteDialog dialog = new NewNoteDialog(count);
            dialog.setNoteCreateListener((header, content, number) -> {
                model.getNotesRepository().insert(notebookBundle.id, header, content, number);
                model.getNotebooksRepository().incrementNotesCount(notebookBundle.id);
            });
            dialog.show();
        }
    }

    private void observerSetup() {
        model.getNotesRepository().getAllLiveFromNotebook(notebookBundle.id).observe(getViewLifecycleOwner(),
                notes -> updateCounter(adapter.setItems((ArrayList<Note>) notes)));
    }
}