package com.vsv.dialogs.entities;

import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Shelf;

public class FoundWordItem {

    public Dictionary dictionary;

    public long id;

    public String leftValue;

    public String percentage;

    public String kind;

    public String rightValue;

    public Shelf shelf;

    public String pathName;

    public FoundWordItem copy() {
        FoundWordItem item = new FoundWordItem();
        item.dictionary = dictionary;
        item.shelf = shelf;
        item.id = id;
        item.leftValue = leftValue;
        item.rightValue = rightValue;
        item.pathName = pathName;
        item.percentage = percentage;
        item.kind = kind;
        return item;
    }
}
