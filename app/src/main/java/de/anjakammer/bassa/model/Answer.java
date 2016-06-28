package de.anjakammer.bassa.model;

public class Answer {

    private String description;
    private String participant;
    private long id;
    private long question_id;


    public Answer(String description, String participant, long id,  long question_id) {
        this.description = description;
        this.participant = participant;
        this.id = id;
        this.question_id = question_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
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
        return participant + ": " + description;
    }
}