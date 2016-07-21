package de.anjakammer.bassa.CommService;



import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;

import net.sharksystem.android.peer.SharkServiceController;

/**
 * Implements Shark Knowledge Port used of SyncProtocol
 */
public class DataPort extends AppCompatActivity {

    private SharkServiceController mServiceController;
    private WifiManager mWifiManager;

    private String mName = "name";
    private String mInterest = "interest";

    public DataPort(){
        mServiceController = SharkServiceController.getInstance(this);
        mServiceController.setOffer(mName, mInterest);
        mServiceController.startShark();
    }

    public void sendData(byte[] message){
        // TODO send with ASIP
    }

    public byte[] getData(){
        return  "message".getBytes();
    }

}
