package com.tufano.tufanomovil.global;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tufano.tufanomovil.R;

import java.io.File;

public class FullScreenImage extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreenimages);

        // Obtiene el path de la imagen
        Bundle bundle = getIntent().getExtras();
        final String img_path = bundle.getString("img_path");
        final ImageView photo = (ImageView) findViewById(R.id.fullscreenphoto);

        if (img_path != null)
        {
            final Thread thread = new Thread()
            {
                @Override
                public void run()
                {
                    synchronized (this)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                // Crea un archivo a partir del path recibido del usuario.
                                File file = new File(img_path);

                                // Coloca la imagen en el imageView
                                photo.setImageBitmap(Funciones.decodeSampledBitmapFromResource(file,
                                        Constantes.IMG_FULL_SIZE_WIDTH,
                                        Constantes.IMG_FULL_SIZE_HEIGHT));

                                // Indica el tipo de escalado a utilizar
                                photo.setScaleType(ImageView.ScaleType.MATRIX);

                                // Habilita el zoom-in, zoom-out
                                FrameLayout view = (FrameLayout) findViewById(R.id.frameLayoutFullScreenImage);
                                view.setOnTouchListener(new PanAndZoomListener(view, photo, PanAndZoomListener.Anchor.TOPLEFT));
                            }
                        });
                    }
                }
            };
            thread.start();
        }
        else
            Log.d("FullScreenImage", "Imagen no encontrada..");
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
}