package de.anjakammer.bassa.SQLiteSync;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import org.json.JSONArray;
import org.json.JSONException;

public class SQLiteSyncHelper {

    private boolean isMaster;
    public static final String TABLE_SETTINGS = "SETTINGS";

    public SQLiteSyncHelper(boolean isMaster){
        this.isMaster = isMaster;
    }

    public JSONArray getDelta(JSONArray peer, String[] tables, SQLiteDatabase db) throws JSONException {
        JSONArray delta = new JSONArray();
        String lastSyncTime = "GetValueFromPeerJSONArray";
        for (String table: tables) {
            Cursor cursor = db.query(
                    table,new String[] {"last_updated"},"last_updated >= '?'",new String[] {lastSyncTime}
                    ,null, null, null
            );
            delta.put("cursor.toString()");
        }

        return delta;
    }

    private String getLastUpdateTime(SQLiteDatabase db){
        Cursor cursor = db.query(
                TABLE_SETTINGS,new String[] {"value"},"key = '?'",new String[] {"lastUpdateTime"}
                ,null, null, null
                );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex("value");
        String lastUpdateTime = cursor.getString(valueIndex);
        return lastUpdateTime;
    }

}
