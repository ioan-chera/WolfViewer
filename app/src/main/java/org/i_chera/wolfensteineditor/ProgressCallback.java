package org.i_chera.wolfensteineditor;

/**
 * Created by ioan_chera on 24.01.2015.
 */
public interface ProgressCallback {
    public void onProgress(int position, int max, String message);

    public static class Data
    {
        public int position;
        public int max;
        public String message;
    }
}
