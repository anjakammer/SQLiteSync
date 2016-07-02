package de.anjakammer.bassa;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;
import android.database.Cursor;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.anjakammer.bassa.model.Answer;
import de.anjakammer.bassa.model.Question;


public class QuestionDataSource {

    private static final String LOG_TAG = QuestionDataSource.class.getSimpleName();
    private DBHandler dbHandler;

//    private String[] questionColumns = {
//            DBHandler.COLUMN_ID,
//            DBHandler.COLUMN_DESCRIPTION,
//            DBHandler.COLUMN_TITLE
//    };
//
//    private String[] answerColumns = {
//            DBHandler.COLUMN_A_ID,
//            DBHandler.COLUMN_A_DESCRIPTION,
//            DBHandler.COLUMN_A_PARTICIPANT,
//            DBHandler.COLUMN_A_QUESTION_ID
//    };


    public QuestionDataSource(Context context) {
        dbHandler = new DBHandler(context);
    }

    // TODO remove this testing method
    private void insertFakeAnswers(long question_id){
        try{
            createAnswer("not answered yet", "Peer1", question_id);
            createAnswer("not answered yet", "Peer2", question_id);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, "createAnswer failed: " + e.getMessage());
        }
    }

    public void open() {
        dbHandler.getWritableDatabase();
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

        long insertId = dbHandler.insert(DBHandler.TABLE_ANSWERS, values);

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_ANSWERS,
                DBHandler.ANSWER_COLUMNS, DBHandler.COLUMN_ID + " = ?",
                new String[]{String.valueOf(insertId)},
                null, null, null, null);

        cursor.moveToFirst();
        Answer answer = cursorToAnswer(cursor);
        cursor.close();
        Log.d(LOG_TAG, "Answer saved! " + answer.toString());
        return answer;
    }

    public Question createQuestion(String description, String title) {
        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_DESCRIPTION, description);
        values.put(DBHandler.COLUMN_TITLE, title);

        long insertId = dbHandler.insert(DBHandler.TABLE_QUESTIONNAIRE, values);

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_QUESTIONNAIRE,
                DBHandler.QUESTION_COLUMNS, DBHandler.COLUMN_ID + " = ?",
                new String[]{String.valueOf(insertId)},
                null, null, null, null);

        cursor.moveToFirst();
        Question question = cursorToQuestion(cursor);
        cursor.close();
        Log.d(LOG_TAG, "Question saved! " + question.toString());
        return question;
    }

    public void deleteQuestion(Question Question) {
        long _id = Question.getId();
        dbHandler.delete(DBHandler.TABLE_QUESTIONNAIRE, _id);
    }

    public Question updateQuestion(long _id, String newQuestion, String newTitle, boolean newIsDeleted) {
        int intValueIsDeleted = (newIsDeleted) ? 1 : 0;

        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_DESCRIPTION, newQuestion);
        values.put(DBHandler.COLUMN_TITLE, newTitle);
        values.put(DBHandler.COLUMN_ISDELETED, intValueIsDeleted);

        dbHandler.update(DBHandler.TABLE_QUESTIONNAIRE, _id, values);

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_QUESTIONNAIRE,
                DBHandler.QUESTION_COLUMNS, DBHandler.COLUMN_ID + " = ?",
                new String[]{String.valueOf(_id)},
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

        String description = cursor.getString(idQuestion);
        String title = cursor.getString(idTitle);
        long id = cursor.getLong(idIndex);

        Question question = new Question(description, title, id);
        //todo test, please remove this
        insertFakeAnswers(id);
        // todo test, please remove this
        question.setAnswers(getRelatedAnswers(id));
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

        String whereIsDeleted = "isDeleted = ?"; // TODO encapsulation needed !
        String[] isFalse = new String[] {"0"};

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_QUESTIONNAIRE,
                DBHandler.QUESTION_COLUMNS, whereIsDeleted,
                isFalse,
                null, null, null, null);

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



        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_ANSWERS,
                    DBHandler.ANSWER_COLUMNS, whereQuestionID,
                    questionID,
                    null, null, null, null);
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

        String whereIsDeleted = "isDeleted = ?"; // TODO encapsulation needed !
        String[] isFalse = new String[] {"1"};

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_QUESTIONNAIRE,
                DBHandler.QUESTION_COLUMNS, whereIsDeleted,
                isFalse,
                null, null, null, null);

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
