package com.vsv.memorizer.adapters;

import androidx.recyclerview.widget.RecyclerView;

public abstract class AbstractAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    public abstract void clearData();
}
