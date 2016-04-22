package com.tufano.tufanomovil.objetos;

import android.content.Context;
import android.database.Cursor;

import com.tufano.tufanomovil.database.DBAdapter;

/**
 * Desarrollado por Gerson el 15/4/2016.
 */
public class Producto
{
    private String id_producto, id_talla, id_tipo, id_color, modelo, precio, numeracion, estatus,
            pares_talla, nombre_talla = null, nombre_tipo = null, nombre_color = null;

    private Context contexto;

    public Producto(String id_producto, String id_talla, String id_tipo, String id_color,
                    String modelo, String precio, String numeracion, String estatus,
                    String pares_talla, Context contexto)
    {
        this.id_producto = id_producto;
        this.id_talla = id_talla;
        this.id_tipo = id_tipo;
        this.id_color = id_color;
        this.modelo = modelo;
        this.precio = precio;
        this.numeracion = numeracion;
        this.estatus = estatus;
        this.pares_talla = pares_talla;
        this.contexto = contexto;
    }

    public String getId_producto()
    {
        return id_producto;
    }

    public String getId_talla()
    {
        return id_talla;
    }

    public String getNombreTalla()
    {
        if (nombre_talla == null)
            return buscarTallaxID(id_talla);
        else
            return nombre_talla;
    }

    public String getId_tipo()
    {
        return id_tipo;
    }

    public String getNombreTipo()
    {
        if (nombre_tipo == null)
            return buscarTipoxID(id_tipo);
        else
            return nombre_tipo;
    }

    public String getId_color()
    {
        return id_color;
    }

    public String getNombreColor()
    {
        if (nombre_color == null)
            return buscarColorxID(id_color);
        else
            return nombre_color;
    }

    public String getModelo()
    {
        return modelo;
    }

    public String getPrecio()
    {
        return precio;
    }

    public String getNumeracion()
    {
        return numeracion;
    }

    public String getEstatus()
    {
        return estatus;
    }

    public String getPares_talla()
    {
        return pares_talla;
    }

    /**
     * Obtiene el nombre del color de la base de datos, a partir de su ID.
     *
     * @param id_color ID del color a consultar.
     * @return Nombre del color.
     */
    private String buscarColorxID(String id_color)
    {
        //Log.i(TAG, "Buscando color con id: "+id_color);
        DBAdapter manager = new DBAdapter(contexto);
        Cursor    cursor  = manager.buscarColorxID(id_color);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {

            nombre_color = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Color encontrado: "+nombre_color);
        }
        cursor.close();
        //manager.cerrar();
        return nombre_color;
    }

    /**
     * Obtiene el nombre de la talla del producto de la BD a partir de su ID.
     *
     * @param id_talla ID de la talla.
     * @return Nombre de la talla del producto.
     */
    private String buscarTallaxID(String id_talla)
    {
        //Log.i(TAG, "Buscando talla con id: "+id_talla);
        DBAdapter manager = new DBAdapter(contexto);
        Cursor    cursor  = manager.buscarTalla_ID(id_talla);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            nombre_talla = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Talla encontrado: "+nombre_talla);
        }
        cursor.close();
        //manager.cerrar();
        return nombre_talla;
    }

    /**
     * Obtiene el nombre del tipo de producto de la BD a partir de su ID.
     *
     * @param id_tipo ID del tipo.
     * @return Nombre del tipo de producto.
     */
    private String buscarTipoxID(String id_tipo)
    {
        //Log.i(TAG, "Buscando tipo con id: "+id_tipo);
        DBAdapter manager = new DBAdapter(contexto);
        Cursor    cursor  = manager.buscarTipo_ID(id_tipo);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            nombre_tipo = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Tipo encontrado: "+nombre_tipo);
        }
        cursor.close();
        //manager.cerrar();
        return nombre_tipo;
    }
}