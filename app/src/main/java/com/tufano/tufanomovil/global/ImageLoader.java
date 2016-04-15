package com.tufano.tufanomovil.global;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.tufano.tufanomovil.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    final         int    stub_id = R.drawable.icn_loading_img;
    private final String TAG     = "ImageLoader";
    MemoryCache memoryCache = new MemoryCache();
    FileCache       fileCache;
    ExecutorService executorService;
    Handler handler = new Handler(); //handler to display images in UI thread
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }

    public void DisplayImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);

        if (bitmap != null) {
            Log.i(TAG, "Esta en caché..");
            imageView.setImageBitmap(bitmap);
        }
        else {
            Log.i(TAG, "No esta en caché, descargando..");
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 70; // Sera cuadrada, 70x70
            int       width_tmp     = o.outWidth, height_tmp = o.outHeight;
            int       scale         = 1;

            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap          bitmap  = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        }
        catch (FileNotFoundException ignored) {
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getBitmap(String modelo) {
        File f = fileCache.getFile(modelo);

        //from cache
        Bitmap b = decodeFile(f);

        if (b != null) {
            Log.i(TAG, "getBitmap from cache");
            return b;
        }

        //from database
        try {
            Log.i(TAG, "getBitmap from database (" + modelo + ")");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/" + modelo + Constantes.EXTENSION_IMG);

                //Log.e(TAG, "Ruta: "+f.getAbsolutePath());
            }
            else {
                f = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/" + modelo + Constantes.EXTENSION_IMG);
            }

            if (!f.exists()) {
                Log.e(TAG, "La imagen no pudo ser localizada..");
            }

            return decodeFile(f);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);

        if (tag == null || !tag.equals(photoToLoad.url)) {
            Log.i(TAG, "imageViewReused");
            return true;
        }
        return false;
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

    //Task for the queue
    private class PhotoToLoad {
        public String    url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad))
                    return;

                Bitmap bmp = getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bmp);

                if (imageViewReused(photoToLoad))
                    return;

                BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            }
            catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap      bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(stub_id);
        }
    }
}