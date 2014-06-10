/**
 * @author John Brynte Turesson
 */

package com.kth.ev.differentiatedrange.puredata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class PureDataHandler {
	
	private final String TAG = "pd";
	
	private Context context;
	
	private PdService pdService = null;

	public static interface ReadyListener{
		public void ready();
	}
	
	private ArrayList<ReadyListener> readyListeners;
	private boolean ready = false;
	
	public PureDataHandler(Context context) {
		this.context = context;
		readyListeners = new ArrayList<PureDataHandler.ReadyListener>();
		
		AudioParameters.init(context);
		PdPreferences.initPreferences(context.getApplicationContext());
		// Create a connection to Pd
		context.bindService(new Intent(context, PdService.class), pdConnection, Context.BIND_AUTO_CREATE);
	}
	
	/**
	 * Receives data from Pd.
	 */
	private PdReceiver receiver = new PdReceiver() {

		private void pdPost(String msg) {
			Log.v(TAG, "Pure Data says, \"" + msg + "\"");
		}

		@Override
		public void print(String s) {
			pdPost(s);
		}

		@Override
		public void receiveBang(String source) {
			pdPost("bang");
		}

		@Override
		public void receiveFloat(String source, float x) {
			pdPost("float: " + x);
		}

		@Override
		public void receiveList(String source, Object... args) {
			pdPost("list: " + Arrays.toString(args));
		}

		@Override
		public void receiveMessage(String source, String symbol, Object... args) {
			pdPost("message: " + Arrays.toString(args));
		}

		@Override
		public void receiveSymbol(String source, String symbol) {
			pdPost("symbol: " + symbol);
		}
	};
	
	/**
	 * ServiceConnection to Pd.
	 */
	private final ServiceConnection pdConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v(TAG, "service connected");
			pdService = ((PdService.PdBinder) service).getService();
			initPd();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
			Log.v(TAG, "service disconnected");
		}
	};
	
	/**
	 * Initialize Pd. Called after successful ServiceConnection to Pd. 
	 */
	private void initPd() {
		PdBase.setReceiver(receiver);
		PdBase.subscribe("android");
		
		Log.v(TAG, "pd initialized");
		
		ready = true;
		for (ReadyListener rl : readyListeners) {
			rl.ready();
		}
	}
	
	public void addReadyListener(ReadyListener listener) {
		if (ready) {
			listener.ready();
		} else {
			readyListeners.add(listener);
		}
	}
	
	/**
	 * Returns true if the service is sending audio.
	 * @return true if the service is sending audio.
	 */
	public boolean isPlaying() {
		return pdService.isRunning();
	}
	
	/**
	 * Start playing sound.
	 */
	public void startAudio() {
		try {
			// negative values will be replaced with defaults/preferences
			pdService.initAudio(-1, -1, -1, -1);
			Log.v(TAG, "start audio");
			pdService.startAudio();
		} catch (IOException e) {
			Log.e(TAG, "start audio: "+e.toString());
		}
	}

	/**
	 * Stop playing sound.
	 */
	public void stopAudio() {
		pdService.stopAudio();
	}
	
	public Patch createPatch(int resource) {
		return new Patch(context, resource);
	}
	
	/**
	 * Close the connection to Pd.
	 */
	public void closeConnection() {
		try {
			context.unbindService(pdConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdService = null;
		}
	}
	
}
