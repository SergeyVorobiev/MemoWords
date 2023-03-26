package com.vsv.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.vsv.db.dao.DictionaryDao;
import com.vsv.db.dao.DictionarySamplesDao;
import com.vsv.db.dao.NoteDao;
import com.vsv.db.dao.NotebookDao;
import com.vsv.db.dao.SampleDao;
import com.vsv.db.dao.ScoreDayDao;
import com.vsv.db.dao.SettingsDao;
import com.vsv.db.dao.ShelfDao;
import com.vsv.db.dao.SpreadsheetDao;
import com.vsv.db.dao.TrackerDao;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Note;
import com.vsv.db.entities.Notebook;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.ScoreDay;
import com.vsv.db.entities.Settings;
import com.vsv.db.entities.Shelf;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.db.entities.SpreadSheetTabName;
import com.vsv.db.entities.Tracker;

@Database(entities = {Dictionary.class, Sample.class, Shelf.class, SpreadSheetInfo.class,
        SpreadSheetTabName.class, Settings.class, Note.class, Notebook.class, Tracker.class, ScoreDay.class},
        version = 47, exportSchema = false)
public abstract class DictionaryDB extends RoomDatabase {

    public static final String DB_NAME = "dictionaries";

    public abstract DictionaryDao dictionaryDao();

    public abstract TrackerDao trackerDao();

    public abstract ScoreDayDao scoreDayDao();

    public abstract SampleDao sampleDao();

    public abstract SettingsDao settingsDao();

    public abstract ShelfDao shelfDao();

    public abstract NoteDao noteDao();

    public abstract NotebookDao notebookDao();

    public abstract SpreadsheetDao spreadSheetDao();

    public abstract DictionarySamplesDao dictionaryWithSamplesDao();

    private static volatile DictionaryDB INSTANCE;

