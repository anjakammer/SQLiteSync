package de.anjakammer.bassa;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class TestPeer {

    public JSONObject getPeerDelta(){
        String lastSyncTime = "1457748127175";
        JSONObject delta = new JSONObject();
        try {
            delta.put("lastSyncTime",lastSyncTime);
            delta.put("DB-ID","QuestionnaireGroup1");
            delta.put("isMaster","false");
            delta.put("tables",new JSONObject(
                    "\"Answers\":[{\"_id\":\"1\",\"question_id\":\"1\",\"answer\":\"not answered yet\",\"participant\":\"Peer1\",\"isDeleted\":\"0\",\"timestamp\":\"1467748003223\"},{\"_id\":\"2\",\"question_id\":\"1\",\"answer\":\"not answered yet\",\"participant\":\"Peer2\",\"isDeleted\":\"0\",\"timestamp\":\"1467748003233\"},{\"_id\":\"3\",\"question_id\":\"1\",\"answer\":\"not answered yet\",\"participant\":\"Peer1\",\"isDeleted\":\"0\",\"timestamp\":\"1467748003241\"},{\"_id\":\"4\",\"question_id\":\"1\",\"answer\":\"not answered yet\",\"participant\":\"Peer2\",\"isDeleted\":\"0\",\"timestamp\":\"1467748003246\"},{\"_id\":\"5\",\"question_id\":\"2\",\"answer\":\"not answered yet\",\"participant\":\"Peer1\",\"isDeleted\":\"0\",\"timestamp\":\"1467748009475\"}]}]}"
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
//            createAnswer("not answered yet", "Peer1", question_id);
//            createAnswer("not answered yet", "Peer2", question_id);
//        }catch (Exception e){
//            e.printStackTrace();
//            Log.e(LOG_TAG, "createAnswer failed: " + e.getMessage());
//        }
//    }

}
