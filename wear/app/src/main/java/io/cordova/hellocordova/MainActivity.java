package io.cordova.hellocordova;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends Activity implements
		MessageClient.OnMessageReceivedListener,
		CapabilityClient.OnCapabilityChangedListener {

	public static final String MESSAGE_PATH = "/NewMessage";

	private TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTextView = findViewById(R.id.text);
		Button mButton = findViewById(R.id.button);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				(new Thread(new MessageRunnable())).start();
			}
		});
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
		if (messageEvent.getPath().equals(MESSAGE_PATH)) {
			logMessage(new String(messageEvent.getData()));
		}
	}

	@Override
	public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
		logMessage("onCapabilityChanged: " + capabilityInfo.getName());
	}

	private class MessageRunnable implements Runnable {

		@Override
		public void run() {
			Task<List<Node>> nodesTask = Wearable.getNodeClient(MainActivity.this)
					.getConnectedNodes();
			nodesTask.addOnSuccessListener(new OnSuccessListener<List<Node>>() {

				@Override
				public void onSuccess(List<Node> nodes) {
					for (Node node : nodes) {
						Wearable.getMessageClient(MainActivity.this)
								.sendMessage(node.getId(), MESSAGE_PATH, "Hello from AndroidWear".getBytes());
					}

					logMessage("Message sent to Cordova");
				}
			});
		}
	}
}
