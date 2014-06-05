/**
 * @author John Brynte Turesson
 */

package com.example.audiobahn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class Patch implements Observer {
	
	private final String TAG = "pd";
	
	ArrayList<DataListener> listeners = new ArrayList<DataListener>();

	private Context context;
	
	private int patch;
	private int patchHandle;
	
	private String fileName = null;
	
	private File patchDir;
	
	public Patch(Context context, int patch) {
		this.context = context;
		this.patch = patch;
		patchDir = context.getCacheDir();
	}
	
	/**
	 * Opens this patch.
	 */
	public void open() {
		File file = null;
		try {
			Resources res = context.getResources();
			InputStream in;
			// Load the Pd patch to cache
			in = res.openRawResource(patch);
			if (fileName == null) {
				fileName = res.getResourceEntryName(patch) + ".pd";
			}
			file = IoUtils.extractResource(in, fileName, patchDir);
			// Open the patch
			patchHandle = PdBase.openPatch(file);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}
	
	/**
	 * Closes this patch.
	 */
	public void close() {
		PdBase.closePatch(patchHandle);
	}
	
	public void addDataListener(DataListener listener) {
        listeners.add(listener);
    }

	@Override
	public void update(Observable observable, Object data) {
		for (DataListener dl : listeners) {
			dl.onDataUpdate(data);
		}
	}
	
}
