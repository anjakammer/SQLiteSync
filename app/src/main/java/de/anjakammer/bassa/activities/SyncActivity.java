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

import java.util.ArrayList;
import java.util.List;

import de.anjakammer.bassa.ContentProvider;
import de.anjakammer.bassa.R;
import de.anjakammer.bassa.commService.SyncProtocol;
import de.anjakammer.bassa.models.Participant;
import de.anjakammer.bassa.models.Question;


/**
 * uses the SyncProtocol
 */


public class SyncActivity extends AppCompatActivity {

    private Handler mThreadHandler;
    private Runnable mThread;
    private ContentProvider contentProvider;
    private ListView mParticipantsListView;
    private SyncProtocol syncProtocol;
    private List<Participant> participantsList;

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
        syncProtocol = new SyncProtocol(
                contentProvider.getProfileName(),contentProvider.getDbId(), mContext);

        mThreadHandler = new Handler();
        mThread = new Runnable() {
            @Override
            public void run() {
                participantsList.clear();

                for(String participantName : syncProtocol.getPeers()){
                    Participant participant = contentProvider.getParticipantByName(participantName);
                    // TODO ID should be -1 for unknown Peers
                    if(participant.getId() == -1){
                        // TODO what about the address?
                        participantsList.add(
                                contentProvider.createParticipant("", participantName, false));
                    }else{
                        participantsList.add(participant);
                    }
                }

                // TODO remove this test-injection
                // TODO create Participants if they appear
                participantsList.add(new Participant("address","name", -1, false ));
                // TODO remove this test-injection

                showAllListEntries();
                mThreadHandler.postDelayed(this, 5000);
            }
        };

        mThreadHandler.post(mThread);
    }

    @Override
    public void onResume() {
        mThreadHandler.post(mThread);
        super.onResume();
    }

    @Override
    public void onPause() {
        mThreadHandler.removeCallbacks(mThread);
        super.onPause();
    }

    private void createSyncButton(){
        final Button button = (Button) findViewById(R.id.button_set_peers_and_sync);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
                    // TODO implement Synchronization here
                    Toast.makeText(getApplicationContext()
                            , "Participant where set, sync was not triggered", Toast.LENGTH_LONG).show();
                }
            }
        });
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
