package de.anjakammer.bassa.SQLiteSync;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SQLiteSyncHelper {


    private boolean isMaster;
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

    public SQLiteSyncHelper(SQLiteDatabase db){
        if(! isTableExisting(TABLE_SETTINGS, db)){
            try {
                Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + SETTINGS_CREATE + " angelegt.");
                db.execSQL(SETTINGS_CREATE);
            }
            catch (Exception ex) {
                Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
            }
            return;
        }

        Cursor cursor = db.query(
                TABLE_SETTINGS,new String[] {"value"},"key = '?'",new String[] {"isMaster"}
                ,null, null, null
        );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex("value");
        this.isMaster = cursor.getInt(valueIndex)== 1;
        cursor.close();
    }

    public boolean isTableExisting(String table, SQLiteDatabase db){
        Cursor cursor = db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '" + table + "'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void tearDownSyncableDB(){
        // TODO execute drop settings table
    }

    public JSONObject getDelta(JSONObject peer, SQLiteDatabase db) throws JSONException {
        // TODO test-data from peer delta
        peer.put("lastSyncTime","1462109540");
        String lastSyncTime = "1462109540";
        // TODO test-data from peer delta

        JSONObject delta = prepareDeltaObject(lastSyncTime, getDbId(db));
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

    private String getDbId(SQLiteDatabase db){
        Cursor cursor = db.query(
                TABLE_SETTINGS,new String[] {"value"},"key = '?'",new String[] {"DB-ID"}
                ,null, null, null
        );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex("value");
        String DbId = cursor.getString(valueIndex);
        cursor.close();
        return DbId;
    }

    private String getLastUpdateTime(SQLiteDatabase db){
        Cursor cursor = db.query(
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

        return result;
    }

}
