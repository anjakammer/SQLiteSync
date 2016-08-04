package de.anjakammer.bassa.commService;


import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.anjakammer.sqlitesync.SyncProtocolInterface;

public class SyncProtocol implements SyncProtocolInterface{
    public static final String LOG_TAG = SyncProtocol.class.getSimpleName();
    private static final String KEY_DB_ID = "DB_ID";
    private static final String KEY_SYNCREQUEST = "syncRequest";
    private static final String KEY_MESSAGE = "message";
    private static final String VALUE_OK = "OK";
    private static final String VALUE_CLOSE = "CLOSE";
    private final String DbId;

    DataPort dataPort;

    public SyncProtocol(String name, String DbId, Context context){
        this.DbId = DbId;
        this.dataPort = new DataPort(name, DbId, context);
    }

    public void syncRequest(){
        // TODO receiver integration not necessary? Broadcast?
        JSONObject request = new JSONObject();
        try {
            request.put(KEY_DB_ID, DbId);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "JSONObject error for writing syncRequest JSON: " + e.getMessage());
        }

        dataPort.sendData(request.toString());
    }

    public List<String> getPeers(){
        return dataPort.getPeers();
    }

    public void sendDelta(JSONObject delta){
        dataPort.sendData(delta.toString());
    }

    public List<JSONObject> receiveResponse() throws JSONException {
        // TODO polling for messages, using Handler and postDelayed
        List<JSONObject> responses = new ArrayList<>();

        List<String> data = dataPort.getData();
        for (String item : data) {
            JSONObject response = new JSONObject(item);

            if (response.get(KEY_DB_ID).equals(this.DbId)){
                responses.add(response);
            }
        }

        return responses;
    }


    public void sendOK(){
        dataPort.sendData(writeMessage(VALUE_OK));
    }

    public void sendClose(){
        dataPort.sendData(writeMessage(VALUE_CLOSE));
    }

    private String writeMessage(String messageToSend){
        JSONObject message = new JSONObject();
        try {
            message.put(KEY_MESSAGE, messageToSend);
            message.put(KEY_DB_ID, this.DbId);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "JSONObject error for writing "+messageToSend+"-Message JSON: " + e.getMessage());
        }
        return message.toString();
    }
}
