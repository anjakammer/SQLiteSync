package de.anjakammer.bassa.SQLiteSync;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SQLiteSyncHelper {

    private boolean isMaster;
    public static final String TABLE_SETTINGS = "SETTINGS";

    public SQLiteSyncHelper(boolean isMaster){
        this.isMaster = isMaster;
    }

    public JSONObject getDelta(JSONObject peer, String[] tables, SQLiteDatabase db) throws JSONException {
        // TODO test-data from peer delta
        peer.put("lastSyncTime","1462109540");
        String lastSyncTime = "1462109540";
        // TODO test-data from peer delta

        JSONObject delta = new JSONObject();
        for (String table: tables) {
            Cursor cursor = db.query(
                    table, new String[] {"last_updated"},"last_updated >= '?'",new String[] {lastSyncTime}
                    ,null, null, null
            );
            cursor.close();
        }

        return delta;
    }

    private JSONObject prepareDelta(){
        JSONObject delta = new JSONObject();


        return delta;
    }

    private String getLastUpdateTime(SQLiteDatabase db){
        Cursor cursor = db.query(
                TABLE_SETTINGS,new String[] {"value"},"key = '?'",new String[] {"lastUpdateTime"}
                ,null, null, null
                );
        cursor.moveToFirst();
        int valueIndex = cursor.getColumnIndex("value");
        cursor.close();

        String lastUpdateTime = cursor.getString(valueIndex);
        return lastUpdateTime;
    }

}
