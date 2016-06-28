package de.anjakammer.bassa;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import de.anjakammer.bassa.SQLiteSync.SQLiteSyncHelper;

public class DBHandler extends SQLiteOpenHelper{

    private static final String LOG_TAG = DBHandler.class.getSimpleName();
    public static final String DB_ID = "QuestionnaireGroup1";
    public static final String DB_NAME = "Questionnaire.db";
    public static final int DB_VERSION = 1;
    public static final boolean IS_MASTER = true;

    public static final String TABLE_QUESTIONNAIRE = "Questionnaire";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DESCRIPTION = "question";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ISDELETED = "isDeleted";

    public static final String TABLE_ANSWERS = "ANSWERS";
    public static final String COLUMN_A_ID = "_id";
    public static final String COLUMN_A_QUESTION_ID = "question_id";
    public static final String COLUMN_A_DESCRIPTION = "answer";
    public static final String COLUMN_A_PARTICIPANT = "participant";

    public static final String QUESTIONS_CREATE =
            "CREATE TABLE " + TABLE_QUESTIONNAIRE +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_ISDELETED + " BOOLEAN NOT NULL DEFAULT 0);";

    public static final String ANSWERS_CREATE =
            "CREATE TABLE " + TABLE_ANSWERS +
                    "(" + COLUMN_A_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_A_QUESTION_ID + " INTEGER, " +
                    COLUMN_A_DESCRIPTION + " TEXT NOT NULL, " +
                    COLUMN_A_PARTICIPANT + " TEXT NOT NULL );";

    public static final String QUESTIONS_DROP = "DROP TABLE IF EXISTS " + TABLE_QUESTIONNAIRE;

    public static final String ANSWERS_DROP = "DROP TABLE IF EXISTS " + TABLE_ANSWERS;

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DBHandler hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    // gets triggert on getWritableDatabase()
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + QUESTIONS_CREATE + " angelegt.");
            db.execSQL(QUESTIONS_CREATE);
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + ANSWERS_CREATE + " angelegt.");
            db.execSQL(ANSWERS_CREATE);
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
        }

        SQLiteSyncHelper SyncDBHelper = new SQLiteSyncHelper(db, IS_MASTER, DB_ID);
        //TODO

    }

    // Die onUpgrade-Methode wird aufgerufen, sobald die neue Versionsnummer höher
    // als die alte Versionsnummer ist und somit ein Upgrade notwendig wird
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(QUESTIONS_DROP);
        db.execSQL(ANSWERS_DROP);
        //TODO drop sync DB
        onCreate(db);
        //TODO create sync db
    }
}