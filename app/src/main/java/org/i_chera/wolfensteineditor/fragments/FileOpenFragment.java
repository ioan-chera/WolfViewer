package org.i_chera.wolfensteineditor.fragments;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.i_chera.wolfensteineditor.Global;
import org.i_chera.wolfensteineditor.MainActivity;
import org.i_chera.wolfensteineditor.R;
import org.i_chera.wolfensteineditor.RunnableArg;
import org.i_chera.wolfensteineditor.document.Document;

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
    private ImageView mUpButton;
    private Button mOpenButton;

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
        mUpButton = (ImageView)v.findViewById(R.id.upButton);
        mUpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mPath.getParent() != null)
                {
                    mPath = new File(mPath.getParent());
                    setFileList(mPath);
                }
            }
        });
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
        mOpenButton = (Button)v.findViewById(R.id.open_button);
        mOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryOpenCurrentPath();
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
        mUpButton.setVisibility(dir.getParent() != null ? View.VISIBLE : View.GONE);
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

    private void tryOpenCurrentPath()
    {
        final File path = mPath;
        AsyncTask<Void, String, Document> task = new AsyncTask<Void, String, Document>() {

            @Override
            protected void onPreExecute() {
                // TODO: init progress
            }

            @Override
            protected Document doInBackground(Void... params) {
                Document document = new Document();
                boolean result = document.loadFromDirectory(path, new RunnableArg<String>() {
                    @Override
                    public void run() {
                        publishProgress(mArgs);
                    }
                });
                return result ? document : null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                // TODO: display progress
            }

            @Override
            protected void onPostExecute(Document document) {
                // TODO: end progress
                if(getActivity() == null) {
                    Log.w("FileOpenFragment", "Async task finished after activity was destroyed");
                    return;
                }

                if (document != null) {
                    ((MainActivity) getActivity()).goToLevelFragment(document, path);
                }
                else
                {
                    Global.showErrorAlert(getActivity(), "Level Load Error", "Couldn't open data " +
                            "from directory " + path.getPath());
                }
            }

        };

        task.execute();
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
