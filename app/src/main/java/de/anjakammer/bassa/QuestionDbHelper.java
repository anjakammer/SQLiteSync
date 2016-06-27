package de.anjakammer.bassa;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class QuestionDbHelper extends SQLiteOpenHelper{

    private static final String LOG_TAG = QuestionDbHelper.class.getSimpleName();
    public static final String DB_NAME = "Questionnaire.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_QUESTIONNAIRE = "Questionnaire";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ISDELETED = "isDeleted";

    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_QUESTIONNAIRE +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_QUESTION + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_ISDELETED + " BOOLEAN NOT NULL DEFAULT 0);";
    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_QUESTIONNAIRE;

    public QuestionDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DbHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    // gets triggert on getWritableDatabase()
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE + " angelegt.");
            db.execSQL(SQL_CREATE);
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
        }
    }

    // Die onUpgrade-Methode wird aufgerufen, sobald die neue Versionsnummer höher
    // als die alte Versionsnummer ist und somit ein Upgrade notwendig wird
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "Die Tabelle mit Versionsnummer " + oldVersion + " wird entfernt.");
        db.execSQL(SQL_DROP);
        onCreate(db);
    }
}