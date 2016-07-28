package de.anjakammer.bassa;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.anjakammer.bassa.CommService.SyncProtocol;
import de.anjakammer.bassa.model.Participant;


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
        participantsList = new ArrayList<>();
        initializeParticipantsListView();
        showAllListEntries();

        Context mContext = getApplicationContext();
        contentProvider = new ContentProvider(mContext);
        syncProtocol = new SyncProtocol(contentProvider.getDbId(), mContext);
        mThreadHandler = new Handler();
        mThread = new Runnable() {
            @Override
            public void run() {
                // TODO refresh the mParticipantsListView
                participantsList =
                        contentProvider.getParticipantsByName(syncProtocol.getPeers());
                mThreadHandler.postDelayed(this, 5000);
            }
        };
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

    private void initializeParticipantsListView() {
        List<Participant> emptyListForInitialization = new ArrayList<>();
        mParticipantsListView = (ListView) findViewById(R.id.listview_items);
        TextView mParticipantsHeadline = (TextView) findViewById(R.id.headline);
        mParticipantsHeadline.setText(R.string.headline_active_participants);
        ArrayAdapter<Participant> ParticipantsArrayAdapter = new ArrayAdapter<Participant>(
                this,
                android.R.layout.simple_list_item_activated_1,
                emptyListForInitialization) {
        };
        mParticipantsListView.setAdapter(ParticipantsArrayAdapter);
    }

    private void showAllListEntries() {
        ArrayAdapter<Participant> adapter = (ArrayAdapter<Participant>) mParticipantsListView.getAdapter();
        // TODO remove this test-injection
        participantsList.add(new Participant("address","name", 39));
        // TODO remove this test-injection

        adapter.clear();
        adapter.addAll(participantsList);
        adapter.notifyDataSetChanged();
    }
}
