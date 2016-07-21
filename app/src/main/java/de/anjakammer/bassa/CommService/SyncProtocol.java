package de.anjakammer.bassa.CommService;


import android.util.Log;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SyncProtocol {
    public static final String LOG_TAG = SyncProtocol.class.getSimpleName();
    private static final String KEY_DB_ID = "DB_ID";
    private static final String KEY_MESSAGE = "message";
    private static final String VALUE_DELTA = "DELTA";
    private static final String VALUE_OK = "OK";
    private static final String VALUE_CLOSE = "CLOSE";
    private final String DbId;

    DataPort dataPort;

    public SyncProtocol(String DbId){
        this.DbId = DbId;
    }

    public void syncRequest(){
        // TODO receiver integration not necessary? Broadcast?
        DataPort dataPort = new DataPort();
        JSONObject request = new JSONObject();
        try {
            request.put(KEY_DB_ID, DbId);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "JSONObject error for writing syncRequest JSON: " + e.getMessage());
        }
        byte[] bytes = request.toString().getBytes();
        dataPort.sendData(bytes);
    }

    public void sendDelta(JSONObject delta){
        byte[] bytes = delta.toString().getBytes();
        dataPort.sendData(bytes);
    }

    public JSONObject receiveResponse() throws JSONException {
        // TODO polling for messages, timeout for x seconds, using future?

        String string = new String(dataPort.getData(), StandardCharsets.UTF_8);
        JSONObject response = new JSONObject(string);

        if (!response.get(KEY_DB_ID).equals(this.DbId)){
            return null;    // TODO better solution for this
        }

        return response;
    }


    public void sendOK(){
        dataPort.sendData(writeMessage(VALUE_OK));
    }

    public void sendClose(){
        dataPort.sendData(writeMessage(VALUE_CLOSE));
    }

    private byte[] writeMessage(String messageToSend){
        JSONObject message = new JSONObject();
        try {
            message.put(KEY_MESSAGE, messageToSend);
            message.put(KEY_DB_ID, this.DbId);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "JSONObject error for writing "+messageToSend+"-Message JSON: " + e.getMessage());
        }
        return message.toString().getBytes();
    }
}
