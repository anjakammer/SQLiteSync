package de.anjakammer.bassa.commService;



import android.content.Context;
import android.util.Log;

import net.sharkfw.asip.ASIPInterest;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkKBException;
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
    public static final String LOG_TAG = DataPort.class.getSimpleName();
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
        mServiceController.sendBroadcast(data);
    }

    public List<String> getData(){
        data = mServiceController.getStringMessages();
        return this.data;
    }

    public List<String> getPeers(){
        CopyOnWriteArrayList<WifiDirectPeer> mPeerList = mServiceController.getPeers();
        List<String> mPeers = new ArrayList<>();


        long current = System.currentTimeMillis();

        Iterator<WifiDirectPeer> iterator = mPeerList.iterator();
        while (iterator.hasNext()){
            WifiDirectPeer peer = iterator.next();
            Log.d(LOG_TAG,"Peer Detected: "+ peer.getName());

            String peerInterest = "";
            try{
                ASIPInterest asipInterest = peer.getmInterest();
                Iterator<SemanticTag> semanticTagIterator = asipInterest.getTopics().stTags();
                while(semanticTagIterator.hasNext()) {
                    if(peerInterest.isEmpty()){
                        peerInterest = semanticTagIterator.next().getName();
                    } else {
                        peerInterest += ", " + semanticTagIterator.next().getName();
                    }
                }
            } catch (SharkKBException e) {
                Log.e(LOG_TAG,"Error while fetching peer interest.");
                e.printStackTrace();
            }

            if((current - peer.getLastUpdated()) < 1000 * 60
                    && peerInterest.equals(this.interest)){
                mPeers.add(peer.getName());
            }
        }
        return mPeers;
    }


}
