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
import java.util.Set;

import de.anjakammer.bassa.ContentProvider;
import de.anjakammer.bassa.R;
import de.anjakammer.bassa.commService.SyncProtocol;
import de.anjakammer.bassa.models.Participant;
import de.anjakammer.sqlitesync.exceptions.SyncableDatabaseException;


/**
 * uses the SyncProtocol
 */


public class SyncActivity extends AppCompatActivity {

    private static final String KEY_NAME = "name";
    public static final String LOG_TAG = SyncProtocol.class.getSimpleName();
    private Handler mThreadHandler;
    private Runnable participantsLookup;
    private Runnable getResponse;
    private ContentProvider contentProvider;
    private ListView mParticipantsListView;
    private SyncProtocol syncProtocol;
    private List<Participant> participantsList;
    private SharkServiceController mServiceController;
    private List<JSONObject> responses;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        createSyncButton();
        initializeParticipantsListView();
        participantsList = new ArrayList<>();
        responses = new ArrayList<>();
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

    private void createSyncButton(){
        final Button button = (Button) findViewById(R.id.button_set_peers_and_sync);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mThreadHandler.removeCallbacks(participantsLookup);
                responses.clear();

                setParticipants();
                Toast.makeText(getApplicationContext()
                        , "Requesting for synchronization. Please wait.", Toast.LENGTH_LONG).show();
                syncProtocol.syncRequest(); // this is a Broadcast

                processParticipantsSync();
                //TODO Outputs an Log as HasMap, write this is a ListView !
                // TODO show a failed sync process in ListView

            }
        });
    }

    private HashMap<String, Boolean> processParticipantsSync() {
        HashMap<String, Boolean> syncLog = new HashMap<>();
        List<Participant> participantsList = contentProvider.getAllParticipants();
        HashMap<String, JSONObject> responseMap = fetchResponses();

        for(Participant participant : participantsList){
            String participantName = participant.getName();
            boolean status;
            if(responseMap.containsKey(participantName)){
                try {
                    syncParticipant(participantName);
                    status = true;
                }catch (SyncableDatabaseException e){
                    status = false;
                    Log.e(LOG_TAG, "Synchronization for Participant "+
                            participantName+" failed: " + e.getMessage());
                }
            }else{
                status = false;
            }
            syncLog.put(participantName,status);
        }
        return syncLog;
    }

    private void syncParticipant(String participantName) throws SyncableDatabaseException{
        // TODO yay finally you can sync it !
        // todo make an hashmap with name and response
    }


    private HashMap<String, JSONObject> fetchResponses(){
        long startTime = System.currentTimeMillis();
        long timeLimit = System.currentTimeMillis()+30000;
        getResponse = new Runnable() {
            @Override
            public void run() {
                setResponses(syncProtocol.receiveResponse());
            }
        };

        boolean fetching = true;

        while(fetching) {
            mThreadHandler.postDelayed(getResponse,2000);
            if (System.currentTimeMillis() - startTime > timeLimit) {
                fetching = false;
            }
        }
        mThreadHandler.removeCallbacks(getResponse);

        HashMap<String, JSONObject> responseMap = new HashMap<>();
        for (JSONObject response : responses){
            try {
                responseMap.put(response.get(KEY_NAME).toString(), response);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "JSONObject error for reading responses JSON: " + e.getMessage());
            }
        }
        return responseMap;
    }

    private void setResponses(List<JSONObject> responses){
        this.responses = responses;
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
}
