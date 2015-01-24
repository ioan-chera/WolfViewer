package org.i_chera.wolfensteineditor;

import android.support.v4.util.LruCache;
import android.util.Log;

import org.i_chera.wolfensteineditor.document.Document;

import java.io.File;

public class StateSaver {

    private static final int LRU_CACHE_SIZE = 5;

    private static LruCache<String, Document> sDocumentCache;

    public static Document withdrawDocument(File path)
    {
        if(sDocumentCache == null)
            return null;
        String pathString = path.getPath();
        Document document = sDocumentCache.get(pathString);
        sDocumentCache.remove(pathString);  // remove it, not needed
        return document;
    }

    public static void putDocument(File path, Document document)
    {
        if(sDocumentCache == null)
            sDocumentCache = new LruCache<>(LRU_CACHE_SIZE);
        sDocumentCache.put(path.getPath(), document);
        Log.i("StateSaver", "Document cache size: " + sDocumentCache.size());
    }
}
