/**
 * @author John Brynte Turesson
 */

package com.kth.ev.differentiatedrange.puredata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class Patch {
	
	private final String TAG = "pd";

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
			Log.e(TAG, "open tag: "+e.toString());
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
	
}
