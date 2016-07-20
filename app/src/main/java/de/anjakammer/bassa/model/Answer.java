package de.anjakammer.bassa.model;

public class Answer {

    private String description;
    private long participantId;
    private long id;
    private long question_id;

    public static final String DEFAULT_ANSWER = "not answered yet";

    public Answer(String description, long participantId, long id,  long question_id) {
        this.description = description;
        this.participantId = participantId;
        this.id = id;
        this.question_id = question_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(long participant) {
        this.participantId = participant;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(long question_id) {
        this.question_id = question_id;
    }

    @Override
    public String toString() {
        return "Participant "+participantId + ": " + (description.equals("") ? DEFAULT_ANSWER : description );
    }
}