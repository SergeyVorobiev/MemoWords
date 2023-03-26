package com.vsv.utils.merger;

import android.text.Html;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.vsv.db.entities.Note;

import java.util.ArrayList;

public class NoteMerger extends SimpleMerger<Note> {

    private final long notebookId;

    private final MergeCollection<Note> mergeCollection = new MergeCollection<>();

    public NoteMerger(@NonNull ArrayList<Note> olds, @NonNull ArrayList<Note> news, long notebookId) {
        super(olds, news);
        this.notebookId = notebookId;
    }

    @Override
    public boolean rightUnique(@NonNull Note item, int index) {
        item.setNotebookId(notebookId);
        mergeCollection.uniqueRights.add(item);
        return true;
    }

    @Override
    public boolean leftUnique(@NonNull Note item, int index) {
        mergeCollection.uniqueLefts.add(item);
        return true;
    }

    @Override
    public boolean equal(@NonNull Note updatedItem, @NonNull Note newItem, int leftIndex, int rightIndex) {
        updatedItem.setNumber(newItem.getNumber());
        updatedItem.setName(newItem.getName());
        updatedItem.setContent(newItem.getContent());
        mergeCollection.equal.add(updatedItem);
        return true;
    }

    public MergeCollection<Note> merge() {
        compare();
        return mergeCollection;
    }

    @Override
    public int compare(Note left, Note right) {
        if (left == null && right == null) {
            return 0;
        } else if (left != null && right != null) {
            String leftName = Html.fromHtml(left.getName(), HtmlCompat.FROM_HTML_MODE_COMPACT).toString();
            String rightName = Html.fromHtml(right.getName(), HtmlCompat.FROM_HTML_MODE_COMPACT).toString();
            return leftName.compareTo(rightName);
        } else if (left != null) {
            return 1;
        } else {
            return -1;
        }
    }
}
