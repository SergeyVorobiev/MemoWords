package com.vsv.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("ALL")
public class DBDictionary extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "Dictionary.db";

    public final String TABLE_NAME;

    public static final String ID = "id";

    public static final String LEFT_VALUE = "leftValue";

    public static final String RIGHT_VALUE = "rightValue";

    public static final String STATUS = "status";

    private HashMap hp;

    private final String CREATE_ENTRIES;

    private final String DELETE_ENTRIES;

    public DBDictionary(Context context, String tableName) {
        super(context, DATABASE_NAME, null, 1);
        TABLE_NAME = tableName;
        CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (" +
                ID + "INTEGER PRIMARY KEY," +
                LEFT_VALUE + " TEXT," +
                RIGHT_VALUE + " TEXT," +
                STATUS + " INTEGER)";
        DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Returns row id, if it equals to -1 then IO Exception has been happen.
    public int insertRow(String leftValue, String rightValue, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LEFT_VALUE, leftValue);
        contentValues.put(RIGHT_VALUE, rightValue);
        contentValues.put(STATUS, status);
        return (int) db.insert(TABLE_NAME, null, contentValues);
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from contacts where id=" + id + "", null);
    }

    public int numberOfRows() {
        return (int) DatabaseUtils.queryNumEntries(this.getReadableDatabase(), TABLE_NAME);
    }

    public boolean updateContact(Long id, String name, String phone, String email, String street, String place) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("street", street);
        contentValues.put("place", place);
        db.update("contacts", contentValues, "id = ? ", new String[]{Long.toString(id)});
        db.close();
        return true;
    }

    public Integer deleteContact(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME,
                "id = ? ",
                new String[]{Integer.toString(id)});
    }

    public ArrayList<String> getAllData() {
        ArrayList<String> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from contacts", null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            int index = res.getColumnIndex(LEFT_VALUE);
            if (index >= 0) {
                array_list.add(res.getString(index));
                res.moveToNext();
            }
        }
        res.close();
        return array_list;
    }
}
