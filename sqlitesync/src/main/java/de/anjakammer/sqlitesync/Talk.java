package de.anjakammer.sqlitesync;


import org.json.JSONException;
import org.json.JSONObject;

import de.anjakammer.sqlitesync.exceptions.SyncableDatabaseException;

public class Talk {

    private String name;
    private String message;
    private String interest;
    private JSONObject data;

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_NAME = "name";
    public static final String KEY_INTEREST = "dbId";
    public static final String KEY_DATA = "tables";


    public Talk(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public Talk(String response) throws SyncableDatabaseException {
        JSONObject object = null;
        try {
            object = new JSONObject(response);
            this.name = object.getString(KEY_NAME);
            this.interest = object.getString(KEY_INTEREST);
            this.message = object.getString(KEY_MESSAGE);
            this.data = object.getJSONObject(KEY_DATA);
        } catch (JSONException e) {
            throw new SyncableDatabaseException(
                    "JSONObject error for creating response: " + e.getMessage());
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
        Talk other = (Talk) obj;
        return !(this.name.equals(other.getName()) || this.message.equals(other.getMessage()));
    }

    @Override
    public String toString() {
        // TODO
        return "todo";
    }

}