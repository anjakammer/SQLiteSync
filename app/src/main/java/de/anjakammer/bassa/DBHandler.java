package de.anjakammer.bassa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Path;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import de.anjakammer.bassa.SQLiteSync.SQLiteSyncHelper;

public class DBHandler extends SQLiteOpenHelper{

    private static final String LOG_TAG = DBHandler.class.getSimpleName();
    public static final String DB_ID = "QuestionnaireGroup1";
    public static final String DB_NAME = "Questionnaire.db";
    public static final int DB_VERSION = 5;
    public static final boolean IS_MASTER = true;

    public static final String TABLE_QUESTIONNAIRE = "Questionnaire";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DESCRIPTION = "question";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ISDELETED = "isDeleted";

    public static final String TABLE_ANSWERS = "Answers";
    public static final String COLUMN_A_ID = "_id";
    public static final String COLUMN_A_QUESTION_ID = "question_id";
    public static final String COLUMN_A_DESCRIPTION = "answer";
    public static final String COLUMN_A_PARTICIPANT = "participant";

    public static final String[] QUESTION_COLUMNS = {
            COLUMN_ID,
            COLUMN_DESCRIPTION,
            COLUMN_TITLE
    };

    public static final String[] ANSWER_COLUMNS = {
            COLUMN_A_ID,
            COLUMN_A_DESCRIPTION,
            COLUMN_A_PARTICIPANT,
            COLUMN_A_QUESTION_ID
    };

    public static final String QUESTIONS_CREATE =
            "CREATE TABLE " + TABLE_QUESTIONNAIRE +
                    "( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_ISDELETED + " BOOLEAN NOT NULL DEFAULT 0 );";

    public static final String ANSWERS_CREATE =
            "CREATE TABLE " + TABLE_ANSWERS +
                    "( " + COLUMN_A_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_A_QUESTION_ID + " INTEGER, " +
                    COLUMN_A_DESCRIPTION + " TEXT NOT NULL, " +
                    COLUMN_A_PARTICIPANT + " TEXT NOT NULL );";

    public static final String QUESTIONS_DROP = "DROP TABLE IF EXISTS " + TABLE_QUESTIONNAIRE;
    public static final String ANSWERS_DROP = "DROP TABLE IF EXISTS " + TABLE_ANSWERS;

    public SQLiteSyncHelper SyncDBHelper;

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        SQLiteDatabase db = getWritableDatabase();
        Log.d(LOG_TAG, "Path to database: " + db.getPath());
        if(this.SyncDBHelper == null){
            this.SyncDBHelper = new SQLiteSyncHelper(db, IS_MASTER, DB_ID);
        }

        Log.d(LOG_TAG, SyncDBHelper.getDelta(new TestPeer().getPeerDelta()).toString());

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if(this.SyncDBHelper == null){
            this.SyncDBHelper = new SQLiteSyncHelper(db, IS_MASTER, DB_ID);
        }
        try {
            db.execSQL(QUESTIONS_CREATE);
            this.SyncDBHelper.makeTableSyncable(TABLE_QUESTIONNAIRE);

            db.execSQL(ANSWERS_CREATE);
            this.SyncDBHelper.makeTableSyncable(TABLE_ANSWERS);
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "creating failed for table onCreate: "+ e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO save data before dropping
        db.execSQL(QUESTIONS_DROP);
        db.execSQL(ANSWERS_DROP);
        this.SyncDBHelper = new SQLiteSyncHelper(db, IS_MASTER, DB_ID);
        this.SyncDBHelper.tearDownSyncableDB();
        onCreate(db);
    }

    public void delete(String table, long _id){
        this.SyncDBHelper.delete(table, _id);
    }

    public void update(String table, long _id, ContentValues values){
        this.SyncDBHelper.update(table, _id, values);
    }

    public long insert(String table, ContentValues values){
        return this.SyncDBHelper.insert(table, values);
    }

    public Cursor select(boolean distinct, String table, String[] columns,
                         String selection, String[] selectionArgs, String groupBy,
                         String having, String orderBy, String limit){
        return this.SyncDBHelper.select(distinct, table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }

    public Cursor selectDeleted(boolean distinct, String table, String[] columns,
                         String selection, String[] selectionArgs, String groupBy,
                         String having, String orderBy, String limit){
        return this.SyncDBHelper.selectDeleted(distinct, table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }

}
