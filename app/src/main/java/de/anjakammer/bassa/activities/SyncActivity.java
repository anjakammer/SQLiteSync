package de.anjakammer.bassa.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.sharkfw.system.L;
import net.sharksystem.android.peer.SharkServiceController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.anjakammer.bassa.ContentProvider;
import de.anjakammer.bassa.R;
import de.anjakammer.bassa.commService.SyncProtocol;
import de.anjakammer.bassa.models.Participant;
import de.anjakammer.sqlitesync.SyncProcess;
import de.anjakammer.sqlitesync.exceptions.SyncableDatabaseException;


/**
 * uses the SyncProtocol
 */


public class SyncActivity extends AppCompatActivity {


    public static final String LOG_TAG = SyncProtocol.class.getSimpleName();
    private HashMap<String, SyncProcess> responseMap;
    private Handler mThreadHandler;
    private Runnable participantsLookup;

    private ContentProvider contentProvider;
    private ListView mParticipantsListView;
    private SyncProtocol syncProtocol;
    private List<Participant> participantsList;
    private SharkServiceController mServiceController;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        createSyncButton();
        initializeParticipantsListView();
        participantsList = new ArrayList<>();
        showAllListEntries();

        Context mContext = getApplicationContext();
        contentProvider = new ContentProvider(mContext);




//        // TODO is this instance call necessary here? Should be in DataPort only
        L.setLogLevel(L.LOGLEVEL_ALL);
//        mServiceController = SharkServiceController.getInstance(mContext);
//        mServiceController.setOffer(contentProvider.getProfileName(), contentProvider.getDbId());
//        mServiceController.startShark();
//        // TODO

        syncProtocol = new SyncProtocol(
                contentProvider.getProfileName(),contentProvider.getDbId(), mContext);

        mThreadHandler = new Handler();
        participantsLookup = new Runnable() {
            @Override
            public void run() {
                participantsList.clear();

                for(String participantName : syncProtocol.getPeers()){
                    Participant participant = contentProvider.getParticipantByName(participantName);
                    if(participant.getId() == -1){
                        participantsList.add(
                                contentProvider.createParticipant(participantName, false));
                    }else{
                        participantsList.add(participant);
                    }
                }

                showAllListEntries();
                mThreadHandler.postDelayed(this, 5000);
            }
        };

        mThreadHandler.post(participantsLookup);

        Runnable responsesLookup = new Runnable() {
            @Override
            public void run() {
                HashMap<String, SyncProcess> oldResponseMap = responseMap;
                responseMap = syncProtocol.receiveResponse(responseMap);
                String message;

                if(oldResponseMap.hashCode() != responseMap.hashCode()){
                    Collection<SyncProcess> responses = responseMap.values();

                    for(SyncProcess talk : responses){
                        message = talk.getMessage();

                        switch (message) {
                            case SyncProtocol.VALUE_SYNCREQUEST:
                                sendDelta(talk);
                                talk.setDeltaHasBeenSent();
                                break;
                            case SyncProtocol.VALUE_DELTA:
                                if(talk.DeltaHasBeenSent()){
                                    JSONObject delta = null;
                                    try {
                                        delta = new JSONObject(talk.toString());
                                        contentProvider.updateDB(delta);
                                        talk.setWaitingForClose();
                                    } catch (JSONException | SyncableDatabaseException e) {
                                        Log.e(LOG_TAG, "Synchronization for Participant " +
                                                talk.getName() + " failed: " + e.getMessage());
                                    }
                                }
                                break;
                            case SyncProtocol.VALUE_OK:
                                syncProtocol.sendClose(talk);
                                talk.setCompleted();
                                break;
                            case SyncProtocol.VALUE_CLOSE:
                                talk.setCompleted();
                                break;
                        }


                    }
                }

                mThreadHandler.postDelayed(this, 3000);
            }
        };

