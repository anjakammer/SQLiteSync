package de.anjakammer.bassa;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestPeer {

    public JSONObject getPeerDelta(){
        String lastSyncTime = "1437748127175";
        JSONObject delta = new JSONObject();
        try {
            delta.put("lastSyncTime",lastSyncTime);
            delta.put("DB_ID","QuestionnaireGroup1");
            delta.put("isMaster","false");
            delta.put("tables",new JSONArray(
                    "[{\"Answers\":[{\"_id\":\"1\",\"question_id\":\"1\",\"answer\":\"\",\"participant_id\":\"1\",\"isDeleted\":\"0\",\"timestamp\":\"1467748203223\"}," +
                            "{\"_id\":\"2\",\"question_id\":\"1\",\"answer\":\"1/5\",\"participant_id\":\"3\",\"isDeleted\":\"0\",\"timestamp\":\"1467758009475\"}," +
                            "{\"_id\":\"3\",\"question_id\":\"2\",\"answer\":\"3/5\",\"participant_id\":\"1\",\"isDeleted\":\"0\",\"timestamp\":\"1455748009475\"}," +
                            "{\"_id\":\"4\",\"question_id\":\"3\",\"answer\":\"5/5\",\"participant_id\":\"1\",\"isDeleted\":\"0\",\"timestamp\":\"1467748609475\"}," +
                            "{\"_id\":\"5\",\"question_id\":\"3\",\"answer\":\"\",\"participant_id\":\"2\",\"isDeleted\":\"0\",\"timestamp\":\"1467748009475\"}," +

                            "]}]"
            ));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Test-Peer", "JSONObject error for writing test-peer JSON: " + e.getMessage());
        }

        return delta;
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
