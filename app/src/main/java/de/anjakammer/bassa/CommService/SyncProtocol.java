package de.anjakammer.bassa.commService;


import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.anjakammer.sqlitesync.Talk;
import de.anjakammer.sqlitesync.SQLiteSyncProtocol;
import de.anjakammer.sqlitesync.exceptions.SyncableDatabaseException;

public class SyncProtocol implements SQLiteSyncProtocol {
    public static final String LOG_TAG = SyncProtocol.class.getSimpleName();
    public static final String KEY_DB_ID = "dbId";
    public static final String VALUE_SYNCREQUEST = "SYNCREQUEST";
    public static final String VALUE_DELTA = "DELTA";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_NAME = "name";
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
        Talk request = new Talk(this.name, VALUE_SYNCREQUEST);
        request.setInterest(DbId);

        dataPort.sendData(request.toString());
    }

    public List<String> getPeers(){
        return dataPort.getPeers();
    }

    public void sendDelta(Talk delta){
        dataPort.sendData(delta.toString());
    }

    public List<Talk> receiveResponse(){
        List<Talk> responses = new ArrayList<>();

        List<String> data = dataPort.getData();
        try{
            for (String item : data) {
                Talk response = new Talk(item);

                if (response.getInterest().equals(this.DbId)){
                    responses.add(response);
                }
            }
        } catch (SyncableDatabaseException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
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
        Talk message = new Talk(this.name, messageToSend);
        message.setInterest(this.DbId);
        return message.toString();
    }
}
