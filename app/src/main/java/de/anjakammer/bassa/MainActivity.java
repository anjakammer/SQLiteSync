package de.anjakammer.bassa;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.widget.AbsListView;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.AdapterView;

import de.anjakammer.bassa.model.Answer;
import de.anjakammer.bassa.model.Participant;
import de.anjakammer.bassa.model.Question;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ContentProvider contentProvider;
    private ListView mQuestionsListView;
    private ListView mAnswersListView;
    private ListView mDeletedQuestionsListView;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DetailFragment detailFragment = new DetailFragment();
        ListFragment listFragment = new ListFragment();

        contentProvider = new ContentProvider(this);
        initializeQuestionsListView();
        initializeDeletedQuestionsListView();
        activateAddButton();
        initializeContextualActionBar();
        initializeAnswersListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        contentProvider.open();
        showAllListEntries();
        showAllDeletedListEntries();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showAllListEntries() {
        List<Question> QuestionList = contentProvider.getAllQuestions();
        ArrayAdapter<Question> adapter = (ArrayAdapter<Question>) mQuestionsListView.getAdapter();

        adapter.clear();
        adapter.addAll(QuestionList);
        adapter.notifyDataSetChanged();
    }

    private void showAllDeletedListEntries() {
        List<Question> DeletedQuestionList = contentProvider.getAllDeletedQuestions();
        ArrayAdapter<Question> adapterForDeleted = (ArrayAdapter<Question>) mDeletedQuestionsListView.getAdapter();

        adapterForDeleted.clear();
        adapterForDeleted.addAll(DeletedQuestionList);
        adapterForDeleted.notifyDataSetChanged();
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

    private void initializeQuestionsListView() {

        List<Question> emptyListForInitialization = new ArrayList<>();
        mQuestionsListView = (ListView) findViewById(R.id.listview_questions);
        ArrayAdapter<Question> QuestionArrayAdapter = new ArrayAdapter<Question>(
                this,
                android.R.layout.simple_list_item_activated_1,
                emptyListForInitialization) {
        };
        mQuestionsListView.setAdapter(QuestionArrayAdapter);
        mQuestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Question question = (Question) adapterView.getItemAtPosition(position);
                TextView QuestionPlaceholder = (TextView) findViewById(R.id.question);
                String str = question.getTitle() + ": \n" + question.getDescription();
                QuestionPlaceholder.setText(str);

                List<Answer> AnswerList = question.getAnswers();
                ArrayAdapter<Answer> adapter = (ArrayAdapter<Answer>) mAnswersListView.getAdapter();

                adapter.clear();
                adapter.addAll(AnswerList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void initializeDeletedQuestionsListView() {
        List<Question> emptyListForInitialization = new ArrayList<>();
        mDeletedQuestionsListView = (ListView) findViewById(R.id.listview_deleted_questions);
        ArrayAdapter<Question> DeletedQuestionArrayAdapter = new ArrayAdapter<Question>(
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
        mDeletedQuestionsListView.setAdapter(DeletedQuestionArrayAdapter);
    }

    private void activateAddButton() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog AddQuestionDialog = createAddQuestionDialog();
                AddQuestionDialog.show();
            }
        });
    }

    private void initializeContextualActionBar() {
        final ListView QuestionsListView = (ListView) findViewById(R.id.listview_questions);
        QuestionsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        QuestionsListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            int selCount = 0;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    selCount++;
                } else {
                    selCount--;
                }
                String cabTitle = selCount + " " + getString(R.string.cab_checked_string);
                mode.setTitle(cabTitle);
                mode.invalidate();
            }

            // In dieser Callback-Methode legen wir die CAB-Menüeinträge an
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.cab_change);
                if (selCount == 1) {
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean returnValue = true;
                SparseBooleanArray touchedQuestionsPositions = QuestionsListView.getCheckedItemPositions();

                switch (item.getItemId()) {
                    case R.id.cab_delete:
                        for (int i = 0; i < touchedQuestionsPositions.size(); i++) {
                            boolean isChecked = touchedQuestionsPositions.valueAt(i);
                            if (isChecked) {
                                int postitionInListView = touchedQuestionsPositions.keyAt(i);
                                Question Question = (Question) QuestionsListView.getItemAtPosition(postitionInListView);
                                Log.d(LOG_TAG, "Position im ListView: " + postitionInListView + " Inhalt: " + Question.toString());
                                contentProvider.deleteQuestion(Question);
                            }
                        }
                        showAllListEntries();
                        showAllDeletedListEntries();
                        mode.finish();
                        break;
                    case R.id.cab_change:
                        for (int i = 0; i < touchedQuestionsPositions.size(); i++) {
                            boolean isChecked = touchedQuestionsPositions.valueAt(i);
                            if (isChecked) {
                                int postitionInListView = touchedQuestionsPositions.keyAt(i);
                                Question Question = (Question) QuestionsListView.getItemAtPosition(postitionInListView);
                                Log.d(LOG_TAG, "Position im ListView: " + postitionInListView + " Inhalt: " + Question.toString());

                                AlertDialog editQuestionDialog = createEditQuestionDialog(Question);
                                editQuestionDialog.show();
                            }
                        }
                        mode.finish();
                        break;
                    default:
                        returnValue = false;
                        break;
                }
                return returnValue;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selCount = 0;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AlertDialog createEditQuestionDialog(final Question Question) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogsView = inflater.inflate(R.layout.dialog_edit_question, null);

        final EditText editTextNewTitle = (EditText) dialogsView.findViewById(R.id.editText_new_title);
        editTextNewTitle.setText(Question.getTitle());

        final EditText editTextNewDescription = (EditText) dialogsView.findViewById(R.id.editText_new_question);
        editTextNewDescription.setText(Question.getDescription());

        builder.setView(dialogsView)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String title = editTextNewTitle.getText().toString();
                        String description = editTextNewDescription.getText().toString();

                        if ((TextUtils.isEmpty(title)) || (TextUtils.isEmpty(description))) {
                            Log.d(LOG_TAG, "Ein Eintrag enthielt keinen Text. Daher Abbruch der Änderung.");
                            return;
                        }

                        Question updatedQuestion = contentProvider.updateQuestion(Question.getId(), description, title);
                        showAllListEntries();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    private AlertDialog createAddQuestionDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogsView = inflater.inflate(R.layout.dialog_edit_question, null);

        final EditText editTextTitle = (EditText) dialogsView.findViewById(R.id.editText_new_title);

        final EditText editTextDescription = (EditText) dialogsView.findViewById(R.id.editText_new_question);

        final String[] participantsIds = contentProvider.getParticipantsIds();

        final List<Long> participants = new ArrayList<>();


        builder.setView(dialogsView)
                .setTitle(R.string.dialog_title)
                .setMultiChoiceItems(participantsIds, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                                if (isChecked) {
                                    participants.add(Long.valueOf(participantsIds[item]));

                                } else {
                                    participants.remove(Long.valueOf(participantsIds[item]));
                                }
                            }
                        })
                .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String title = editTextTitle.getText().toString();
                        String description = editTextDescription.getText().toString();

                        if (TextUtils.isEmpty(title)) {
                            editTextTitle.setError(getString(R.string.editText_errorMessage));
                            return;
                        }
                        if (TextUtils.isEmpty(description)) {
                            editTextDescription.setError(getString(R.string.editText_errorMessage));
                            return;
                        }
                        contentProvider.createQuestion(description, title, participants);
                        showAllListEntries();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}