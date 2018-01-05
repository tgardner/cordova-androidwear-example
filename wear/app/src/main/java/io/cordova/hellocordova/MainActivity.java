package io.cordova.hellocordova;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements
        MessageClient.OnMessageReceivedListener,
        CapabilityClient.OnCapabilityChangedListener {

    private TextView mTextView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);
        Button mButton = findViewById(R.id.button);

        mContext = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (new Thread(new MessageRunnable())).start();
                logMessage("Message sent to cordova");
            }
        });

        logMessage("onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this)
                .addListener(
                        this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);

        logMessage("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
    }

    private void logMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.append(message);
                mTextView.append("\n");
            }
        });
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        logMessage(Arrays.toString(messageEvent.getData()));
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        logMessage(capabilityInfo.toString());
    }

    private class MessageRunnable implements Runnable {

        @Override
        public void run() {
            try {
                List<Node> nodes =
                        Tasks.await(Wearable.getNodeClient(getApplicationContext())
                                .getConnectedNodes());

                for (Node node : nodes) {
                    Task<Integer> messageTask = Wearable.getMessageClient(mContext).sendMessage(node.getId(),
                            Constants.MESSAGE_RECEIVED_PATH, "Hello from AndroidWear".getBytes());
                    Tasks.await(messageTask);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
