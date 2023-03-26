package com.vsv.removeitems;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.Dictionary;
import com.vsv.dialogs.DeleteShelfDialog;
import com.vsv.dialogs.SendShelfDialog;
import com.vsv.dialogs.SingleWindow;
import com.vsv.dialogs.WaitDialog;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerShelvesAdapter;
import com.vsv.statics.GlobalData;
import com.vsv.statics.GlobalExecutors;
import com.vsv.toasts.Toasts;
import com.vsv.utils.AppException;
import com.vsv.utils.GoogleTasksExceptionHandler;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ShelfRemover extends AbstractRemover {

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return super.isItemViewSwipeEnabled();
        }

        @Override
        public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getBindingAdapterPosition();
            if (!canBeSent) {
                assert viewHolder.getBindingAdapter() != null;
                viewHolder.getBindingAdapter().notifyItemChanged(position);
                return;
            }
            final RecyclerShelvesAdapter adapter = (RecyclerShelvesAdapter) viewHolder.getBindingAdapter();
            assert adapter != null;
            if (SingleWindow.isShownToast()) {
                adapter.notifyItemChanged(position);
                return;
            }
            if (direction == ItemTouchHelper.LEFT) {
                RecyclerShelvesAdapter.CardHolder holder = (RecyclerShelvesAdapter.CardHolder) viewHolder;
                String name = holder.getName().getText().toString();
                DeleteShelfDialog dialog = new DeleteShelfDialog(name);
                dialog.setOnDeleteListener(() -> StaticUtils.getModel().getShelvesRepository().delete(adapter.getItem(position)));
                dialog.setOnCancelListener(() -> adapter.notifyItemChanged(position));
                dialog.show();
            } else if (GlobalData.account != null) {
                adapter.notifyItemChanged(position);
                BackgroundTask<ArrayList<Dictionary>> getDictionaries = new BackgroundTask<>(10, () -> {
                    ArrayList<Dictionary> dictionaries = StaticUtils.getModel().getDictionariesRepository().getAllDictionaries(adapter.getItem(position).getId()).get(10, TimeUnit.SECONDS);
                    if (dictionaries == null || dictionaries.isEmpty()) {
                        throw new AppException(R.string.toast_cannot_get_dictionaries);
                    }
                    return dictionaries;
                });
                getDictionaries.setRunMainThreadOnSuccess(dictionaries -> {
                    // Reset the flag before the dialog close to allow showing next dialog.
                    SingleWindow.setShown(false);
                    SendShelfDialog dialog = new SendShelfDialog(dictionaries);
                    dialog.show();
                });
                getDictionaries.setRunMainThreadOnFail(e -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(e)));
                getDictionaries.buildWaitDialog().showOver();
            } else {
                Toasts.needLogin();
                adapter.notifyItemChanged(position);
            }
        }
    };

    private final boolean canBeSent;

    public ShelfRemover(boolean canBeSent) {
        this.canBeSent = canBeSent;
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
    }

    public void clearData() {
        this.itemTouchHelper.attachToRecyclerView(null);
    }
}
