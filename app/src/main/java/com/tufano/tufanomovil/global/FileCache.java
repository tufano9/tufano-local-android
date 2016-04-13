package com.tufano.tufanomovil.global;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.net.URLEncoder;

public class FileCache {
    private final String TAG = "FileCache";
    private File cacheDir;

    public FileCache(Context context) {
        //Find the dir to save cached images
        /*if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            Log.i(TAG, "new File");
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "LazyList");
        }*/
        //else
        //{
        Log.i(TAG, "context");
        cacheDir = context.getCacheDir();
        //}
        if (!cacheDir.exists()) {
            if (cacheDir.mkdirs())
                Log.i(TAG, "Created Dir..");
            else
                Log.i(TAG, "Dir cannot be created..");
        }
    }

    public File getFile(String url) {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        //String filename = String.valueOf(url.hashCode());

        //Another possible solution (thanks to grantland)
        //noinspection deprecation
        String filename = URLEncoder.encode(url);

        Log.i(TAG, "URL: " + url + ", filename: " + filename);

        return new File(cacheDir, filename);
    }

    public void clear() {
        File[] files = cacheDir.listFiles();

        if (files == null)
            return;

        for (File f : files)
            if (f.delete())
                Log.i(TAG, "File Deleted");
    }
}