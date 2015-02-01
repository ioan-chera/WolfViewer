package org.i_chera.wolfensteineditor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.i_chera.wolfensteineditor.Global;
import org.i_chera.wolfensteineditor.MainActivity;
import org.i_chera.wolfensteineditor.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class LevelMenuFragment extends Fragment
{
    private static final String STATE_CURRENT_WALL_CHOICE = "currentWallChoice";

    // state
    private int mCurrentWallChoice;

    private ListView mWallList;

    private JSONArray mWallChoices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_level_menu, container, false);

        mWallList = (ListView)v.findViewById(R.id.wall_list);

        readWallChoices();

        if(savedInstanceState != null)
        {
            mCurrentWallChoice = savedInstanceState.getInt(STATE_CURRENT_WALL_CHOICE);
        }

        mWallList.setAdapter(new WallListAdapter());
        mWallList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                mCurrentWallChoice = position;
                ((WallListAdapter)mWallList.getAdapter()).notifyDataSetChanged();
            }
        });


        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(STATE_CURRENT_WALL_CHOICE, mCurrentWallChoice);
        super.onSaveInstanceState(outState);
    }

    void updateWallList()
    {
        ((WallListAdapter)mWallList.getAdapter()).notifyDataSetChanged();
    }

    int getChoiceId()
    {
        if(mWallChoices == null || !Global.inBounds(mCurrentWallChoice, 0, mWallChoices.length() - 1))
            return -1;
        JSONObject object = mWallChoices.optJSONObject(mCurrentWallChoice);
        if(object == null)
            return -1;
        return object.optInt("id");
    }

    private void readWallChoices()
    {
        // Called upon creation
        InputStream is = null;
        StringBuilder sb;
        try
        {
            sb = new StringBuilder(5000);
            is = getActivity().getAssets().open("wall_choices.json");

            byte[] buffer = new byte[512];
            while(is.read(buffer) > 0)
                sb.append(new String(buffer, "UTF-8"));

        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }
        finally
        {
            if(is != null)
            {
                try
                {
                    is.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        String jsonString = sb.toString();
        JSONArray array;
        try
        {
            array = new JSONArray(jsonString);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
            ((MainActivity)getActivity()).showErrorAlert("Internal error", "Failed to read the wall choice list!");
            return;
        }
        mWallChoices = array;
        mCurrentWallChoice = Global.boundValue(mCurrentWallChoice, 0, mWallChoices.length() - 1);
    }

    private class WallListAdapter extends BaseAdapter
    {

        @Override
        public int getCount()
        {
            return mWallChoices != null ? mWallChoices.length() : 0;
        }

        @Override
        public Object getItem(int position)
        {
            return mWallChoices != null ? mWallChoices.optJSONObject(position) : null;
        }

        @Override
        public long getItemId(int position)
        {
            if(mWallChoices == null)
                return -1;
            else
            {
                JSONObject object = (JSONObject)getItem(position);
                if(object != null)
                    return object.optLong("id");
                else
                    return -1;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView item;
            if(convertView == null)
            {
                MainActivity activity = (MainActivity)getActivity();
                item = new ImageView(getActivity());
                AbsListView.LayoutParams alvlp = new AbsListView.LayoutParams(
                        (int)(48 * activity.getPixelScale()),
                        (int)(48 * activity.getPixelScale()));
                item.setLayoutParams(alvlp);
                item.setPadding(0, (int)(5 * activity.getPixelScale()),
                        0, (int)(5 * activity.getPixelScale()));
            }
            else
                item = (ImageView)convertView;
            int index = (int)getItemId(position);
            if(mCurrentWallChoice == position)
                item.setBackgroundResource(R.drawable.frame_selection);
            else
                item.setBackgroundResource(0);

            LevelFragment fragment = (LevelFragment)getParentFragment();
            if(!fragment.isDocumentLoaded() || !fragment.setBitmapFromMapValue(item, index))
                item.setImageDrawable(null);

            return item;
        }
    }
}
