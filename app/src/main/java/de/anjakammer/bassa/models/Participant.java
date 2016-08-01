package de.anjakammer.bassa.models;

public class Participant {

    private String address;
    private String name;
    private long id;
    private boolean isSelected;

    public Participant(String address, String name, long id, Boolean isSelected) {
        this.address = address;
        this.name = name;
        this.id = id;
        this.isSelected = isSelected;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return name + ": " + address;
    }
}