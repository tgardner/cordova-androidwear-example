package io.cordova.hellocordova;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

public class WearListenerService extends WearableListenerService {

    private static final String TAG = WearListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived");

        if(messageEvent.getPath().equals(Constants.MESSAGE_RECEIVED_PATH)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    @Override
    public void onPeerConnected (Node peer) {
        super.onPeerConnected(peer);

        Log.d(TAG, "onPeerConnected");
    }
}
