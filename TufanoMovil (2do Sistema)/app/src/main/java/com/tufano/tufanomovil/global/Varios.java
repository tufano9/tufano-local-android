package com.tufano.tufanomovil.global;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.tufano.tufanomovil.R;

/**
 * Clase en construccion para templates de componentes.
 */
public class Varios
{
    /**
     * Dialogo por defecto de base.
     * @param context Contexto de la clase.
     */
    @SuppressWarnings("unused")
    private void dialogos(Context context)
    {
        // context = clase.this;
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        dialog.setTitle(R.string.confirmacion);
        dialog.setMessage(R.string.confirmacion_editar_producto);
        dialog.setCancelable(false);
        dialog.setPositiveButton("SI", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });

        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        dialog.show();
    }
}
