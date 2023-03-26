package com.vsv.removeitems;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbstractRemover {

    protected ItemTouchHelper itemTouchHelper;

    public void bindRecycleView(RecyclerView view) {
        this.itemTouchHelper.attachToRecyclerView(view);
    }

    public abstract void clearData();
}
