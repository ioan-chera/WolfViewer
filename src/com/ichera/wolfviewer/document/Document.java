package com.ichera.wolfviewer.document;

import java.io.File;

import com.ichera.wolfviewer.RunnableArg;

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
	private LevelContainer	mLevels;
	
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
	public boolean loadFromDirectory(File directory, RunnableArg<String> progressUpdater)
	{
		if(!directory.isDirectory())
			return false;
		
		// First check if files exist
		if(progressUpdater != null)
			progressUpdater.run("Checking for files...");
		File vSwapFile = new File(directory, "vswap.wl6");
		if(!vSwapFile.exists())
			return false;
		File mapHeadFile = new File(directory, "maphead.wl6");
		if(!mapHeadFile.exists())
			return false;
		File gameMapsFile = new File(directory, "gamemaps.wl6");
		if(!gameMapsFile.exists())
			return false;
		
		// Try loading vswap container
		if(progressUpdater != null)
			progressUpdater.run("Loading VSWAP file...");
		VSwapContainer vswap = new VSwapContainer();
		if(!vswap.loadFile(vSwapFile))
			return false;
		
		// Try loading level container
		if(progressUpdater != null)
			progressUpdater.run("Loading MAPHEAD and GAMEMAPS files...");
		LevelContainer levels = new LevelContainer();
		if(!levels.loadFile(mapHeadFile, gameMapsFile))
			return false;
		
		// Success
		mDirectory = directory;
		mVSwap = vswap;
		mLevels = levels;
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
	
	public LevelContainer getLevels()
	{
		return mLevels;
	}
}
