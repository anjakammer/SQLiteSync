package de.anjakammer.bassa.CommService;



import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;

import net.sharksystem.android.Application;
import net.sharksystem.android.peer.SharkServiceController;

/**
 * Implements Shark Knowledge Port used of SyncProtocol
 */
public class DataPort extends AppCompatActivity {
    private Context context;
    private SharkServiceController mServiceController;
    private WifiManager mWifiManager;

    private String mName = "name";
    private String mInterest = "interest";

    public DataPort(){

        context = Application.getAppContext();
        //mBroadCastAdapter = new WifiDirectBroadcastAdapter(context);
        mServiceController = SharkServiceController.getInstance(this);
        mServiceController.setOffer(mName, mInterest);
        mServiceController.startShark();
		// https://github.com/SharedKnowledge/SBC/blob/master/app/src/main/java/net/sharksystem/sbc/fragments/BroadcastsFragment.java
    }

    public void sendData(byte[] message){
        // TODO send with ASIP
    }

    public byte[] getData(){
        return  "message".getBytes();
    }

}
