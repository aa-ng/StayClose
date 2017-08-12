package com.example.alexng.stayclose;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Ming on 2017-08-12.
 */

public class FencerHandler {

	private final String FENCE_RECEIVER_ACTION =
			BuildConfig.APPLICATION_ID + "FENCE_RECEIVER_ACTION";
	private PendingIntent mPendingIntent;
	private FenceReceiver mFenceReceiver;
	// The fence key is how callback code determines which fence fired.
	private final String FENCE_KEY = "fence_key";
	private GoogleApiClient mApiClient;

	public FencerHandler(final FragmentActivity context){
		mApiClient = new GoogleApiClient.Builder(context)
				.addApi(Awareness.API)
				.enableAutoManage(context, 1, null)
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(@Nullable Bundle bundle) {
						// Set up the PendingIntent that will be fired when the fence is triggered.
						Intent intent = new Intent(FENCE_RECEIVER_ACTION);
						mPendingIntent =
								PendingIntent.getBroadcast(context, 0, intent, 0);

						// The broadcast receiver that will receive intents when a fence is triggered.
						mFenceReceiver = new FenceReceiver();
						context.registerReceiver(mFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
						//setupFences();
					}

					@Override
					public void onConnectionSuspended(int i) {
					}
				})
				.build();

	}

	/**
	 * A basic BroadcastReceiver to handle intents from from the Awareness API.
	 */
	public class FenceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!TextUtils.equals(FENCE_RECEIVER_ACTION, intent.getAction())) {
				//mLogFragment.getLogView()
				//		.println("Received an unsupported action in FenceReceiver: action="
				//				+ intent.getAction());
				return;
			}

			// The state information for the given fence is em
			FenceState fenceState = FenceState.extract(intent);

			if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
				String fenceStateStr;
				switch (fenceState.getCurrentState()) {
					case FenceState.TRUE:
						fenceStateStr = "true";
						break;
					case FenceState.FALSE:
						fenceStateStr = "false";
						break;
					case FenceState.UNKNOWN:
						fenceStateStr = "unknown";
						break;
					default:
						fenceStateStr = "unknown value";
				}
			}
		}
	}
}
