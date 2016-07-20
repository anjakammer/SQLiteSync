package de.anjakammer.bassa;



import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class TestPeer {
    public static final String LOG_TAG = TestPeer.class.getSimpleName();

    public TestPeer(DBHandler dbHandler){
        ContentValues valuesA = new ContentValues();
        valuesA.put(DBHandler.COLUMN_P_ID, 1);
        valuesA.put(DBHandler.COLUMN_P_NAME, "Peer1");
        valuesA.put(DBHandler.COLUMN_P_ADDRESS, "Address:to:1");
        dbHandler.insert(DBHandler.TABLE_PARTICIPANTS,valuesA);

        ContentValues valuesB = new ContentValues();
        valuesB.put(DBHandler.COLUMN_P_ID, 2);
        valuesB.put(DBHandler.COLUMN_P_NAME, "Peer2");
        valuesB.put(DBHandler.COLUMN_P_ADDRESS, "Address:to:2");
        dbHandler.insert(DBHandler.TABLE_PARTICIPANTS,valuesB);

        ContentValues valuesC = new ContentValues();
        valuesC.put(DBHandler.COLUMN_P_ID, 3);
        valuesC.put(DBHandler.COLUMN_P_NAME, "Peer3");
        valuesC.put(DBHandler.COLUMN_P_ADDRESS, "Address:to:3");
        dbHandler.insert(DBHandler.TABLE_PARTICIPANTS,valuesC);
    }

    public JSONObject getPeerDelta(Context context){
        String lastSyncTime = "1437748127175";
        JSONObject delta = new JSONObject();
        try {
            delta.put("lastSyncTime",lastSyncTime);
            delta.put("DB_ID","QuestionnaireGroup1");
            delta.put("isMaster","false");
            delta.put("tables",new JSONArray(getDataFromFile(context, "participantsAnswers.json")));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "JSONObject error for writing test-peer JSON: " + e.getMessage());
        }

        return delta;
    }

    public String getDataFromFile(Context context, String path) {
        InputStream input;
        AssetManager assetManager = context.getAssets();
        byte[] buffer = null;
        try {
            input = assetManager.open(path);

            int size = input.available();
            buffer = new byte[size];
            input.read(buffer);
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "failed to read from test delta file: " + e.getMessage());
        }
        return new String(buffer);
    }

}
