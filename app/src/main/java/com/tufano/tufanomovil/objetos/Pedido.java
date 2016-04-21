package com.tufano.tufanomovil.objetos;

public class Pedido
{
    private String id_pedido, razon_social, nombre_vendedor, monto_pedido, fecha_pedido,
            estatus_pedido, observaciones;
    //private ArrayList<Producto> productos;

    public Pedido(String id_pedido, String razon_social, String nombre_vendedor, String monto_pedido,
                  String fecha_pedido, String estatus_pedido, String observaciones)
    {
        this.id_pedido = id_pedido;
        this.razon_social = razon_social;
        this.nombre_vendedor = nombre_vendedor;
        this.monto_pedido = monto_pedido;
        this.fecha_pedido = fecha_pedido;
        this.estatus_pedido = estatus_pedido;
        this.observaciones = observaciones;
    }

    public String getId_pedido()
    {
        return id_pedido;
    }

    public String getRazon_social()
    {
        return razon_social;
    }

    public String getNombre_vendedor()
    {
        return nombre_vendedor;
    }

    public String getMonto_pedido()
    {
        return monto_pedido;
    }

    public String getFecha_pedido()
    {
        return fecha_pedido;
    }

    public String getEstatus_pedido()
    {
        return estatus_pedido;
    }

    public String getObservaciones()
    {
        return observaciones;
    }
}