    public static DictionaryDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DictionaryDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), DictionaryDB.class,
                            DB_NAME).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                            MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                            MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                            MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
                            MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
                            MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25,
                            MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29,
                            MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32, MIGRATION_32_33,
                            MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36, MIGRATION_36_37,
                            MIGRATION_37_38, MIGRATION_38_39, MIGRATION_39_40, MIGRATION_40_41,
                            MIGRATION_41_42, MIGRATION_42_43, MIGRATION_43_44, MIGRATION_44_45,
                            MIGRATION_45_46, MIGRATION_46_47).build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {

        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create the new table
            database.execSQL(
                    "CREATE TABLE shelf (id INTEGER NOT NULL, name TEXT, PRIMARY KEY(id))");
            // database.execSQL(
            //        "INSERT INTO users_new (userid, username, last_update) SELECT userid, username, last_update FROM users");
            // Remove the old table
            // database.execSQL("DROP TABLE users");
            // Change the table name to the correct one
            // database.execSQL("ALTER TABLE users_new RENAME TO users");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            // Create new dictionary table
            database.execSQL(
                    "CREATE TABLE dictionary_new (id INTEGER NOT NULL" +
                            ", shelfId INTEGER NOT NULL" +
                            ", name TEXT" +
                            ", PRIMARY KEY(id)" +
                            ", FOREIGN KEY(shelfId) REFERENCES shelf(id) ON DELETE CASCADE)");

            // Create a default shelf
            database.execSQL("INSERT INTO shelf (id, name) VALUES (1, 'first')");

            // Move dictionary entities into new table
            database.execSQL("INSERT INTO dictionary_new (id, shelfId, name) SELECT id, 1, name FROM dictionary");

            // Remove the old table
            database.execSQL("DROP TABLE dictionary");

            // Change the table name to the correct one
            database.execSQL("ALTER TABLE dictionary_new RENAME TO dictionary");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE sample ADD COLUMN left_answered_count INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE sample ADD COLUMN right_answered_count INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE sample ADD COLUMN type TEXT";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE spreadSheetInfo (id INTEGER NOT NULL, spreadSheetId TEXT, name TEXT, PRIMARY KEY(id))");
            database.execSQL(
                    "CREATE TABLE spreadSheetTabName (id INTEGER NOT NULL, spreadSheetInfoId INTEGER NOT NULL, name TEXT, PRIMARY KEY(id), FOREIGN KEY(spreadSheetInfoId) REFERENCES spreadSheetInfo(id) ON DELETE CASCADE)");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE spreadSheetInfo");
            database.execSQL("DROP TABLE spreadSheetTabName");
            database.execSQL(
                    "CREATE TABLE spreadSheetInfo (id INTEGER NOT NULL, spreadSheetId TEXT, name TEXT, PRIMARY KEY(id))");
            database.execSQL(
                    "CREATE TABLE spreadSheetTabName (id INTEGER NOT NULL, spreadSheetInfoId INTEGER NOT NULL, name TEXT, PRIMARY KEY(id), FOREIGN KEY(spreadSheetInfoId) REFERENCES spreadSheetInfo(id) ON DELETE CASCADE)");
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE sample ADD COLUMN example TEXT";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_8_9 = new Migration(8, 9) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE dictionary ADD COLUMN left_locale_language TEXT DEFAULT ''";
            database.execSQL(query);
            query = "ALTER TABLE dictionary ADD COLUMN right_locale_language TEXT DEFAULT ''";
            database.execSQL(query);
            query = "ALTER TABLE dictionary ADD COLUMN left_locale_country TEXT DEFAULT ''";
            database.execSQL(query);
            query = "ALTER TABLE dictionary ADD COLUMN right_locale_country TEXT DEFAULT ''";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_9_10 = new Migration(9, 10) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE settings (id INTEGER NOT NULL, " +
                            "sortShelf INTEGER DEFAULT 0 NOT NULL, " +
                            "sortSheets INTEGER DEFAULT 0 NOT NULL, " +
                            "autoLogout INTEGER DEFAULT 1 NOT NULL, " +
                            "PRIMARY KEY(id))");

            // Create a default settings
            database.execSQL("INSERT INTO settings (id, sortShelf, sortSheets, autoLogout) VALUES (1, 0, 0, 1)");
        }
    };

    static final Migration MIGRATION_10_11 = new Migration(10, 11) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE settings ADD COLUMN sortTabs INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_11_12 = new Migration(11, 12) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE dictionary ADD COLUMN hide_remembered INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE dictionary ADD COLUMN sorted_type INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE dictionary ADD COLUMN passed_percentage INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_12_13 = new Migration(12, 13) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE dictionary RENAME COLUMN left_locale_language TO left_locale_abb";
            database.execSQL(query);
            query = "ALTER TABLE dictionary RENAME COLUMN right_locale_language TO right_locale_abb";
            database.execSQL(query);
            query = "ALTER TABLE dictionary RENAME COLUMN left_locale_country TO free_field_1";
            database.execSQL(query);
            query = "ALTER TABLE dictionary RENAME COLUMN right_locale_country TO free_field_2";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_13_14 = new Migration(13, 14) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE INDEX index_dictionary_shelfId ON dictionary (shelfId)");
            database.execSQL("CREATE INDEX index_spreadSheetTabName_spreadSheetInfoId ON spreadSheetTabName (spreadSheetInfoId)");
        }
    };

    static final Migration MIGRATION_14_15 = new Migration(14, 15) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE sample_new (id INTEGER NOT NULL" +
                            ", dictionaryId INTEGER NOT NULL" +
                            ", left_value TEXT NOT NULL DEFAULT ''" +
                            ", right_value TEXT NOT NULL DEFAULT ''" +
                            ", left_answered INTEGER NOT NULL DEFAULT 0" +
                            ", right_answered INTEGER NOT NULL DEFAULT 0" +
                            ", left_answered_count INTEGER NOT NULL DEFAULT 0" +
                            ", right_answered_count INTEGER NOT NULL DEFAULT 0" +
                            ", type TEXT NOT NULL DEFAULT ''" +
                            ", example TEXT NOT NULL DEFAULT ''" +
                            ", PRIMARY KEY(id)" +
                            ", FOREIGN KEY(dictionaryId) REFERENCES dictionary(id) ON DELETE CASCADE)"
            );

            database.execSQL("INSERT INTO sample_new (id, dictionaryId, left_value, right_value, left_answered, right_answered, left_answered_count, right_answered_count, type, example) " +
                    "SELECT id, dictionaryId, left_value, right_value, left_answered, right_answered, left_answered_count, right_answered_count, IFNULL(type, '') type, IFNULL(example, '') example FROM sample");

            database.execSQL("DROP TABLE sample");

            database.execSQL("ALTER TABLE sample_new RENAME TO sample");

            database.execSQL("CREATE INDEX index_sample_dictionaryId ON sample (dictionaryId)");
        }
    };

    static final Migration MIGRATION_15_16 = new Migration(15, 16) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE shelf ADD COLUMN sorted INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE shelf ADD COLUMN hideRemembered INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_16_17 = new Migration(16, 17) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE sample ADD COLUMN answered_date INTEGER";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_17_18 = new Migration(17, 18) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE dictionary ADD COLUMN can_copy INTEGER DEFAULT 1 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_18_19 = new Migration(18, 19) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE notebook (id INTEGER NOT NULL, sorted INTEGER NOT NULL DEFAULT 0, name TEXT, PRIMARY KEY(id))");
            database.execSQL(
                    "CREATE TABLE note (id INTEGER NOT NULL, notebookId INTEGER NOT NULL, name TEXT, content TEXT, PRIMARY KEY(id), FOREIGN KEY(notebookId) REFERENCES notebook(id) ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX index_note_notebookId ON note (notebookId)");
        }
    };

    static final Migration MIGRATION_19_20 = new Migration(19, 20) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE settings ADD COLUMN sortNotebooks INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_20_21 = new Migration(20, 21) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE dictionary RENAME COLUMN free_field_1 TO spreadsheet_id";
            database.execSQL(query);
            query = "ALTER TABLE dictionary RENAME COLUMN free_field_2 TO sheet_name";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_21_22 = new Migration(21, 22) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE notebook ADD COLUMN spreadsheet_id TEXT";
            database.execSQL(query);
            query = "ALTER TABLE notebook ADD COLUMN sheet_name TEXT";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_22_23 = new Migration(22, 23) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE note ADD COLUMN number INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_23_24 = new Migration(23, 24) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE notebook ADD COLUMN can_copy INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_24_25 = new Migration(24, 25) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE dictionary RENAME COLUMN passed_percentage TO count";
            database.execSQL(query);
            query = "ALTER TABLE dictionary ADD COLUMN passed_percentage FLOAT DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE dictionary ADD COLUMN remembered_count INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_25_26 = new Migration(25, 26) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE sample RENAME COLUMN left_answered_count TO left_percentage";
            database.execSQL(query);
            query = "ALTER TABLE sample RENAME COLUMN right_answered_count TO right_percentage";
            database.execSQL(query);
            query = "ALTER TABLE sample ADD COLUMN last_correct INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE sample ADD COLUMN correct_series INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_26_27 = new Migration(26, 27) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE shelf ADD COLUMN fontDictTitleIndex INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE settings ADD COLUMN fontDictTitleIndex INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_27_28 = new Migration(27, 28) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE settings ADD COLUMN fontSpreadsheetTitleIndex INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE settings ADD COLUMN fontNotebookTitleIndex INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_28_29 = new Migration(28, 29) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE spreadSheetInfo ADD COLUMN type INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_29_30 = new Migration(29, 30) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE notebook ADD COLUMN notes_count INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE notebook ADD COLUMN fontNotesTitleIndex INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_30_31 = new Migration(30, 31) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE settings ADD COLUMN startIndex INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_31_32 = new Migration(31, 32) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE dictionary ADD COLUMN update_check INTEGER";
            database.execSQL(query);
            query = "ALTER TABLE dictionary ADD COLUMN data_date INTEGER";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_32_33 = new Migration(32, 33) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE dictionary ADD COLUMN need_update INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_33_34 = new Migration(33, 34) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE dictionary ADD COLUMN column_id INTEGER DEFAULT -1 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_34_35 = new Migration(34, 35) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE notebook ADD COLUMN update_check INTEGER";
            database.execSQL(query);

            query = "ALTER TABLE notebook ADD COLUMN data_date INTEGER";
            database.execSQL(query);

            query = "ALTER TABLE notebook ADD COLUMN need_update INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);

            query = "ALTER TABLE notebook ADD COLUMN sheet_id INTEGER DEFAULT -1 NOT NULL";
            database.execSQL(query);

            query = "ALTER TABLE spreadSheetTabName ADD COLUMN sheetId INTEGER DEFAULT -1 NOT NULL";
            database.execSQL(query);

            query = "ALTER TABLE dictionary RENAME COLUMN column_id TO sheet_id";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_35_36 = new Migration(35, 36) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE tracker (id INTEGER NOT NULL" +
                            ", dictionaryId INTEGER NOT NULL" +
                            ", progress INTEGER NOT NULL" +
                            ", timestamp INTEGER NOT NULL" +
                            ", PRIMARY KEY(id)" +
                            ", FOREIGN KEY(dictionaryId) REFERENCES dictionary(id) ON DELETE CASCADE)"
            );
            database.execSQL("CREATE INDEX index_tracker_dictionaryId ON tracker (dictionaryId)");

            database.execSQL("ALTER TABLE settings ADD COLUMN todayScore INTEGER NOT NULL DEFAULT 0");

            database.execSQL("ALTER TABLE settings ADD COLUMN timeStampForScore INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_36_37 = new Migration(36, 37) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };

    static final Migration MIGRATION_37_38 = new Migration(37, 38) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE scoreDay (timestamp INTEGER NOT NULL" +
                            ", score INTEGER NOT NULL" +
                            ", PRIMARY KEY(timestamp))"
            );
            database.execSQL("ALTER TABLE settings ADD COLUMN score INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_38_39 = new Migration(38, 39) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE dictionary ADD COLUMN spreadsheet_name TEXT");
            database.execSQL("ALTER TABLE notebook ADD COLUMN spreadsheet_name TEXT");
        }
    };

    static final Migration MIGRATION_39_40 = new Migration(39, 40) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE settings RENAME COLUMN timeStampForScore TO timestampForTodayScore");
        }
    };

    static final Migration MIGRATION_40_41 = new Migration(40, 41) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE dictionary ADD COLUMN today_score INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE dictionary ADD COLUMN timestamp_for_today_score INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE tracker ADD COLUMN score INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_41_42 = new Migration(41, 42) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE dictionary ADD COLUMN author TEXT");
            database.execSQL("ALTER TABLE notebook ADD COLUMN author TEXT");
        }
    };

    static final Migration MIGRATION_42_43 = new Migration(42, 43) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE settings ADD COLUMN appLocale TEXT");
        }
    };

    static final Migration MIGRATION_43_44 = new Migration(43, 44) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE settings ADD COLUMN mobileInternet INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_44_45 = new Migration(44, 45) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE settings ADD COLUMN trainTimeInSeconds INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_45_46 = new Migration(45, 46) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE sample ADD COLUMN google_translate_left INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
            query = "ALTER TABLE sample ADD COLUMN google_translate_right INTEGER DEFAULT 0 NOT NULL";
            database.execSQL(query);
        }
    };

    static final Migration MIGRATION_46_47 = new Migration(46, 47) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String query = "ALTER TABLE settings ADD COLUMN lastCrashException TEXT";
            database.execSQL(query);
        }
    };
}