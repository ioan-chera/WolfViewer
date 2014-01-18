package com.ichera.wolfviewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Startup activity
 * @author ioan
 *
 */
public class MainActivity extends Activity 
{

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Global.initialize(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case R.id.action_open:
    	{
    		Intent intent = new Intent(this, OpenActivity.class);
    		intent.putExtra(OpenActivity.KEY_PATH, Environment
    				.getExternalStorageDirectory().getPath());
    		startActivity(intent);
    	}
    		break;
    	default:
    		break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
}
