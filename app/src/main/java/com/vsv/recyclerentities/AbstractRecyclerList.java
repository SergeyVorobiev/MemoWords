package com.vsv.recyclerentities;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.AbstractAdapter;
import com.vsv.removeitems.AbstractRemover;
import com.vsv.statics.GlobalExecutors;
import com.vsv.statics.WeakContext;

import java.util.concurrent.Future;

public abstract class AbstractRecyclerList<T extends AbstractRemover, E extends AbstractAdapter<?>> {

    private RecyclerView recyclerView;

    protected E adapter;

    protected T remover;

    public final int poolSize;

    protected AbstractRecyclerList(int poolSize) {
        this.poolSize = poolSize;
    }

    @MainThread
    protected final Future<? extends AbstractRecyclerList<T, E>> build() {
        return GlobalExecutors.viewsExecutor.submit(() -> {
            recyclerView = (RecyclerView) View.inflate(WeakContext.getContext(), R.layout.main_recycler, null);
            adapter = buildAdapter(recyclerView);
            remover = buildRemover(recyclerView);
            recyclerView.setAdapter(adapter);
            RecyclerView.RecycledViewPool pool = recyclerView.getRecycledViewPool();
            pool.setMaxRecycledViews(0, poolSize);
            for (int i = 0; i < poolSize; i++) {
                pool.putRecycledView(adapter.createViewHolder(recyclerView, 0));
            }
            return this;
        });
    }

    public abstract T buildRemover(RecyclerView recyclerView);

    public abstract E buildAdapter(RecyclerView recyclerView);

    @MainThread
    public final void clearPool() {
        recyclerView.getRecycledViewPool().clear();
    }

    @MainThread
    @NonNull
    public final E getAdapter() {
        return adapter;
    }

    @MainThread
    @NonNull
    public final RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @MainThread
    public final void detach() {
        ViewGroup parent = (ViewGroup) recyclerView.getParent();
        if (parent != null) {
            parent.removeView(recyclerView);
        }
    }

    @MainThread
    public final void attachTo(ViewGroup container) {
        detach();
        remover.bindRecycleView(recyclerView);
        container.addView(recyclerView, new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @MainThread
    public final void detachAndClear() {
        detach();
        clearData();
    }

    @MainThread
    public final void clearData() {
        adapter.clearData();
        remover.clearData();
    }
}
