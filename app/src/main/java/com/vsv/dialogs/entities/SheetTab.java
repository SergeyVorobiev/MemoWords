package com.vsv.dialogs.entities;

import androidx.annotation.NonNull;

public class SheetTab {

    @NonNull
    private String title;

    private int id;

    private boolean isChecked;

    public SheetTab(@NonNull String title, int id) {
        this.title = title;
        this.id = id;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

}
