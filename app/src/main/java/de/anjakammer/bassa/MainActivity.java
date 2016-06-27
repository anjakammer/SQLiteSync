package de.anjakammer.bassa;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import android.view.inputmethod.InputMethodManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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

import de.anjakammer.bassa.model.Question;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private QuestionDataSource dataSource;
    private ListView mQuestionsListView;
    private ListView mDeletedQuestionsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "Das Datenquellen-Objekt wird angelegt.");
        dataSource = new QuestionDataSource(this);

        initializeQuestionsListView();
        initializeDeletedQuestionsListView();

        activateAddButton();
        initializeContextualActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();

        dataSource.open();

        showAllListEntries();
        showAllDeletedListEntries();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();
    }

    private void showAllListEntries () {
        List<Question> QuestionList = dataSource.getAllQuestions();
        ArrayAdapter<Question> adapter = (ArrayAdapter<Question>) mQuestionsListView.getAdapter();

        adapter.clear();
        adapter.addAll(QuestionList);
        adapter.notifyDataSetChanged();
    }

    private void showAllDeletedListEntries () {
        List<Question> DeletedQuestionList = dataSource.getAllDeletedQuestions();
        ArrayAdapter<Question> adapterForDeleted = (ArrayAdapter<Question>) mDeletedQuestionsListView.getAdapter();

        adapterForDeleted.clear();
        adapterForDeleted.addAll(DeletedQuestionList);
        adapterForDeleted.notifyDataSetChanged();
    }

    private void initializeQuestionsListView() {
        List<Question> emptyListForInitialization = new ArrayList<>();

        mQuestionsListView = (ListView) findViewById(R.id.listview_questions);

        // Erstellen des ArrayAdapters für unseren ListView
        ArrayAdapter<Question> QuestionArrayAdapter = new ArrayAdapter<Question> (
                this,
                android.R.layout.simple_list_item_activated_1,
                emptyListForInitialization) {
        };

        mQuestionsListView.setAdapter(QuestionArrayAdapter);

        mQuestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Question Question = (Question) adapterView.getItemAtPosition(position);

                // TODO Vorschau anzeigen / Question Preview
            }
        });
    }

    private void initializeDeletedQuestionsListView() {
        List<Question> emptyListForInitialization = new ArrayList<>();

        mDeletedQuestionsListView = (ListView) findViewById(R.id.listview_deleted_questions);
        // Erstellen des ArrayAdapters für unseren ListView
        ArrayAdapter<Question> DeletedQuestionArrayAdapter = new ArrayAdapter<Question> (
                this,
                android.R.layout.simple_list_item_activated_1,
                emptyListForInitialization) {

            // Wird immer dann aufgerufen, wenn der übergeordnete ListView die Zeile neu zeichnen muss
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =  super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textView.setTextColor(Color.rgb(175,175,175));
                return view;
            }
        };

        mDeletedQuestionsListView.setAdapter(DeletedQuestionArrayAdapter);

    }

    private void activateAddButton() {
        Button buttonAddProduct = (Button) findViewById(R.id.button_add_question);
        final EditText editTextTitle = (EditText) findViewById(R.id.editText_title);
        final EditText editTextDescription = (EditText) findViewById(R.id.editText_question);

        if(buttonAddProduct != null && editTextTitle != null && editTextDescription != null ) {

            buttonAddProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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

                    editTextTitle.setText("");
                    editTextDescription.setText("");

                    dataSource.createQuestion(description, title);

                    InputMethodManager inputMethodManager;
                    inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (getCurrentFocus() != null) {
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    }

                    showAllListEntries();
                }
            });
        }
    }

    private void initializeContextualActionBar() {

        final ListView QuestionsListView = (ListView) findViewById(R.id.listview_questions);
        QuestionsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        QuestionsListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            int selCount = 0;

            // In dieser Callback-Methode zählen wir die ausgewählen Listeneinträge mit
            // und fordern ein Aktualisieren der Contextual Action Bar mit invalidate() an
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

            // In dieser Callback-Methode reagieren wir auf den invalidate() Aufruf
            // Wir lassen das Edit-Symbol verschwinden, wenn mehr als 1 Eintrag ausgewählt ist
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

            // In dieser Callback-Methode reagieren wir auf Action Item-Klicks
            // Je nachdem ob das Löschen- oder Ändern-Symbol angeklickt wurde
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
                                dataSource.deleteQuestion(Question);
                            }
                        }
                        showAllListEntries();
                        showAllDeletedListEntries();
                        mode.finish();
                        break;

                    case R.id.cab_change:
                        Log.d(LOG_TAG, "Eintrag ändern");
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

            // In dieser Callback-Methode reagieren wir auf das Schließen der CAB
            // Wir setzen den Zähler auf 0 zurück
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selCount = 0;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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


                        // An dieser Stelle schreiben wir die geänderten Daten in die SQLite Datenbank
                        Question updatedQuestion = dataSource.updateQuestion(Question.getId(), description, title, Question.isDeleted());
                        Log.d(LOG_TAG, "Alter Eintrag - ID: " + Question.getId() + " Inhalt: " + Question.toString());
                        Log.d(LOG_TAG, "Neuer Eintrag - ID: " + updatedQuestion.getId() + " Inhalt: " + updatedQuestion.toString());

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