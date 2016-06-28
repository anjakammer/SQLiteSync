package de.anjakammer.bassa.SQLiteSync;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import de.anjakammer.bassa.DBHandler;


public class SQLiteSyncHelper {

    private boolean isMaster;
    private String dbID;
    private SQLiteDatabase db;
    private static final String LOG_TAG = SQLiteSyncHelper.class.getSimpleName();
    private static final String TABLE_SETTINGS = "SETTINGS";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_KEY = "key";
    private static final String COLUMN_VALUE = "value";
    private static final String SETTINGS_CREATE =
            "CREATE TABLE " + TABLE_SETTINGS +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_KEY + " TEXT NOT NULL, " +
                    COLUMN_VALUE + " TEXT NOT NULL ;";
    private static final String KEY_IS_MASTER = "isMaster";
    private static final String KEY_DB_ID = "DB_ID";


    public SQLiteSyncHelper(SQLiteDatabase db, boolean isMaster, String dbID){
        this.db = db;
        if(! isTableExisting(TABLE_SETTINGS)){
            prepareSyncableDB(isMaster);
            return;
        }

        if(isDbMaster() != isMaster) {
            setMaster(isMaster);
        }

        if(getDbId() != dbID) {
            setDbID(dbID);
        }
    }

    public void setMaster(boolean isMaster) {
        this.isMaster = isMaster;
        int intValueIsMaster = (this.isMaster) ? 1 : 0;

        ContentValues values = new ContentValues();
        values.put(COLUMN_VALUE, intValueIsMaster);

        this.db.update(TABLE_SETTINGS,
                values,
                COLUMN_KEY + "=" + KEY_IS_MASTER,
                null);
    }

    public void setDbID(String dbID) {
        this.dbID = dbID;

        ContentValues values = new ContentValues();
        values.put(COLUMN_VALUE, this.dbID);

        this.db.update(TABLE_SETTINGS,
                values,
                COLUMN_KEY + "=" + KEY_DB_ID,
                null);
    }


    private void prepareSyncableDB(boolean isMaster){
        // TODO fill settings table
        try {
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + SETTINGS_CREATE + " angelegt.");
            this.db.execSQL(SETTINGS_CREATE);
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
        }

        ContentValues isMasterKeyValue = new ContentValues();
        isMasterKeyValue.put(COLUMN_KEY, KEY_IS_MASTER);
        isMasterKeyValue.put(COLUMN_VALUE, isMaster);

        this.db.insert(TABLE_SETTINGS, null, isMasterKeyValue);


    }
    public void tearDownSyncableDB(){
        // TODO execute drop settings table
    }

    public JSONObject getDelta(JSONObject peer, SQLiteDatabase db) throws JSONException {
        // TODO test-data from peer delta
        peer.put("lastSyncTime","1462109540");
        String lastSyncTime = "1462109540";
        // TODO test-data from peer delta

        JSONObject delta = prepareDeltaObject(lastSyncTime, getDbId());
        List<String> tables = getTableNames(db);
        for (String table: tables) {
            Cursor cursor = db.query(
                    table, new String[] {"last_updated"},"last_updated >= '?'",new String[] {lastSyncTime}
                    ,null, null, null
            );
            cursor.close();
        }
        return delta;

    }

    private JSONObject prepareDeltaObject(String lastSyncTime, String dbId){
        JSONObject delta = new JSONObject();
        try {
            delta.put("DB-ID", dbId);
            delta.put("isMaster",String.valueOf(isMaster));
            delta.put("lastSyncTime",lastSyncTime);
            delta.put("tables",new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return delta;
    }

    private String getDbId(){
        Cursor cursor = this.db.query(
                TABLE_SETTINGS,new String[] {"value"},"key = '?'",new String[] {"DB-ID"}
                ,null, null, null
        );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex("value");
        String DbId = cursor.getString(valueIndex);
        cursor.close();
        return DbId;
    }

    private String getLastUpdateTime(){
        Cursor cursor = this.db.query(
                TABLE_SETTINGS,new String[] {"value"},"key = '?'",new String[] {"lastUpdateTime"}
                ,null, null, null
        );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex("value");
        String lastUpdateTime = cursor.getString(valueIndex);
        cursor.close();
        return lastUpdateTime;
    }

    public List<String> getTableNames(SQLiteDatabase db){
        List<String> result = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master", null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        result.remove(TABLE_SETTINGS);
        return result;
    }


    public boolean isTableExisting(String table){
        Cursor cursor = this.db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '" + table + "'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public boolean isDbMaster(){
        Cursor cursor = this.db.query(
                TABLE_SETTINGS,new String[] {"value"},"key = '?'",new String[] {"isMaster"}
                ,null, null, null
        );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex("value");
        boolean isMaster = cursor.getInt(valueIndex)== 1;
        cursor.close();
        return isMaster;
    }

}