        mThreadHandler.post(responsesLookup);
    }

    private void sendDelta(SyncProcess syncProcess) {
        JSONObject participantDelta;
        JSONObject delta = new JSONObject();
        try {
            participantDelta = new JSONObject(syncProcess.toString());
            delta = contentProvider.getDelta(participantDelta);
        } catch (JSONException | SyncableDatabaseException e) {
            Log.e(LOG_TAG, "Fetching Delta failed for Participant " +
                    syncProcess.getName() + " while SyncRequest processing: " + e.getMessage());
        }
        syncProtocol.sendDelta(delta);
    }



    private void createSyncButton(){
        final Button button = (Button) findViewById(R.id.button_set_peers_and_sync);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mThreadHandler.removeCallbacks(participantsLookup);
                setParticipants();
                Toast.makeText(getApplicationContext()
                        , "Requesting for synchronization. Please wait.", Toast.LENGTH_LONG).show();
                syncProtocol.syncRequest(); // this sends a Broadcast
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    Toast.makeText(getApplicationContext()
                            , "Synchronization failed", Toast.LENGTH_LONG).show();
                    return;
                }
                processParticipantsSync();
                //TODO Outputs an Log as HasMap, write this is a ListView !
                // TODO show a failed sync process in ListView

            }
        });
    }

    private HashMap<String, Boolean> processParticipantsSync() {
        HashMap<String, Boolean> syncLog = new HashMap<>();
        List<Participant> participantsList = contentProvider.getAllParticipants();

        if(participantsList.size()>0) {

            for (Participant participant : participantsList) {
                String participantName = participant.getName();

                boolean status = false;
                if (responseMap.containsKey(participantName) &&
                        responseMap.get(participantName).getMessage().equals(SyncProtocol.VALUE_DELTA)) {
                    try {
                        JSONObject participantsDelta = new JSONObject(
                                responseMap.get(participantName).toString());
                        JSONObject delta = contentProvider.getUpdate(participantsDelta);
                        syncProtocol.sendDelta(delta);
                        Thread.sleep(4000);
                        status = responseMap.get(participantName).getMessage()
                                .equals(SyncProtocol.VALUE_OK);
                        if (status) {
                            // TODO syncProtocol.sendClose();
                        }
                    } catch (SyncableDatabaseException | JSONException |InterruptedException e) {
                        status = false;
                        Log.e(LOG_TAG, "Synchronization for Participant " +
                                participantName + " failed: " + e.getMessage());
                    }
                }
                syncLog.put(participantName, status);
            }
        }
        responseMap.clear();
        return syncLog;
    }

    private void setParticipants(){
        SparseBooleanArray touchedParticipantsPositions = mParticipantsListView.getCheckedItemPositions();
        int itemCount = touchedParticipantsPositions.size();
        if(itemCount < 1){
            Toast.makeText(getApplicationContext(), R.string.select_participants, Toast.LENGTH_LONG).show();
        }else {
            for (int i = 0; i < itemCount; i++) {
                boolean isSelected = touchedParticipantsPositions.valueAt(i);
                int positionInListView = touchedParticipantsPositions.keyAt(i);

                Participant participant = (Participant) mParticipantsListView.getItemAtPosition(positionInListView);
                contentProvider.setParticipant(participant, isSelected);
            }
        }
    }

    private void initializeParticipantsListView() {
        List<Participant> emptyListForInitialization = new ArrayList<>();
        mParticipantsListView = (ListView) findViewById(R.id.listview_items);
        TextView mParticipantsHeadline = (TextView) findViewById(R.id.headline);
        mParticipantsHeadline.setText(R.string.headline_active_participants);
        ArrayAdapter<Participant> ParticipantsArrayAdapter = new ArrayAdapter<Participant>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                emptyListForInitialization) {
        };
        mParticipantsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mParticipantsListView.setAdapter(ParticipantsArrayAdapter);
    }

    private void showAllListEntries() {
        ArrayAdapter<Participant> adapter = (ArrayAdapter<Participant>) mParticipantsListView.getAdapter();
        adapter.clear();
        adapter.addAll(participantsList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        mThreadHandler.post(participantsLookup);
        super.onResume();
    }

    @Override
    public void onPause() {
        mThreadHandler.removeCallbacks(participantsLookup);
        super.onPause();
    }
}
