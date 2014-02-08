package com.ichera.wolfviewer.document;

import java.io.File;

/**
 * Singleton document instance of this program. Manages all classes related
 * to the game
 */
public class Document 
{
	// unique instance
	private static Document	sInstance;
	
	// Wolf data
	private VSwapContainer	mVSwap;
	
	// If it was loaded, it has a corresponding directory. NOTE: it may not
	// exist any more or be invalidated. Either declare the Document damaged
	// (if not the whole data was loaded) or recreate the file tree (if the
	// whole was loaded)
	private File			mDirectory;
	
	/**
	 * Private constructor
	 */
	private Document()
	{
		
	}
	
	/**
	 * Pointer to unique instance
	 * @return unique instance
	 */
	public static Document getInstance()
	{
		if(sInstance == null)
			sInstance = new Document();
		return sInstance;
	}
	
	/**
	 * Returns true if the document was successfully loaded
	 * @return
	 */
	public boolean isLoaded()
	{
		return mDirectory != null;
	}
	
	/**
	 * Loads data from a given directory. It only overwrites its contents if it
	 * returns true.
	 * @param directory Directory containing Wolfenstein files
	 * @return True on success
	 */
	public boolean loadFromDirectory(File directory)
	{
		if(!directory.isDirectory())
			return false;
		
		// Try to read VSWAP.WL6
		File vSwapFile = new File(directory, "vswap.wl6");
		if(!vSwapFile.exists())
			return false;
		VSwapContainer vswap = new VSwapContainer();
		if(!vswap.loadFile(vSwapFile))
			return false;
		
		// Success
		mDirectory = directory;
		mVSwap = vswap;
		return true;
	}
	
	/**
	 * Gets reference to VSWap container
	 * @return
	 */
	public VSwapContainer getVSwap()
	{
		return mVSwap;
	}
}
