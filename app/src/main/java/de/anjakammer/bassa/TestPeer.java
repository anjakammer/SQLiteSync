package de.anjakammer.bassa;



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
import java.util.Scanner;

public class TestPeer {
    public static final String LOG_TAG = TestPeer.class.getSimpleName();



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
//    // TODO remove this testing method
//    private void insertFakeAnswers(long question_id){
//        try{
//            createAnswer("", "1", question_id);
//            createAnswer("", "2", question_id);
//        }catch (Exception e){
//            e.printStackTrace();
//            Log.e(LOG_TAG, "createAnswer failed: " + e.getMessage());
//        }
//    }

}
