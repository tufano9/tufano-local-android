package com.tufano.tufanomovil.global;

import android.database.Cursor;
import android.util.Log;

import com.tufano.tufanomovil.database.DBAdapter;

import java.util.ArrayList;

/**
 * Desarrollado por Gerson el 4/4/2016.
 */
public class FuncionesTablas
{
    public static ArrayList idsCargados(DBAdapter manager)
    {
        Cursor cursor = manager.cargarPedidosDetallesEditar();
        ArrayList<String> ids = new ArrayList<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            ids.add(cursor.getString(2));
        }
        return ids;
    }

    public static boolean estaAgregado(String modelo_producto, ArrayList ids_productos_cargados, String TAG)
    {
        for (int i = 0; i < ids_productos_cargados.size(); i++)
        {
            if( ids_productos_cargados.get(i).equals(modelo_producto) )
            {
                Log.w(TAG, "Ya esta agregado, no lo agrego..");
                return true;
            }
        }
        return false;
    }

    public static String calcularSubtotal(int pares, int bultos, double precio_producto)
    {
        double total = pares * bultos * precio_producto;
        return String.valueOf(total);
    }

    public static String obtenerNombreColor(String id_color, DBAdapter manager)
    {
        //Log.i(TAG, "Buscando color con id: "+id_color);
        String nombre_color = null;
        Cursor cursor = manager.buscarColor_ID(id_color);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            nombre_color = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Color encontrado: "+nombre_color);
        }
        cursor.close();
        return nombre_color;
    }

    public static String obtenerNombreTalla(String id_talla, DBAdapter manager)
    {
        //Log.i(TAG, "Buscando talla con id: " + id_talla);
        String nombre_talla = null;
        Cursor cursor = manager.buscarTalla_ID(id_talla);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            nombre_talla = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Talla encontrado: "+nombre_talla);
        }
        cursor.close();
        return nombre_talla;
    }

    public static String obtenerNombreTipo(String id_tipo, DBAdapter manager)
    {
        //Log.i(TAG, "Buscando tipo con id: " + id_tipo);
        String nombre_tipo = null;
        Cursor cursor = manager.buscarTipo_ID(id_tipo);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            nombre_tipo = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Tipo encontrado: "+nombre_tipo);
        }
        cursor.close();
        return nombre_tipo;
    }

    /**
     * Verifica si el producto esta habilitado.
     * @param estatus Estatus del producto.
     * @return True si el producto esta habilitado, False en caso contrario
     */
    public static boolean productoHabilitado(String estatus, String TAG)
    {
        //Log.d(TAG, "Verificando si el producto esta deshabilitado..");

        if(estatus.equals("1"))
        {
            //Log.i(TAG, "El producto esta habilitado..");
            return true;
        }
        else
        {
            Log.w(TAG, "El producto esta deshabilitado..");
            return false;
        }
    }

    /**
     * Calcula el numero total de pares por talla.
     * @param paresxtalla Pares por talla en el siguiente formato separado por comas: 2,2,3,2,2,1
     * @return Numero total de pares para la talla en cuestion.
     */
    public static String calcularPares(String paresxtalla)
    {
        int total = 0;
        String[] numeros = paresxtalla.split(",");

        for (String numero : numeros)
        {
            total += Integer.parseInt(numero);
        }

        return String.valueOf(total);
    }
}
