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
import android.os.Environment;
import android.util.Log;

public class Patch {
	
	private final String TAG = "pd";
	
	private String patchPath = Environment.getExternalStorageDirectory() + "/puredata/";

	private Context context;
	
	private File patchFile = null;
	private int patchId = -1;
	private int patchHandle;
	
	/**
	 * Load a patch from the resource folder
	 * @param context Reference to the app, for file management
	 * @param patchId Resource id
	 */
	public Patch(Context context, int patchId) {
		this.context = context;
		this.patchId = patchId;
	}
	
	/**
	 * Use a patch from the "puredata" folder on the file system
	 * (usually "storage/emulated/0/puredata/")
	 * @param patchPath The name of the patch. 
	 */
	public Patch(String patchName) {
		patchFile = new File(patchPath + patchName);
	}
	
	/**
	 * Opens this patch.
	 */
	public void open() {
		File file = null;
		try {
			if (patchFile != null) {
				file = patchFile;
			} else {
				// load from a resource
				Resources res = context.getResources();
				InputStream in;
				File patchDir = context.getCacheDir();
				String fileName = res.getResourceEntryName(patchId) + ".pd";
				// load the patch to cache
				in = res.openRawResource(patchId);
				file = IoUtils.extractResource(in, fileName, patchDir);			
			}
			// Open the patch
			patchHandle = PdBase.openPatch(file);
		} catch (IOException e) {
			Log.e(TAG, "open tag: "+e.toString());
		} finally {
			// from test code, possibly just to free up space...
			//if (file != null) {
			//	file.delete();
			//}
		}
	}
	
	/**
	 * Closes this patch.
	 */
	public void close() {
		PdBase.closePatch(patchHandle);
	}
	
}
