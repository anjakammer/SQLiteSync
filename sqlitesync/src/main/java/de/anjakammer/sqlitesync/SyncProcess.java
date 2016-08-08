package de.anjakammer.sqlitesync;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SyncProcess {

    public static final String LOG_TAG = SyncProcess.class.getSimpleName();

    private String name;
    private String message;

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_NAME = "name";
    private boolean waitingForClose;
    private boolean waitingForOk;
    private boolean DeltaHasBeenSend;
    private boolean finished;


    public SyncProcess(String name) {
        this.name = name;
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

    public void setWaitingForOK(){
        this.waitingForOk = true;
    }

    public boolean isWaitingForOK(){
        return this.waitingForOk;
    }

    public void setCompleted(){
        this.finished = true;
    }

    public boolean isCompleted(){
        return this.finished;
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
            object.put(KEY_NAME, getName());

        } catch (JSONException e) {
            Log.e(LOG_TAG, "SyncProcess to String failed for " + getName() + e.getMessage());
        }
        return object.toString();
    }

}