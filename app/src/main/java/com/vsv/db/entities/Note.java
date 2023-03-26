package com.vsv.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "note", foreignKeys = {@ForeignKey(entity = Notebook.class,
        parentColumns = "id",
        childColumns = "notebookId",
        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = {"notebookId"})})
public class Note {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "number")
    private int number;

    private long notebookId;

    @Ignore
    transient
    public boolean isOpen;

    public Note(long id, long notebookId, String name, String content, int number) {
        this.id = id;
        this.notebookId = notebookId;
        this.name = name;
        this.content = content;
        this.number = number;
    }

    @Ignore
    public Note(long notebookId, String name, String content, int number) {
        this.notebookId = notebookId;
        this.name = name;
        this.content = content;
        this.number = number;
    }

    // This copy method will note copy id.
    public Note copy() {
        return new Note(this.notebookId, this.name, this.content, this.number);

    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setNotebookId(long notebookId) {
        this.notebookId = notebookId;
    }

    public long getNotebookId() {
        return this.notebookId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
