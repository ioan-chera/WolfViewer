package com.ichera.wolfviewer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class SwitchableFragment extends Fragment 
{
	private Bundle mStateBundle;
	
	public void setStateBundle(Bundle stateBundle)
	{
		mStateBundle = stateBundle;
	}
	
	protected abstract void saveState(Bundle target);
	
	public void saveSwitchState()
	{
		saveState(mStateBundle);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		saveState(outState);
		super.onSaveInstanceState(outState);
	}
	
	protected Bundle getActualState(Bundle saved)
	{
		return saved == null ? getArguments() : saved;
	}
}
