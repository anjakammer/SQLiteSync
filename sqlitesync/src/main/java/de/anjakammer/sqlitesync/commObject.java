package de.anjakammer.sqlitesync;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.anjakammer.sqlitesync.exceptions.SyncableDatabaseException;

public class CommObject {

    public static final String LOG_TAG = CommObject.class.getSimpleName();

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


    public CommObject(String name, String message) {
        this.name = name;
        this.message = message;
        this.data = new JSONObject();
    }

    public CommObject(JSONObject response) throws SyncableDatabaseException {

        try {
            this.name = response.getString(KEY_NAME);
            this.interest = response.getString(KEY_INTEREST);
            this.data = response.getJSONObject(KEY_DATA);
            this.lastSyncTime = response.getLong(KEY_LAST_SYNC_TIME);

            this.message = response.getString(KEY_MESSAGE);
        } catch (JSONException e) {
            throw new SyncableDatabaseException(
                    "JSONObject error for creating commObject: " + e.getMessage());
        }
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
        CommObject other = (CommObject) obj;
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
            Log.e(LOG_TAG, "commObject to String failed for " + getName() + e.getMessage());
        }
        return object.toString();
    }

}