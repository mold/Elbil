/**
 * @author John Brynte Turesson
 */

package com.kth.ev.differentiatedrange.puredata;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;

import com.kth.ev.differentiatedrange.CarData;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class PureDataHandler implements Runnable {
	
	private final String TAG = "puredata";
	private final int THREAD_SLEEP = 50;
	
	private Thread thread;
	private Context context;
	private CarData carData;
	
	private PdService pdService = null;

	public static interface ReadyListener{
		public void ready();
	}
	
	private ArrayList<ReadyListener> readyListeners;
	private boolean ready = false;
	
	public PureDataHandler(Context context, CarData carData) {
		this.context = context;
		this.carData = carData;
		readyListeners = new ArrayList<PureDataHandler.ReadyListener>();
		
		AudioParameters.init(context);
		PdPreferences.initPreferences(context);
		// Create a connection to Pd
		context.bindService(new Intent(context, PdService.class), pdConnection, Context.BIND_AUTO_CREATE);
	}
	
	/**
	 * Receives data from Pd.
	 */
	private PdReceiver receiver = new PdReceiver() {

		private void pdPost(String source, String symbol, String message) {
			Log.v(TAG, "[PD] source: " + source + "; symbol: " + symbol + "; message: " + message);
		}

		@Override
		public void print(String s) {
			pdPost("", "", s);
		}

		@Override
		public void receiveBang(String source) {
			pdPost(source, "", "bang");
		}

		@Override
		public void receiveFloat(String source, float x) {
			pdPost(source, "", "float: " + x);
		}

		@Override
		public void receiveList(String source, Object... args) {
			pdPost(source, "", "list: " + Arrays.toString(args));
		}

		@Override
		public void receiveMessage(String source, String symbol, Object... args) {
			pdPost(source, symbol, "message: " + Arrays.toString(args));
		}

		@Override
		public void receiveSymbol(String source, String symbol) {
			pdPost(source, "symbol: " + symbol, "");
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
		// Use this to send data from pd ([s source] block in pd)
		//PdBase.subscribe("source");
		
		Log.v(TAG, "pd initialized");
		
		ready = true;
		for (ReadyListener rl : readyListeners) {
			rl.ready();
		}
		
		thread = new Thread(this);
		thread.start();
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
		if(!ready) 
			return;
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
		if(ready) 
		pdService.stopAudio();
	}
	
	/**
	 * Create a patch from a resource id
	 * @param resource Resource id
	 * @return New Patch
	 * @throws Exception Handling the case that PD has not been loaded before calling this method.
	 */
	public Patch createPatch(int resource) throws Exception {
		if(!ready)
			throw new Exception("PD has not yet loaded!");
		return new Patch(context, resource);
	}
	
	public Patch[] loadPatchesFromDirectory(String dir) {
		File[] files = new File(dir).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				Pattern pattern = Pattern.compile(".*\\.(pd|PD)$");
				Matcher matcher = pattern.matcher(filename);
				return matcher.matches();
			}
		});
		Patch[] patches = new Patch[files.length];
		for (int i = 0; i < files.length; i++) {
			patches[i] = new Patch(files[i]);
		}
		return patches;
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

	@Override
	public void run() {
		while(true) {
			PdBase.sendFloat("soc", (float) carData.getSoc(true));
			PdBase.sendFloat("speed", (float) carData.getSpeed(true));
			PdBase.sendFloat("fan", (float) carData.getFan(true));
			PdBase.sendFloat("climate", (float) carData.getClimate(true));
			
			try {
				Thread.sleep(THREAD_SLEEP);
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}
	
}
