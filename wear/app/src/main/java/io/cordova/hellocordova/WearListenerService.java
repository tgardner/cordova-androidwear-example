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
            final String message = new String(messageEvent.getData());
            // handle
            Intent intent = new Intent("MessageReceived");
            intent.putExtra("message", message);
            sendBroadcast(intent);
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
