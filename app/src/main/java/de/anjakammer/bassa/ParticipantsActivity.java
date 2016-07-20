package de.anjakammer.bassa;


import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.anjakammer.bassa.model.Answer;
import de.anjakammer.bassa.model.Participant;
import de.anjakammer.bassa.model.Question;

public class ParticipantsActivity extends AppCompatActivity {

    public static final String LOG_TAG = ParticipantsActivity.class.getSimpleName();
    private ContentProvider contentProvider;
    private ListView mParticipantsListView;
    private ListView mDeletedParticipantsListView;
    private ListView mAnswersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DetailFragment detailFragment = new DetailFragment();
        ListFragment listFragment = new ListFragment();

        contentProvider = new ContentProvider(this);

        initializeParticipantsListView();
        initializeAnswersListView();
        initializeDeletedParticipantsListView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        contentProvider.open();
        showAllListEntries();
        showAllDeletedListEntries();
    }

    private void showAllListEntries() {
        List<Participant> ParticipantsList = contentProvider.getAllParticipants();
        ArrayAdapter<Participant> adapter = (ArrayAdapter<Participant>) mParticipantsListView.getAdapter();

        adapter.clear();
        adapter.addAll(ParticipantsList);
        adapter.notifyDataSetChanged();
    }

    private void showAllDeletedListEntries() {
        List<Participant> DeletedParticipantsList = contentProvider.getAllDeletedParticipants();
        ArrayAdapter<Participant> adapterForDeleted = (ArrayAdapter<Participant>) mDeletedParticipantsListView.getAdapter();

        adapterForDeleted.clear();
        adapterForDeleted.addAll(DeletedParticipantsList);
        adapterForDeleted.notifyDataSetChanged();
    }
    private void initializeParticipantsListView() {

        List<Participant> emptyListForInitialization = new ArrayList<>();
        mParticipantsListView = (ListView) findViewById(R.id.listview_items);
        TextView mParticipantsHeadline = (TextView) findViewById(R.id.headline);
        mParticipantsHeadline.setText(R.string.headline_participants);
        ArrayAdapter<Participant> ParticipantsArrayAdapter = new ArrayAdapter<Participant>(
                this,
                android.R.layout.simple_list_item_activated_1,
                emptyListForInitialization) {
        };
        mParticipantsListView.setAdapter(ParticipantsArrayAdapter);
        mParticipantsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Participant participant = (Participant) adapterView.getItemAtPosition(position);
                TextView ParticipantPlaceholder = (TextView) findViewById(R.id.item);
                String str = participant.getName() + ": \n" + participant.getAddress();
                ParticipantPlaceholder.setText(str);
                setmAnswersListView(participant);
            }
        });
    }
    private void initializeDeletedParticipantsListView() {
        List<Participant> emptyListForInitialization = new ArrayList<>();
        mDeletedParticipantsListView = (ListView) findViewById(R.id.listview_deleted_items);
        TextView mDeletedParticipantsHeadline = (TextView) findViewById(R.id.headline_deleted);
        mDeletedParticipantsHeadline.setText(R.string.headline_deleted_questions);
        ArrayAdapter<Participant> DeletedParticipantsArrayAdapter = new ArrayAdapter<Participant>(
                this,
                android.R.layout.simple_list_item_activated_1,
                emptyListForInitialization) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textView.setTextColor(Color.rgb(175, 175, 175));
                return view;
            }
        };
        mDeletedParticipantsListView.setAdapter(DeletedParticipantsArrayAdapter);
    }
    private void initializeAnswersListView() {
        List<Answer> emptyListForInitialization = new ArrayList<>();
        mAnswersListView = (ListView) findViewById(R.id.listview_answers);
        ArrayAdapter<Answer> AnswersArrayAdapter = new ArrayAdapter<Answer>(
                this,
                android.R.layout.simple_list_item_activated_1,
                emptyListForInitialization) {
        };
        mAnswersListView.setAdapter(AnswersArrayAdapter);
    }
    private void setmAnswersListView(Participant participant){
        List<Answer> AnswerList = contentProvider.getAnswersOfParticipant(participant.getId());
        ArrayAdapter<Answer> adapter = (ArrayAdapter<Answer>) mAnswersListView.getAdapter();

        adapter.clear();
        adapter.addAll(AnswerList);
        adapter.notifyDataSetChanged();
    }
}
