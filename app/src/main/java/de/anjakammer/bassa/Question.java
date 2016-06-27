package de.anjakammer.bassa;

public class Question {

    private String question;
    private String title;
    private long id;
    private boolean isDeleted;


    public Question(String question, String title, long id, boolean isDeleted) {
        this.question = question;
        this.title = title;
        this.id = id;
        this.isDeleted = isDeleted;
    }


    public String getQuestion() {
        return question;
    }

    public void setQuestion (String question) {
        this.question = question;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setIsDeleted (boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        String output = title + "\n" + question;

        return output;
    }
}