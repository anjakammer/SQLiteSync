package de.anjakammer.bassa.CommService;

/**
 * Implements Shark Knowledge Port used of SyncProtocol
 */
public class DataPort {

    public DataPort(){

    }

    public void sendData(byte[] message){
        // TODO send with ASIP
    }

    public byte[] getData(){
        return  "message".getBytes();
    }

}
