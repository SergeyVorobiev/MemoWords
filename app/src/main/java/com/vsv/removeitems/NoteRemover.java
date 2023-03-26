package com.vsv.removeitems;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.dialogs.DeleteNoteDialog;
import com.vsv.dialogs.SingleWindow;
import com.vsv.memorizer.adapters.RecyclerNotesAdapter;
import com.vsv.utils.StaticUtils;

public class NoteRemover extends AbstractRemover {

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getBindingAdapterPosition();
            final RecyclerNotesAdapter adapter = (RecyclerNotesAdapter) viewHolder.getBindingAdapter();
            assert adapter != null;
            if (SingleWindow.isShownToast()) {
                adapter.notifyItemChanged(position);
                return;
            }
            RecyclerNotesAdapter.CardHolder holder = (RecyclerNotesAdapter.CardHolder) viewHolder;
            String name = holder.header.getText().toString();
            DeleteNoteDialog dialog = new DeleteNoteDialog(name);
            dialog.setOnDeleteListener(() -> StaticUtils.getModel().getNotesRepository().deleteAndDecrementNotesCount(adapter.getItem(position)));
            dialog.setOnCancelListener(() -> adapter.notifyItemChanged(position));
            dialog.show();
        }
    };

    public NoteRemover() {
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
    }

    public void clearData() {
        this.itemTouchHelper.attachToRecyclerView(null);
    }
}
