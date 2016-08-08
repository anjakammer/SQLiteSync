package de.anjakammer.bassa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.anjakammer.sqlitesync.SQLiteSyncHelper;
import de.anjakammer.sqlitesync.exceptions.SyncableDatabaseException;

public class DBHandler extends SQLiteOpenHelper{

    private static final String LOG_TAG = DBHandler.class.getSimpleName();
    public static final String DB_ID = "Quiz";
    public static final String DB_NAME = "Questionnaire.db";
    public static final int DB_VERSION = 6;
    public static final boolean IS_MASTER = true;

    public static final String TABLE_QUESTIONS = "Questions";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DESCRIPTION = "question";
    public static final String COLUMN_TITLE = "title";

    public static final String TABLE_ANSWERS = "Answers";
    public static final String COLUMN_A_ID = "_id";
    public static final String COLUMN_A_QUESTION_ID = "question_id";
    public static final String COLUMN_A_DESCRIPTION = "answer";
    public static final String COLUMN_A_PARTICIPANT_ID = "participant_id";

    public static final String TABLE_PARTICIPANTS = "Participants";
    public static final String COLUMN_P_ID = "_id";
    public static final String COLUMN_P_NAME = "name";
    public static final String COLUMN_P_IS_SELECTED = "isSelected";

    public static final String[] QUESTION_COLUMNS = {
            COLUMN_ID,
            COLUMN_DESCRIPTION,
            COLUMN_TITLE
    };

    public static final String[] ANSWER_COLUMNS = {
            COLUMN_A_ID,
            COLUMN_A_DESCRIPTION,
            COLUMN_A_PARTICIPANT_ID,
            COLUMN_A_QUESTION_ID
    };

    public static final String[] PARTICIPANTS_COLUMNS = {
            COLUMN_P_ID,
            COLUMN_P_NAME,
            COLUMN_P_IS_SELECTED
    };

    public Map<String, String[]> uniqueColumns = new HashMap<String, String[]>() {{
        put( TABLE_ANSWERS, new String[]{COLUMN_A_QUESTION_ID, COLUMN_A_PARTICIPANT_ID});
    }};

    public static final String QUESTIONS_CREATE =
            "CREATE TABLE " + TABLE_QUESTIONS +
                    "( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT NOT NULL );";

    public static final String ANSWERS_CREATE =
            "CREATE TABLE " + TABLE_ANSWERS +
                    "( " + COLUMN_A_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_A_QUESTION_ID + " INTEGER, " +
                    COLUMN_A_DESCRIPTION + " TEXT NOT NULL, " +
                    COLUMN_A_PARTICIPANT_ID + " INTEGER NOT NULL, " +
                    "UNIQUE("+ COLUMN_A_QUESTION_ID +", "+
                    COLUMN_A_PARTICIPANT_ID +") );";

    public static final String PARTICIPANTS_CREATE =
            "CREATE TABLE " + TABLE_PARTICIPANTS +
                    "( " + COLUMN_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_P_IS_SELECTED + " BOOLEAN NOT NULL DEFAULT 0, " +
                    COLUMN_P_NAME + " TEXT NOT NULL );";

    public static final String QUESTIONS_DROP = "DROP TABLE IF EXISTS " + TABLE_QUESTIONS;
    public static final String ANSWERS_DROP = "DROP TABLE IF EXISTS " + TABLE_ANSWERS;
    public static final String PARTICIPANTS_DROP = "DROP TABLE IF EXISTS " + TABLE_PARTICIPANTS;

    public SQLiteSyncHelper SyncDBHelper;

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        SQLiteDatabase db = getWritableDatabase();
        Log.d(LOG_TAG, "Path to database: " + db.getPath());
        if(this.SyncDBHelper == null){
            this.SyncDBHelper = new SQLiteSyncHelper(db, IS_MASTER, DB_ID);
        }

//        try {
        // TODO rerender view after updating the DB !
//            if(SyncDBHelper.updateDB(new TestPeer(this).getPeerDelta())){
//                Log.d(LOG_TAG, "successfully DB update");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (SyncableDatabaseException e) {
//            e.printStackTrace();
//            Log.d(LOG_TAG, "unable to update DB " + e.getMessage());
//        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if(this.SyncDBHelper == null){
            this.SyncDBHelper = new SQLiteSyncHelper(db, IS_MASTER, DB_ID);
        }
        try {
            db.execSQL(QUESTIONS_CREATE);
            this.SyncDBHelper.makeTableSyncable(TABLE_QUESTIONS);

            db.execSQL(ANSWERS_CREATE);
            this.SyncDBHelper.makeTableSyncable(TABLE_ANSWERS);

            db.execSQL(PARTICIPANTS_CREATE);
            this.SyncDBHelper.makeTableSyncable(TABLE_PARTICIPANTS);


        }
        catch (Exception e) {
            Log.e(LOG_TAG, "creating failed for table onCreate: "+ e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO save data before dropping?
        db.execSQL(QUESTIONS_DROP);
        db.execSQL(ANSWERS_DROP);
        db.execSQL(PARTICIPANTS_DROP);
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

    public long insertIfNotExists(String table, ContentValues values){
        long _id = -1;

        try {
            _id = this.SyncDBHelper.insertIfNotExists(table, values, getUniqueColumns(table));
        } catch (SyncableDatabaseException e) {
            Log.e(LOG_TAG, "Inserting failed: "+ e.getMessage());
        }
        return _id;
    }

    private String[] getUniqueColumns(String table) {
        return this.uniqueColumns.get(table);
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

    public void setProfileName(String newName) {
        SyncDBHelper.setInstanceName(newName);
    }

    public String getProfileName() {
        return SyncDBHelper.getInstanceName();
    }

    public JSONObject getUpdate(JSONObject participantDelta) throws SyncableDatabaseException {
        JSONObject dBDelta;
        boolean status;
        try{
            dBDelta = this.SyncDBHelper.getDelta(participantDelta.getString(SQLiteSyncHelper.KEY_LAST_SYNC_TIME));
            status = this.SyncDBHelper.updateDB(participantDelta);
        }catch (JSONException e){
            throw new SyncableDatabaseException("Error while creating delta for synchronization: "+
            e.getMessage());
        }
        return (status)? dBDelta : null;
    }

    public JSONObject getDelta(JSONObject delta) throws SyncableDatabaseException {
        try {
            return this.SyncDBHelper.getDelta(delta.getString(SQLiteSyncHelper.KEY_LAST_SYNC_TIME));
        }catch (JSONException e){
            throw new SyncableDatabaseException("Error while creating delta for synchronization: "+
                    e.getMessage());
        }
    }

    public void updateDB(JSONObject delta) throws SyncableDatabaseException {
        try {
            this.SyncDBHelper.updateDB(delta);
        } catch (JSONException | SyncableDatabaseException e) {
            throw new SyncableDatabaseException("Error while updating DB for synchronization: "+
                    e.getMessage());
        }
    }
}
