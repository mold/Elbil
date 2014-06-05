/**
 * 
 * @author John Brynte Turesson
 * 
 */

package com.example.audiobahn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.IoUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AudioBahnActivity extends Activity implements OnClickListener,
		OnSeekBarChangeListener,
		SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "pd";

	private Button play;
	private TextView logs;
	private SeekBar frequencySlider;
	private TextView frequencyText;
	private Button togglePatchButton;
	private TextView patchText;

	private PdService pdService = null;

	private Toast toast = null;

	private int patchHandle;
	private int currentPatch = 0;
	private int[] patches = { R.raw.sine, R.raw.test };

	private void toast(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (toast == null) {
					toast = Toast.makeText(getApplicationContext(), "",
							Toast.LENGTH_SHORT);
				}
				toast.setText(TAG + ": " + msg);
				toast.show();
			}
		});
	}

	private void post(final String s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				logs.append(s + ((s.endsWith("\n")) ? "" : "\n"));
			}
		});
	}

	private PdReceiver receiver = new PdReceiver() {

		private void pdPost(String msg) {
			toast("Pure Data says, \"" + msg + "\"");
			Log.v(TAG, "Pure Data says, \"" + msg + "\"");
		}

		@Override
		public void print(String s) {
			post(s);
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
			pdService = ((PdService.PdBinder) service).getService();
			initPd();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
		}
	};

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AudioParameters.init(this);
		PdPreferences.initPreferences(getApplicationContext());
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.registerOnSharedPreferenceChangeListener(this);
		initGui();
		// Create a connection between this activity and Pd 
		bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanup();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (pdService.isRunning()) {
			startAudio();
		}
	}

	private void initGui() {
		setContentView(R.layout.activity_audiobahn);
		frequencySlider = (SeekBar) findViewById(R.id.frequency_slider);
		frequencySlider.setOnSeekBarChangeListener(this);
		frequencyText = (TextView) findViewById(R.id.frequency_text);
		play = (Button) findViewById(R.id.play_button);
		play.setOnClickListener(this);
		togglePatchButton = (Button) findViewById(R.id.toggle_patch_button);
		togglePatchButton.setOnClickListener(this);
		patchText = (TextView) findViewById(R.id.loaded_patch_text);
		logs = (TextView) findViewById(R.id.log_box);
		logs.setMovementMethod(new ScrollingMovementMethod());
	}

	/**
	 * Initialize Pd. Called after successful ServiceConnection to Pd. 
	 */
	private void initPd() {
		PdBase.setReceiver(receiver);
		PdBase.subscribe("android");
		
		openPatch(patches[currentPatch]);
	}

	/**
	 * Open a cached patch.
	 * @param patch Reference to resource id (R.raw.patch_name).
	 */
	private void openPatch(int patch) {
		File file = null;
		try {
			Resources res = getResources();
			InputStream in;
			String name;
			// Load the Pd patch to cache
			in = res.openRawResource(patch);
			name = res.getResourceEntryName(patch) + ".pd";
			file = IoUtils.extractResource(in, name, getCacheDir());
			Log.v(TAG, "Loaded " + name + " (" + file + ")");
			// Open the patch
			closeCurrentPatch();
			patchHandle = PdBase.openPatch(file);
			patchText.setText("Patch: " + file);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			finish();
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}
	
	/**
	 * Close the current loaded patch.
	 */
	private void closeCurrentPatch() {
		PdBase.closePatch(patchHandle);
	}

	/**
	 * Start playing sound.
	 */
	private void startAudio() {
		try {
			// negative values will be replaced with defaults/preferences
			pdService.initAudio(-1, -1, -1, -1);
			pdService.startAudio();
		} catch (IOException e) {
			toast(e.toString());
		}
	}

	/**
	 * Stop playing sound.
	 */
	private void stopAudio() {
		pdService.stopAudio();
	}

	private void cleanup() {
		try {
			unbindService(pdConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdService = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.play_button:
			if (pdService.isRunning()) {
				stopAudio();
			} else {
				startAudio();
			}
			break;
		case R.id.toggle_patch_button:
			currentPatch++;
			if (currentPatch >= patches.length) {
				currentPatch = 0;
			}
			openPatch(patches[currentPatch]);
			break;
		default:
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		frequencyText.setText(progress + " hz");
		PdBase.sendFloat("frequency", (float) progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}
}
