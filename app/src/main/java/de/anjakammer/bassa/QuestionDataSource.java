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
    private DBHandler dbHandler;

    private String[] columns = {
            DBHandler.COLUMN_ID,
            DBHandler.COLUMN_DESCRIPTION,
            DBHandler.COLUMN_TITLE,
            DBHandler.COLUMN_ISDELETED
    };


    public QuestionDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHandler.");
        dbHandler = new DBHandler(context);
    }

    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHandler.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHandler.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public Question createQuestion(String description, String title) {
        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_DESCRIPTION, description);
        values.put(DBHandler.COLUMN_TITLE, title);

        long insertId = database.insert(DBHandler.TABLE_QUESTIONNAIRE, null, values);

        Cursor cursor = database.query(DBHandler.TABLE_QUESTIONNAIRE,
                columns, DBHandler.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Question Question = cursorToQuestion(cursor);
        cursor.close();
        Log.d(LOG_TAG, "Eintrag  gespeichert! " + Question.toString());
        return Question;
    }

    public void deleteQuestion(Question Question) {
        long id = Question.getId();
        int intValueIsDeleted = 1;

        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_ISDELETED, intValueIsDeleted);

        database.update(DBHandler.TABLE_QUESTIONNAIRE,
                values,
                DBHandler.COLUMN_ID + "=" + id,
                null);

        Log.d(LOG_TAG, "Eintrag als gelöscht gespeichert! ID: " + id + " Inhalt: " + Question.toString());
    }

    public Question updateQuestion(long id, String newQuestion, String newTitle, boolean newIsDeleted) {
        int intValueIsDeleted = (newIsDeleted) ? 1 : 0;

        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_DESCRIPTION, newQuestion);
        values.put(DBHandler.COLUMN_TITLE, newTitle);
        values.put(DBHandler.COLUMN_ISDELETED, intValueIsDeleted);

        database.update(DBHandler.TABLE_QUESTIONNAIRE,
                values,
                DBHandler.COLUMN_ID + "=" + id,
                null);

        Cursor cursor = database.query(DBHandler.TABLE_QUESTIONNAIRE,
                columns, DBHandler.COLUMN_ID + "=" + id,
                null, null, null, null);

        cursor.moveToFirst();
        Question Question = cursorToQuestion(cursor);
        cursor.close();

        return Question;
    }

    private Question cursorToQuestion(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DBHandler.COLUMN_ID);
        int idQuestion = cursor.getColumnIndex(DBHandler.COLUMN_DESCRIPTION);
        int idTitle = cursor.getColumnIndex(DBHandler.COLUMN_TITLE);
        int idIsDeleted = cursor.getColumnIndex(DBHandler.COLUMN_ISDELETED);

        String description = cursor.getString(idQuestion);
        String title = cursor.getString(idTitle);
        long id = cursor.getLong(idIndex);
        int intValueIsDeleted = cursor.getInt(idIsDeleted);

        boolean isDeleted = (intValueIsDeleted != 0);

        return new Question(description, title, id, isDeleted);
    }


    public List<Question> getAllQuestions() {
        List<Question> QuestionList = new ArrayList<>();

        String whereIsDeleted = "isDeleted = ?";
        String[] isFalse = new String[] {"0"};

        Cursor cursor = database.query(DBHandler.TABLE_QUESTIONNAIRE,
                columns, whereIsDeleted, isFalse, null, null, null);

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

    public List<Question> getAllDeletedQuestions() {
        List<Question> QuestionList = new ArrayList<>();

        String whereIsDeleted = "isDeleted = ?";
        String[] isFalse = new String[] {"1"};

        Cursor cursor = database.query(DBHandler.TABLE_QUESTIONNAIRE,
                columns, whereIsDeleted, isFalse, null, null, null);

        cursor.moveToFirst();
        Question Question;

        while (!cursor.isAfterLast()) {
            Question = cursorToQuestion(cursor);
            QuestionList.add(Question);
            Log.d(LOG_TAG, "ID: " + Question.getId() + ",gelöschter Inhalt: " + Question.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return QuestionList;
    }
}