package com.tufano.tufanomovil.global;

import android.content.Context;
import android.content.Intent;

import com.tufano.tufanomovil.gestion.perfil.EditarPerfil;

public class FuncionesMenus
{
    /**
     * Metodo con el cual se mostrara el perfil del usuario conectado actualmente.
     */
    public static void mostrarPerfil(String usuario, Context contexto, Context from)
    {
        Intent c = new Intent(from, EditarPerfil.class);
        c.putExtra("usuario", usuario);
        c.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        contexto.startActivity(c);
    }

    /**
     * Metodo con el cual se mostrara las configuraciones del usuario conectado actualmente.
     */
    public static void mostrarConfiguracion(String usuario, Context contexto, Context from)
    {
        /*Intent c = new Intent(from, EditarPerfil.class);
        c.putExtra("usuario", usuario);
        c.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        contexto.startActivity(c);*/
    }
}
