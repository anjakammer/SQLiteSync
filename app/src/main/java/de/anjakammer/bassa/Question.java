package de.anjakammer.bassa;

public class Question {

    private String description;
    private String title;
    private long id;
    private boolean isDeleted;


    public Question(String description, String title, long id, boolean isDeleted) {
        this.description = description;
        this.title = title;
        this.id = id;
        this.isDeleted = isDeleted;
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

    public void setIsDeleted (boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return title + "\n" + description;
    }
}