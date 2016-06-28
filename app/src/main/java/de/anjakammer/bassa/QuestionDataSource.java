package de.anjakammer.bassa;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.anjakammer.bassa.model.Answer;
import de.anjakammer.bassa.model.Question;


public class QuestionDataSource {

    private static final String LOG_TAG = QuestionDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private DBHandler dbHandler;

    private String[] questionColumns = {
            DBHandler.COLUMN_ID,
            DBHandler.COLUMN_DESCRIPTION,
            DBHandler.COLUMN_TITLE,
            DBHandler.COLUMN_ISDELETED
    };

    private String[] answerColumns = {
            DBHandler.COLUMN_A_ID,
            DBHandler.COLUMN_A_DESCRIPTION,
            DBHandler.COLUMN_A_PARTICIPANT,
            DBHandler.COLUMN_A_QUESTION_ID
    };


    public QuestionDataSource(Context context) {
        dbHandler = new DBHandler(context);
    }

    // TODO remove this testing method
    private void insertFakeAnswers(long question_id){
        try{
            createAnswer("first Answer", "Peer1", question_id);
            createAnswer("second Answer", "Peer2", question_id);
        }catch (Exception e){
            Log.e(LOG_TAG, "createAnswer failed.");
        }
    }

    public void open() {
        database = dbHandler.getWritableDatabase();
        Log.d(LOG_TAG, "Path to database-.: " + database.getPath());
    }

    public void close() {
        dbHandler.close();
        Log.d(LOG_TAG, "closed database.");
    }

    public Answer createAnswer(String description, String participant, long question_id) {
        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_A_DESCRIPTION, description);
        values.put(DBHandler.COLUMN_A_PARTICIPANT, participant);
        values.put(DBHandler.COLUMN_A_QUESTION_ID, question_id);

        long insertId = database.insert(DBHandler.TABLE_ANSWERS, null, values);

        Cursor cursor = database.query(DBHandler.TABLE_ANSWERS,
                answerColumns, DBHandler.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Answer answer = cursorToAnswer(cursor);
        cursor.close();
        Log.d(LOG_TAG, "Antwort  saved! " + answer.toString());
        return answer;
    }

    public Question createQuestion(String description, String title) {
        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_DESCRIPTION, description);
        values.put(DBHandler.COLUMN_TITLE, title);

        long insertId = database.insert(DBHandler.TABLE_QUESTIONNAIRE, null, values);

        Cursor cursor = database.query(DBHandler.TABLE_QUESTIONNAIRE,
                questionColumns, DBHandler.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Question question = cursorToQuestion(cursor);
        cursor.close();
        Log.d(LOG_TAG, "Question  saved! " + question.toString());
        return question;
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
                questionColumns, DBHandler.COLUMN_ID + "=" + id,
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

        Question question = new Question(description, title, id, isDeleted);
        //todo test, please remove this
//        insertFakeAnswers(id);
        // todo test, please remove this
        question.setAnswers(getRelatedAnswers(id));
        Log.d(LOG_TAG, "Mit Antwort " + id + " Inhalt: " + question.toString());
        return question;
    }
    private Answer cursorToAnswer(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DBHandler.COLUMN_A_ID);
        int idDescription = cursor.getColumnIndex(DBHandler.COLUMN_A_DESCRIPTION);
        int idParticipant = cursor.getColumnIndex(DBHandler.COLUMN_A_PARTICIPANT);
        int idQuestionID = cursor.getColumnIndex(DBHandler.COLUMN_A_QUESTION_ID);

        String description = cursor.getString(idDescription);
        String participant = cursor.getString(idParticipant);
        long id = cursor.getLong(idIndex);
        long question_id = cursor.getLong(idQuestionID);

        return new Answer(description, participant, id, question_id);
    }

    public List<Question> getAllQuestions() {
        List<Question> QuestionList = new ArrayList<>();

        String whereIsDeleted = "isDeleted = ?";
        String[] isFalse = new String[] {"0"};

        Cursor cursor = database.query(DBHandler.TABLE_QUESTIONNAIRE,
                questionColumns, whereIsDeleted, isFalse, null, null, null);

        cursor.moveToFirst();
        Question question;

        while (!cursor.isAfterLast()) {
            question = cursorToQuestion(cursor);
            QuestionList.add(question);
            cursor.moveToNext();
        }

        cursor.close();

        return QuestionList;
    }

    public List<Answer> getRelatedAnswers(long questionId) {
        List<Answer> AnswerList = new ArrayList<>();

        String whereQuestionID = "question_id = ?";
        String[] questionID = new String[] {String.valueOf(questionId)};

        Cursor cursor = database.query(DBHandler.TABLE_ANSWERS,
                answerColumns, whereQuestionID, questionID, null, null, null);
        cursor.moveToFirst();
        Answer answer;

        while (!cursor.isAfterLast()) {
            answer = cursorToAnswer(cursor);
            AnswerList.add(answer);
            cursor.moveToNext();
        }

        cursor.close();
        return AnswerList;
    }

    public List<Question> getAllDeletedQuestions() {
        List<Question> QuestionList = new ArrayList<>();

        String whereIsDeleted = "isDeleted = ?";
        String[] isFalse = new String[] {"1"};

        Cursor cursor = database.query(DBHandler.TABLE_QUESTIONNAIRE,
                questionColumns, whereIsDeleted, isFalse, null, null, null);

        cursor.moveToFirst();
        Question question;

        while (!cursor.isAfterLast()) {
            question = cursorToQuestion(cursor);
            QuestionList.add(question);
            cursor.moveToNext();
        }

        cursor.close();

        return QuestionList;
    }
}