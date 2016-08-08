package de.anjakammer.bassa;

import android.content.Context;
import android.util.Log;
import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.anjakammer.bassa.models.Answer;
import de.anjakammer.bassa.models.Participant;
import de.anjakammer.bassa.models.Question;
import de.anjakammer.sqlitesync.exceptions.SyncableDatabaseException;


public class ContentProvider {

    private static final String LOG_TAG = ContentProvider.class.getSimpleName();

    private DBHandler dbHandler;

    public ContentProvider(Context context) {
        dbHandler = new DBHandler(context);
    }

    public void open() {
        dbHandler.getWritableDatabase();
    }

    public String getDbId(){
        return DBHandler.DB_ID;
    }

    public String getProfileName() {
        return dbHandler.getProfileName();
    }

    public void setProfileName(String newName){
        dbHandler.setProfileName(newName);
    }

    public Answer createAnswer(String description, long participantId, long question_id) {
        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_A_DESCRIPTION, description);
        values.put(DBHandler.COLUMN_A_PARTICIPANT_ID, participantId);
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

    public Participant createParticipant(String name, Boolean isSelected) {
        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_P_NAME, name);
        values.put(DBHandler.COLUMN_P_IS_SELECTED, (isSelected) ? 1 : 0);

        long insertId = dbHandler.insert(DBHandler.TABLE_PARTICIPANTS, values);

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_PARTICIPANTS,
                DBHandler.PARTICIPANTS_COLUMNS, DBHandler.COLUMN_ID + " = ?",
                new String[]{String.valueOf(insertId)},
                null, null, null, null);

        cursor.moveToFirst();
        Participant participant = cursorToParticipant(cursor);
        cursor.close();
        Log.d(LOG_TAG, "Participant saved! " + participant.toString());
        return participant;
    }

    public Question createQuestion(String description, String title) {
        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_DESCRIPTION, description);
        values.put(DBHandler.COLUMN_TITLE, title);

        long insertId = dbHandler.insert(DBHandler.TABLE_QUESTIONS, values);

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_QUESTIONS,
                DBHandler.QUESTION_COLUMNS, DBHandler.COLUMN_ID + " = ?",
                new String[]{String.valueOf(insertId)},
                null, null, null, null);

        cursor.moveToFirst();
        Question question = cursorToQuestion(cursor);
        cursor.close();
        Log.d(LOG_TAG, "Question saved! " + question.toString());
        return question;
    }

    public void deleteQuestion(Question question) {
        long _id = question.getId();
        dbHandler.delete(DBHandler.TABLE_QUESTIONS, _id);

        List<Answer> answers = getRelatedAnswers(_id);
        for(Answer answer : answers){
            deleteAnswer(answer);
        }
    }

    public void deleteAnswer(Answer answer) {
        long _id = answer.getId();
        dbHandler.delete(DBHandler.TABLE_ANSWERS, _id);
    }

    public void deleteAnswers(long questionId) {

        Cursor answers = dbHandler.select(false,DBHandler.TABLE_ANSWERS,
                new String[]{DBHandler.COLUMN_A_ID},
                DBHandler.COLUMN_A_QUESTION_ID +" = ?",
                new String[]{String.valueOf(questionId)},
                null, null, null, null);
        answers.moveToFirst();

        while (!answers.isAfterLast()) {
            Long answerId = answers.getLong(answers.getColumnIndex(DBHandler.COLUMN_A_ID));
            dbHandler.delete(DBHandler.TABLE_ANSWERS,answerId);
            answers.moveToNext();
        }
        answers.close();
    }

    public Question updateQuestion(long _id, String newQuestion, String newTitle) {

        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_DESCRIPTION, newQuestion);
        values.put(DBHandler.COLUMN_TITLE, newTitle);

        dbHandler.update(DBHandler.TABLE_QUESTIONS, _id, values);

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_QUESTIONS,
                DBHandler.QUESTION_COLUMNS, DBHandler.COLUMN_ID + " = ?",
                new String[]{String.valueOf(_id)},
                null, null, null, null);

        cursor.moveToFirst();
        Question question = cursorToQuestion(cursor);
        cursor.close();

        return question;
    }

    private Question cursorToQuestion(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DBHandler.COLUMN_ID);
        int idQuestion = cursor.getColumnIndex(DBHandler.COLUMN_DESCRIPTION);
        int idTitle = cursor.getColumnIndex(DBHandler.COLUMN_TITLE);

        String description = cursor.getString(idQuestion);
        String title = cursor.getString(idTitle);
        long id = cursor.getLong(idIndex);

        Question question = new Question(description, title, id);
        question.setAnswers(getRelatedAnswers(id));
        return question;
    }

    private Answer cursorToAnswer(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DBHandler.COLUMN_A_ID);
        int idDescription = cursor.getColumnIndex(DBHandler.COLUMN_A_DESCRIPTION);
        int idParticipant = cursor.getColumnIndex(DBHandler.COLUMN_A_PARTICIPANT_ID);
        int idQuestionID = cursor.getColumnIndex(DBHandler.COLUMN_A_QUESTION_ID);

        String description = cursor.getString(idDescription);
        long participant = cursor.getLong(idParticipant);
        long id = cursor.getLong(idIndex);
        long question_id = cursor.getLong(idQuestionID);
        return new Answer(description, participant, id, question_id);
    }

    private Participant cursorToParticipant(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DBHandler.COLUMN_P_ID);
        int idName = cursor.getColumnIndex(DBHandler.COLUMN_P_NAME);
        int idIsSelected = cursor.getColumnIndex(DBHandler.COLUMN_P_IS_SELECTED);

        String name = cursor.getString(idName);
        long id = cursor.getLong(idIndex);
        boolean isSelected = cursor.getLong(idIsSelected)== 1;
        return new Participant(name, id, isSelected);
    }

    public Participant updateParticipant(long _id, String name, boolean isSelected) {
        ContentValues values = new ContentValues();
        values.put(DBHandler.COLUMN_P_NAME, name);
        values.put(DBHandler.COLUMN_P_IS_SELECTED, (isSelected) ? 1 : 0);

        dbHandler.update(DBHandler.TABLE_PARTICIPANTS, _id, values);

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_PARTICIPANTS,
                DBHandler.PARTICIPANTS_COLUMNS, DBHandler.COLUMN_P_ID + " = ?",
                new String[]{String.valueOf(_id)},
                null, null, null, null);

        cursor.moveToFirst();
        Participant participant = cursorToParticipant(cursor);
        cursor.close();

        return participant;
    }

    public List<Participant> getAllParticipants() {
        List<Participant> participantList = new ArrayList<>();

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_PARTICIPANTS,
                DBHandler.PARTICIPANTS_COLUMNS, DBHandler.COLUMN_P_IS_SELECTED+" = ?",
                new String[]{"1"}, null, null, null, null);

        cursor.moveToFirst();
        Participant participant;

        while (!cursor.isAfterLast()) {
            participant = cursorToParticipant(cursor);
            participantList.add(participant);
            cursor.moveToNext();
        }

        cursor.close();
        return participantList;
    }

    public String[] getParticipantsIds(){
        List<Participant> participantList = getAllParticipants();
        String[] participantsArray = new String[participantList.size()];
        int i = 0;
        for (Participant participant: participantList) {
            participantsArray[i] = String.valueOf(participant.getId());
            i++;
        }
        return participantsArray;
    }

    public List<Participant> getParticipantsByName(List<String> names){
        List<Participant> participantList = new ArrayList<>();
        for (String name: names) {

            Cursor cursor = dbHandler.select(false, DBHandler.TABLE_PARTICIPANTS,
                    DBHandler.PARTICIPANTS_COLUMNS, DBHandler.COLUMN_P_NAME + " = ?",
                    new String[] {name},
                    null, null, null, null);
            cursor.moveToFirst();
            Participant participant;
            while (!cursor.isAfterLast()) {
                participant = cursorToParticipant(cursor);
                participantList.add(participant);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return participantList;
    }

    public Participant getParticipantByName(String name){
        Participant participant = null;
        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_PARTICIPANTS,
                DBHandler.PARTICIPANTS_COLUMNS, DBHandler.COLUMN_P_NAME + " = ?",
                new String[] {name},
                null, null, null, null);
        cursor.moveToFirst();
        if(cursor.getCount() < 1){
            participant = new Participant(name, -1, false);
        }else{
            participant = cursorToParticipant(cursor);
        }
        cursor.close();
        return participant;
    }

    public List<Question> getAllQuestions() {
        List<Question> QuestionList = new ArrayList<>();


        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_QUESTIONS,
                DBHandler.QUESTION_COLUMNS, null, null, null, null, null, null);

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

        String whereQuestionID = DBHandler.COLUMN_A_QUESTION_ID + " = ?";
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

    public List<Answer> getAnswersOfParticipant(long participantId) {
        List<Answer> AnswerList = new ArrayList<>();

        Cursor cursor = dbHandler.select(false, DBHandler.TABLE_ANSWERS,
                DBHandler.ANSWER_COLUMNS, DBHandler.COLUMN_A_PARTICIPANT_ID + " = ?",
                new String[] {String.valueOf(participantId)},
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

        Cursor cursor = dbHandler.selectDeleted(false, DBHandler.TABLE_QUESTIONS,
                DBHandler.QUESTION_COLUMNS, null, null, null, null, null, null);

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

    public List<Participant> getAllDeletedParticipants() {
        List<Participant> ParticipantList = new ArrayList<>();

        Cursor cursor = dbHandler.selectDeleted(false, DBHandler.TABLE_PARTICIPANTS,
                DBHandler.PARTICIPANTS_COLUMNS, null, null, null, null, null, null);

        cursor.moveToFirst();
        Participant participant;

        while (!cursor.isAfterLast()) {
            participant = cursorToParticipant(cursor);
            ParticipantList.add(participant);
            cursor.moveToNext();
        }
        cursor.close();
        return ParticipantList;
    }

    public void setParticipant(Participant participant, boolean isSelected) {
        long participantId = participant.getId();

        if(isSelected) {
            List<Question> questionList = getAllQuestions();
            for (Question question : questionList) {
                long questionId = question.getId();

                ContentValues answerValues = new ContentValues();
                answerValues.put(DBHandler.COLUMN_A_DESCRIPTION, "");
                answerValues.put(DBHandler.COLUMN_A_QUESTION_ID, questionId);
                answerValues.put(DBHandler.COLUMN_A_PARTICIPANT_ID, participantId);
                dbHandler.insertIfNotExists(DBHandler.TABLE_ANSWERS, answerValues);
                updateParticipant(participantId, participant.getName(), true);
            }
        }else{
            List<Answer> answerList = getAnswersOfParticipant(participantId);
            for (Answer answer : answerList) {
                dbHandler.delete(DBHandler.TABLE_ANSWERS, answer.getId());
            }
            updateParticipant(participantId, participant.getName(), false);
        }
    }

    public JSONObject getUpdate(JSONObject participantDelta) throws SyncableDatabaseException {
        return dbHandler.getUpdate(participantDelta);
    }

    public JSONObject getDelta(JSONObject delta) throws SyncableDatabaseException {

        JSONObject deltaToSend = dbHandler.getDelta(delta);
        return deltaToSend;
    }

    public void updateDB(JSONObject delta) throws SyncableDatabaseException {
        dbHandler.updateDB(delta);
    }
}
