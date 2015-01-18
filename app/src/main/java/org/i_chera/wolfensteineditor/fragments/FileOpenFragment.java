package org.i_chera.wolfensteineditor.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.i_chera.wolfensteineditor.R;

import java.io.File;
import java.util.Arrays;

/**
 * Created by ioan_chera on 17.01.2015.
 */
public class FileOpenFragment extends Fragment
{
    // State
    private File mPath;
    private static final String KEY_Path = "Path";

    // Derived
    private File[] mFileList;

    // controls
    private TextView mPathView;
    private ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // Get saved instance data
        if(savedInstanceState != null) {
            String pathString = savedInstanceState.getString(KEY_Path);
            mPath = new File(pathString);
        }
        else
            mPath = Environment.getExternalStorageDirectory();  // TODO: restore path

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        String path = mPath.getPath();
        outState.putString(KEY_Path, path);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.fragment_file_open, null);

        // Get views
        mPathView = (TextView)v.findViewById(R.id.pathView);
        mListView = (ListView)v.findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File dir = (File)parent.getAdapter().getItem(position);
                if(dir.isDirectory())
                {
                    mPath = dir;
                    setFileList(dir);
                }
            }
        });

        // Set derived data
        setFileList(mPath);
        // Must be set here
        mListView.setAdapter(new PathAdapter());

        return v;
    }

    private void setFileList(File dir)
    {
        File[] files = dir.listFiles();
        if(files != null)
            Arrays.sort(files);
        if(mFileList != files)
        {
            mFileList = files;

            mPathView.setText(dir.getPath());
            PathAdapter adapter = (PathAdapter)mListView.getAdapter();
            if(adapter != null)
                adapter.notifyDataSetChanged();
        }
    }

    private class PathAdapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return mFileList == null ? 0 : mFileList.length;
        }

        @Override
        public Object getItem(int position) {
            return mFileList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
            {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_file_open,
                        parent, false);
            }
            TextView textView = (TextView)convertView.findViewById(R.id.textView);
            File file = mFileList[position];
            if(file.isDirectory())
                textView.setTypeface(Typeface.DEFAULT_BOLD);
            else
                textView.setTypeface(Typeface.DEFAULT);
            textView.setText(file.getName());
            return convertView;
        }
    }
}
