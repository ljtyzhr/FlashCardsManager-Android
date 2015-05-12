package org.random_access.flashcardsmanager.storage.contracts;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.random_access.flashcardsmanager.storage.FlashCardsProvider;

/**
 * Project: FlashCards Manager for Android
 * Date: 09.05.15
 * Author: Monika Schrenk
 * E-Mail: software@random-access.org
 */
public class ProjectContract {

    private static final String TAG = ProjectContract.class.getSimpleName();
    public static final Uri  CONTENT_URI = Uri.parse("content://" + FlashCardsProvider.AUTHORITY + "/" + ProjectContract.ProjectEntry.TABLE_NAME);

    // prevent instantiation
    private ProjectContract() {}

    /**
     * Table name: _TBL_PROJECTS
     * <br>
     * Columns:
     * <ul>
     *     <li>_ID: int, PK, NN, AI</li>
     *     <li>_TITLE: text</li>
     *     <li>_STACKS: int</li>
     * </ul>
     */
    public static abstract class ProjectEntry implements BaseColumns {

        public static final String TABLE_NAME = "_TBL_PROJECTS";

        public static final String COLUMN_NAME_TITLE = "_TITLE";
        public static final String COLUMN_NAME_STACKS = "_STACKS";

        public static final String COLUMN_NAME_ID_FULLNAME = TABLE_NAME + "." + _ID;
        public static final String COLUMN_NAME_TITLE_FULLNAME = TABLE_NAME + "." + COLUMN_NAME_TITLE;
        public static final String COLUMN_NAMEß_STACKS_FULLNAME = TABLE_NAME + "." + COLUMN_NAME_STACKS;
    }

    private static final String DATABASE_CREATE = "create table if not exists "
            + ProjectEntry.TABLE_NAME
            + "("
            + ProjectEntry._ID + " integer primary key autoincrement, "
            + ProjectEntry.COLUMN_NAME_TITLE + " text not null, "
            + ProjectEntry.COLUMN_NAME_STACKS + " integer not null"
            + ");";

    public static void onCreate (SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        Log.d(TAG, DATABASE_CREATE);
    }

    public static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion) {
        // add upgrade procedure if necessary
    }

}
