package de.anjakammer.bassa.model;

import java.util.List;

public class Question {

    private String description;
    private String title;
    private long id;
    private boolean isDeleted;
    private List<Answer> answers;

    public Question(String description, String title, long id) {
        this.description = description;
        this.title = title;
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        String str = title + ": " + description + "\n Answers: \n";
        for (Answer answer: getAnswers()) {
            str += answer.toString()+ "\n";
        }
        return str;
    }
}