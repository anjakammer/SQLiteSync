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



    private HashMap<String, SyncProcess> responseMap = new HashMap<>();
    private Handler mThreadHandler;
    private ContentProvider contentProvider;
    private ListView mParticipantsListView;
    private SyncProtocol syncProtocol;
    private List<Participant> participantsList = new ArrayList<>();
    private Runnable participantsLookup;
    private ListView mSynchronizedListView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        createSyncButton();
        initializeParticipantsListView();
        initializeSynchronizedListView();
        showAllListEntries();

        Context mContext = getApplicationContext();
        contentProvider = new ContentProvider(mContext);

        // TODO remove this
        L.setLogLevel(L.LOGLEVEL_ALL);

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


        Runnable responsesLookup = new Runnable() {
            @Override
            public void run() {
                HashMap<String, SyncProcess> oldResponseMap = getResponseMap();
                HashMap<String, SyncProcess> responseMap = syncProtocol.receiveResponse(oldResponseMap);
                String message;
                    // todo repsonsemap cannot be accessed
                if(oldResponseMap.size() != responseMap.size()){
                List<SyncProcess> responses = new ArrayList<>(responseMap.values());

                    for(SyncProcess talk : responses){
                        message = talk.getMessage();

                        switch (message) {
                            case SyncProtocol.VALUE_SYNCREQUEST:
                                sendDelta(talk);
                                talk.setDeltaHasBeenSent();
                                Log.d(LOG_TAG, "SyncRequest was sent to: " + talk.getName());
                                break;
                            case SyncProtocol.VALUE_DELTA:
                                if(talk.DeltaHasBeenSent()){
                                    JSONObject delta = null;
                                    try {
                                        delta = new JSONObject(talk.toString());
                                        contentProvider.updateDB(delta);
                                        talk.setWaitingForClose();
                                        Log.d(LOG_TAG, "updated DB, wiating for close from: " + talk.getName());
                                    } catch (JSONException | SyncableDatabaseException e) {
                                        Log.e(LOG_TAG, "Synchronization for Participant " +
                                                talk.getName() + " failed: " + e.getMessage());
                                    }
                                }
                                break;
                            case SyncProtocol.VALUE_OK:
                                syncProtocol.sendClose(talk);
                                talk.setCompleted();
                                Log.d(LOG_TAG, "Completed through OK from: " + talk.getName());
                                break;
                            case SyncProtocol.VALUE_CLOSE:
                                talk.setCompleted();
                                Log.d(LOG_TAG, "Completed through CLOSE from: " + talk.getName());
                                break;
                        }
                                responseMap.put(talk.getName(), talk); // TODO write into responsemap !!!
                    }
               }
                setResponseMap(responseMap);
                mThreadHandler.postDelayed(this, 3000);
            }
        };

        Runnable syncStatusLookup = new Runnable() {
            @Override
            public void run() {
                updateSyncList();
                mThreadHandler.postDelayed(this, 3000);
            }
        };

        mThreadHandler.post(participantsLookup);
        mThreadHandler.post(responsesLookup);
        mThreadHandler.post(syncStatusLookup);
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
            }
        });
    }

    private void updateSyncList() {
        List<Participant> syncLog = new ArrayList<>();
        List<Participant> participantsList = contentProvider.getAllParticipants();

        for (Participant participant : participantsList){
            String participantName = participant.getName();
            if(responseMap.containsKey(participantName)
                    && responseMap.get(participantName).isCompleted())
                syncLog.add(participant);
        }
        ArrayAdapter<Participant> adapter = (ArrayAdapter<Participant>) mSynchronizedListView.getAdapter();
        adapter.clear();
        adapter.addAll(syncLog);
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

    public HashMap<String, SyncProcess> getResponseMap() {
        return this.responseMap;
    }

    public void setResponseMap(HashMap<String, SyncProcess> responseMap) {
        this.responseMap = responseMap;
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

    private void initializeSynchronizedListView() {
        List<Participant> emptyListForInitialization = new ArrayList<>();
        mSynchronizedListView = (ListView) findViewById(R.id.listview_items_2);
        TextView mParticipantsHeadline = (TextView) findViewById(R.id.headline_2);
        mParticipantsHeadline.setText(R.string.headline_updated_participants);
        ArrayAdapter<Participant> ParticipantsArrayAdapter = new ArrayAdapter<Participant>(
                this,
                android.R.layout.simple_list_item_checked,
                emptyListForInitialization) {
        };
        mSynchronizedListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        mSynchronizedListView.setAdapter(ParticipantsArrayAdapter);
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
