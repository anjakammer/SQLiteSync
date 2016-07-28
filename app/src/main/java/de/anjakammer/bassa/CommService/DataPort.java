package de.anjakammer.bassa.commService;



import android.content.Context;

import net.sharksystem.android.peer.SharkServiceController;
import net.sharksystem.android.protocols.wifidirect_obsolete.WifiDirectPeer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * used of SyncProtocol
 */
public class DataPort{

    private SharkServiceController mServiceController;
    private String interest;
    public List<String> data;

    public DataPort(String name, String interest, Context context){
        this.interest = interest;
        mServiceController = SharkServiceController.getInstance(context);
        mServiceController.setOffer(name, interest);
        mServiceController.startShark();
    }

    public void sendData(String data){
        // TODO send with ASIP
        mServiceController.sendBroadcast(data);
    }

    public List<String> getData(){
        data = mServiceController.getStringMessages();
        return  this.data;
    }

    public List<String> getPeers(){
        CopyOnWriteArrayList<WifiDirectPeer> mPeerList = mServiceController.getPeers();
        List<String> mPeers = new ArrayList<>();

        long current = System.currentTimeMillis();

        Iterator<WifiDirectPeer> iterator = mPeerList.iterator();
        while (iterator.hasNext()){
            WifiDirectPeer peer = iterator.next();
            if((current - peer.getLastUpdated()) < 1000 * 60
                    || peer.getmInterest().equals(this.interest)){
                mPeers.add(peer.getName());
            }
        }
        return mPeers;
    }
}
