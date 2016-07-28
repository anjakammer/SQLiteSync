package de.anjakammer.sqlitesync;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public interface SyncProtocolInterface {

    String KEY_DB_ID = "DB_ID";
    String KEY_SYNCREQUEST = "syncRequest";
    String KEY_MESSAGE = "message";
    String VALUE_OK = "OK";
    String VALUE_CLOSE = "CLOSE";

    void syncRequest();

    List<String> getPeers();

    void sendDelta(JSONObject delta);

    List<JSONObject> receiveResponse() throws JSONException;

    void sendOK();

    void sendClose();
}
