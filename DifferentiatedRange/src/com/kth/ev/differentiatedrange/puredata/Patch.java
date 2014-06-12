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
	
	private String fileName = null;
	private File patchFile = null;
	private int patchId = -1;
	private int patchHandle;
	private boolean patchOpen = false;
	
	/**
	 * Load a patch from the resource folder
	 * @param context Reference to the app, for file management
	 * @param patchId Resource id
	 */
	public Patch(Context context, int patchId) {
		this.context = context;
		this.patchId = patchId;
		fileName = context.getResources().getResourceEntryName(patchId) + ".pd";
	}
	
	/**
	 * Use a patch from the "puredata" folder on the file system
	 * (usually "storage/emulated/0/puredata/")
	 * @param patchName The name of the patch. 
	 */
	public Patch(String patchName) {
		patchFile = new File(patchPath + patchName);
		fileName = patchFile.getName();
	}
	
	/**
	 * Use a file pointer
	 * @param patchFile The patch file. 
	 */
	public Patch(File patchFile) {
		this.patchFile = patchFile;
		fileName = patchFile.getName();
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
		patchOpen = true;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Closes this patch.
	 */
	public void close() {
		PdBase.closePatch(patchHandle);
		patchOpen = false;
	}
	
	/**
	 * Returns true if the patch is open
	 * @return true if the patch is open
	 */
	public boolean isOpen() {
		return patchOpen;
	}
	
}
