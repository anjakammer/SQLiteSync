package de.anjakammer.bassa;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;


public class QuestionDataSource {

    private static final String LOG_TAG = QuestionDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private QuestionDbHelper dbHelper;

    private String[] columns = {
            QuestionDbHelper.COLUMN_ID,
            QuestionDbHelper.COLUMN_QUESTION,
            QuestionDbHelper.COLUMN_TITLE,
            QuestionDbHelper.COLUMN_ISDELETED
    };


    public QuestionDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new QuestionDbHelper(context);
    }

    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public Question createQuestion(String question, String title) {
        ContentValues values = new ContentValues();
        values.put(QuestionDbHelper.COLUMN_QUESTION, question);
        values.put(QuestionDbHelper.COLUMN_TITLE, title);

        long insertId = database.insert(QuestionDbHelper.TABLE_QUESTIONNAIRE, null, values);

        Cursor cursor = database.query(QuestionDbHelper.TABLE_QUESTIONNAIRE,
                columns, QuestionDbHelper.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Question Question = cursorToQuestion(cursor);
        cursor.close();

        return Question;
    }

    public void deleteQuestion(Question Question) {
        long id = Question.getId();

        database.delete(QuestionDbHelper.TABLE_QUESTIONNAIRE,
                QuestionDbHelper.COLUMN_ID + "=" + id,
                null);

        Log.d(LOG_TAG, "Eintrag gelöscht! ID: " + id + " Inhalt: " + Question.toString());
    }

    public Question updateQuestion(long id, String newQuestion, String newTitle, boolean newIsDeleted) {
        int intValueIsDeleted = (newIsDeleted) ? 1 : 0;

        ContentValues values = new ContentValues();
        values.put(QuestionDbHelper.COLUMN_QUESTION, newQuestion);
        values.put(QuestionDbHelper.COLUMN_TITLE, newTitle);
        values.put(QuestionDbHelper.COLUMN_ISDELETED, intValueIsDeleted);

        database.update(QuestionDbHelper.TABLE_QUESTIONNAIRE,
                values,
                QuestionDbHelper.COLUMN_ID + "=" + id,
                null);

        Cursor cursor = database.query(QuestionDbHelper.TABLE_QUESTIONNAIRE,
                columns, QuestionDbHelper.COLUMN_ID + "=" + id,
                null, null, null, null);

        cursor.moveToFirst();
        Question Question = cursorToQuestion(cursor);
        cursor.close();

        return Question;
    }

    private Question cursorToQuestion(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(QuestionDbHelper.COLUMN_ID);
        int idQuestion = cursor.getColumnIndex(QuestionDbHelper.COLUMN_QUESTION);
        int idTitle = cursor.getColumnIndex(QuestionDbHelper.COLUMN_TITLE);
        int idIsDeleted = cursor.getColumnIndex(QuestionDbHelper.COLUMN_ISDELETED);

        String question = cursor.getString(idQuestion);
        String title = cursor.getString(idTitle);
        long id = cursor.getLong(idIndex);
        int intValueChecked = cursor.getInt(idIsDeleted);

        boolean isDeleted = (intValueChecked != 0);

        return new Question(question, title, id, isDeleted);
    }


    public List<Question> getAllQuestions() {
        List<Question> QuestionList = new ArrayList<>();

        Cursor cursor = database.query(QuestionDbHelper.TABLE_QUESTIONNAIRE,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Question Question;

        while (!cursor.isAfterLast()) {
            Question = cursorToQuestion(cursor);
            QuestionList.add(Question);
            Log.d(LOG_TAG, "ID: " + Question.getId() + ", Inhalt: " + Question.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return QuestionList;
    }
}