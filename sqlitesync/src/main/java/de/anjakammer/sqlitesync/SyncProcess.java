package de.anjakammer.sqlitesync;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.anjakammer.sqlitesync.exceptions.SyncableDatabaseException;

public class SyncProcess {

    public static final String LOG_TAG = SyncProcess.class.getSimpleName();

    private long lastSyncTime;
    private String name;
    private String message;
    private String interest;
    private JSONObject data;

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_NAME = "name";
    public static final String KEY_INTEREST = "dbId";
    public static final String KEY_DATA = "tables";
    public static final String KEY_LAST_SYNC_TIME = "lastSyncTime";
    public static final String VALUE_SYNCREQUEST = "SYNCREQUEST";
    public static final String VALUE_DELTA = "DELTA";
    public static final String VALUE_OK = "OK";
    public static final String VALUE_CLOSE = "CLOSE";
    private boolean waitingForClose;
    private boolean DeltaHasBeenSend;
    private boolean finished;


    public SyncProcess(String name, String message) {
        this.name = name;
        this.message = message;
        this.data = new JSONObject();
    }

    public SyncProcess(JSONObject response) throws SyncableDatabaseException {

        try {
            this.name = response.getString(KEY_NAME);
            this.interest = response.getString(KEY_INTEREST);
            this.data = response.getJSONObject(KEY_DATA);
            this.lastSyncTime = response.getLong(KEY_LAST_SYNC_TIME);

            this.message = response.getString(KEY_MESSAGE);
            switch (message) {
                case VALUE_SYNCREQUEST:
                    setDeltaHasBeenSent();
                    break;
                // TODO is this necessary?
            }
        } catch (JSONException e) {
            throw new SyncableDatabaseException(
                    "JSONObject error for creating response: " + e.getMessage());
        }
    }

    public void setDeltaHasBeenSent(){
        this.DeltaHasBeenSend = true;
    }

    public boolean DeltaHasBeenSent(){
        return this.DeltaHasBeenSend;
    }

    public void setWaitingForClose(){
        this.waitingForClose = true;
    }

    public boolean isWaitingForClose(){
        return this.waitingForClose;
    }

    public void setCompleted(){
        this.finished = true;
    }

    public boolean isCompleted(){
        return this.finished;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getInterest() {
        return interest;
    }

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.message.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SyncProcess other = (SyncProcess) obj;
        return !(this.name.equals(other.getName()) || this.message.equals(other.getMessage()));
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put(KEY_MESSAGE, getMessage());
        object.put(KEY_INTEREST, getInterest());
            object.put(KEY_NAME, getName());
            object.put(KEY_LAST_SYNC_TIME, getLastSyncTime());
            object.put(KEY_DATA, getData());
        } catch (JSONException e) {
            Log.e(LOG_TAG, "SyncProcess to String failed for " + getName() + e.getMessage());
        }
        return object.toString();
    }

}