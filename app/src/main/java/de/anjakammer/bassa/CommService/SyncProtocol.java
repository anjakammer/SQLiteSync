package de.anjakammer.bassa.commService;


import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import de.anjakammer.sqlitesync.CommObject;
import de.anjakammer.sqlitesync.SyncProcess;
import de.anjakammer.sqlitesync.SQLiteSyncProtocol;
import de.anjakammer.sqlitesync.exceptions.SyncableDatabaseException;

public class SyncProtocol implements SQLiteSyncProtocol {
    public static final String LOG_TAG = SyncProtocol.class.getSimpleName();
    public static final String KEY_NAME = "name";
    public static final String KEY_DB_ID = "dbId";
    public static final String KEY_MESSAGE = "message";
    public static final String VALUE_SYNCREQUEST = "SYNCREQUEST";
    public static final String VALUE_DELTA = "DELTA";
    public static final String VALUE_OK = "OK";
    public static final String VALUE_CLOSE = "CLOSE";
    private final String DbId;
    private final String name;

    DataPort dataPort;

    public SyncProtocol(String name, String DbId, Context context){
        this.DbId = DbId;
        this.name = name;
        this.dataPort = new DataPort(name, DbId, context);
    }

    public void syncRequest(){
        CommObject request = new CommObject(this.name, VALUE_SYNCREQUEST);
        request.setInterest(DbId);
        dataPort.sendData(request.toString());
    }

    public List<String> getPeers(){
        return dataPort.getPeers();
    }

    public void sendDelta(JSONObject delta){
        dataPort.sendData(delta.toString());
    }

    public HashMap<String, CommObject> receiveResponse(HashMap<String, CommObject> inputMap){
        List<String> data = dataPort.getData();

        for (String item : data) {
            JSONObject talk = null;
            try {
                talk = new JSONObject(item);
                if (talk.getString(KEY_DB_ID).equals(DbId)) {
                    inputMap.put(talk.getString(KEY_NAME),new CommObject(new JSONObject(item)));
                }
            } catch (JSONException | SyncableDatabaseException e) {
                Log.e(LOG_TAG, "Fetching data from broadcast failed: " +
                        item + " Error: "+  e.getMessage());
            }
        }
        return inputMap;
    }


    public void sendOK(SyncProcess message){
        message.setMessage(VALUE_OK);
        message.setName(this.name);
        dataPort.sendData(message.toString());
    }

    public void sendClose(SyncProcess message){
        message.setMessage(VALUE_CLOSE);
        message.setName(this.name);

        dataPort.sendData(message.toString());
    }
}
