/*
 * Wolfenstein 3D editor for Android
 * Copyright (C) 2014  Ioan Chera
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ichera.wolfviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;

/**
 * Global stuff. Miscellaneous static stuff that weren't classified yet go here.
 * @author ioan
 *
 */
public class Global 
{
	private static final String TAG = "Global";
	private static final String[] sWolfFileNames =
		{
		"AUDIOHED.WL6",
		"AUDIOT.WL6",
		"GAMEMAPS.WL6",
		"MAPHEAD.WL6",
		"VGADICT.WL6",
		"VGAGRAPH.WL6",
		"VGAHEAD.WL6",
		"VSWAP.WL6",
		};
	
	public static final int	SOUND_SAMPLE_RATE_HZ = 6896;
	
	private static float 	sScale;
	
	private static boolean 	sInitialized;
	
	private static SparseIntArray sActorSpriteMap;
	
	static
	{
		sActorSpriteMap = new SparseIntArray(200);
		
		// BJ
		sActorSpriteMap.put(19, 408);
		sActorSpriteMap.put(20, 409);
		sActorSpriteMap.put(21, 410);
		sActorSpriteMap.put(22, 411);
		
		// statics
		for(int i = 23; i <= 72; ++i)
			sActorSpriteMap.put(i, i - 21);
		
		// guard
		for(int i = 180; i <= 183; ++i)
			sActorSpriteMap.put(i, 56 - (i - 180) * 2);
		for(int i = 144; i <= 147; ++i)
			sActorSpriteMap.put(i, 56 - (i - 144) * 2);
		for(int i = 108; i <= 111; ++i)
			sActorSpriteMap.put(i, 56 - (i - 108) * 2);
		for(int i = 184; i <= 187; ++i)
			sActorSpriteMap.put(i, 64 - (i - 184) * 2);
		for(int i = 148; i <= 151; ++i)
			sActorSpriteMap.put(i, 64 - (i - 148) * 2);
		for(int i = 112; i <= 115; ++i)
			sActorSpriteMap.put(i, 64 - (i - 112) * 2);
		
		// officer
		for(int i = 188; i <= 191; ++i)
			sActorSpriteMap.put(i, 244 - (i - 188) * 2);
		for(int i = 152; i <= 155; ++i)
			sActorSpriteMap.put(i, 244 - (i - 152) * 2);
		for(int i = 116; i <= 119; ++i)
			sActorSpriteMap.put(i, 244 - (i - 116) * 2);
		for(int i = 192; i <= 195; ++i)
			sActorSpriteMap.put(i, 252 - (i - 192) * 2);
		for(int i = 156; i <= 159; ++i)
			sActorSpriteMap.put(i, 252 - (i - 156) * 2);
		for(int i = 120; i <= 123; ++i)
			sActorSpriteMap.put(i, 252 - (i - 120) * 2);
		
		// ss
		for(int i = 198; i <= 201; ++i)
			sActorSpriteMap.put(i, 144 - (i - 198) * 2);
		for(int i = 162; i <= 165; ++i)
			sActorSpriteMap.put(i, 144 - (i - 162) * 2);
		for(int i = 126; i <= 129; ++i)
			sActorSpriteMap.put(i, 144 - (i - 126) * 2);
		for(int i = 202; i <= 205; ++i)
			sActorSpriteMap.put(i, 152 - (i - 202) * 2);
		for(int i = 166; i <= 169; ++i)
			sActorSpriteMap.put(i, 152 - (i - 166) * 2);
		for(int i = 130; i <= 133; ++i)
			sActorSpriteMap.put(i, 152 - (i - 130) * 2);
		
		// dogs
		for(int i = 206; i <= 209; ++i)
			sActorSpriteMap.put(i, 105 - (i - 206) * 2);
		for(int i = 170; i <= 173; ++i)
			sActorSpriteMap.put(i, 105 - (i - 170) * 2);
		for(int i = 134; i <= 137; ++i)
			sActorSpriteMap.put(i, 105 - (i - 134) * 2);
		for(int i = 210; i <= 213; ++i)
			sActorSpriteMap.put(i, 113 - (i - 210) * 2);
		for(int i = 174; i <= 177; ++i)
			sActorSpriteMap.put(i, 113 - (i - 174) * 2);
		for(int i = 138; i <= 141; ++i)
			sActorSpriteMap.put(i, 113 - (i - 138) * 2);
		
		sActorSpriteMap.put(214, 296);	// hans
		sActorSpriteMap.put(197, 385);	// gretel
		sActorSpriteMap.put(215, 360);	// gift
		sActorSpriteMap.put(179, 396);	// fat
		sActorSpriteMap.put(196, 307);	// doctor
		sActorSpriteMap.put(160, 321);	// fake
		sActorSpriteMap.put(178, 334);	// hitler
		
		// mutants
		for(int i = 252; i <= 255; ++i)
			sActorSpriteMap.put(i, 193 - (i - 252) * 2);
		for(int i = 234; i <= 237; ++i)
			sActorSpriteMap.put(i, 193 - (i - 234) * 2);
		for(int i = 216; i <= 219; ++i)
			sActorSpriteMap.put(i, 193 - (i - 216) * 2);
		for(int i = 256; i <= 259; ++i)
			sActorSpriteMap.put(i, 201 - (i - 256) * 2);
		for(int i = 238; i <= 241; ++i)
			sActorSpriteMap.put(i, 201 - (i - 238) * 2);
		for(int i = 220; i <= 223; ++i)
			sActorSpriteMap.put(i, 201 - (i - 220) * 2);
		
		// evil ghosts
		sActorSpriteMap.put(224, 288);
		sActorSpriteMap.put(225, 292);
		sActorSpriteMap.put(226, 290);
		sActorSpriteMap.put(227, 294);
	}
	
