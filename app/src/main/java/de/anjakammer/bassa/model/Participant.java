package de.anjakammer.bassa.model;

public class Participant {

    private String address;
    private String name;
    private long id;


    public Participant(String address, String name, long id) {
        this.address = address;
        this.name = name;
        this.id = id;
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

    @Override
    public String toString() {
        return name + ": " + address;
    }
}