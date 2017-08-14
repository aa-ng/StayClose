package com.example.alexng.stayclose;

import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

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

	public FencerHandler(final FragmentActivity context, final double latitude, final double longitude, final double radius) {
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

						setupFences(context, latitude, longitude, radius);
					}

					@Override
					public void onConnectionSuspended(int i) {
					}
				})
				.build();



	}

	private void setupFences(FragmentActivity context, double latitude, double longitude, double radius) {

		if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
			//Log.e(getClass().toString(), "Permission denied");
			//return;
		}
		AwarenessFence exitingFence = LocationFence.exiting(latitude, longitude, radius);

		// Register the fence to receive callbacks.
		Awareness.FenceApi.updateFences(
				mApiClient,
				new FenceUpdateRequest.Builder()
						.addFence(FENCE_KEY, exitingFence, mPendingIntent)
						.build())
				.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(@NonNull Status status) {
						if(status.isSuccess()) {
							Log.i(getClass().toString(), "Fence was successfully registered.");
						} else {
							Log.e(getClass().toString(), "Fence could not be registered: " + status);
						}
					}
				});
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
				Log.e(getClass().toString(), "unsupported action");
				return;
			}

			Log.i(getClass().toString(), "fence received");

			// The state information for the given fence is em
			FenceState fenceState = FenceState.extract(intent);

			if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
				String fenceStateStr;
				switch (fenceState.getCurrentState()) {
					case FenceState.TRUE:
						fenceStateStr = "true";
						Log.i(getClass().toString(), "Fence State = true");
						break;
					case FenceState.FALSE:
						Log.i(getClass().toString(), "Fence State = false");
						fenceStateStr = "false";
						break;
					case FenceState.UNKNOWN:
						Log.i(getClass().toString(), "Fence State = unknown");
						fenceStateStr = "unknown";
						break;
					default:
						Log.i(getClass().toString(), "Fence State = default unknown");
						fenceStateStr = "unknown value";
				}
			}
		}
	}
}