	/**
	 * Initializes the global variables. May have to be called when activity is
	 * reborn
	 * @param context the relevant context
	 */
	public static void initialize(Activity context)
	{
		if(sInitialized)
			return;
		Log.i(TAG, "Initializing...");
		sInitialized = true;
				
		sScale = context.getResources().getDisplayMetrics().density;
	}
	
	/**
	 * A static list of valid Wolfenstein data file names, as strings
	 * @return
	 */
	public static String[] getWolfFileNames()
	{
		return sWolfFileNames;
	}
	
	/**
	 * A mapping between plane 1 tile ID and display sprite
	 * @return A sparse int array.
	 */
	public static SparseIntArray getActorSpriteMap()
	{
		return sActorSpriteMap;
	}
	
	/**
	 * Screen pixel scale
	 * @return
	 */
	public static float getScale()
	{
		return sScale;
	}
		
	/**
	 * Shows an alert view with the OK button
	 * @param context An activity or application-given context
	 * @param title Dialog title
	 * @param message Dialog message
	 */
	public static void showErrorAlert(Context context, String title, String message)
	{
		new AlertDialog.Builder(context).setTitle(title)
		.setMessage(message).setNeutralButton("OK", null).show();
	}
	
	/**
	 * Limits a value between two bounds
	 * @param val Current value
	 * @param min Minimum bound
	 * @param max Maximum bound
	 * @return The value, capped to min or max, if it initially was out of the 
	 * min-max inclusive interval
	 */
	public static int boundValue(int val, int min, int max)
	{
		if(val < min)
			val = min;
		else if(val > max)
			val = max;
		return val;
	}
	
	/**
	 * Checks if value is in bounds
	 * @param val Current value
	 * @param min Minimum bound
	 * @param max Maximum bound
	 * @return true if val is in [min, max]
	 */
	public static boolean inBounds(int val, int min, int max)
	{
		return val >= min && val <= max;
	}
}
