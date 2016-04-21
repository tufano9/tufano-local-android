package com.tufano.tufanomovil.database.altern;

import static com.tufano.tufanomovil.database.tables.Productos.CN_DESTACADO_PRODUCTO;
import static com.tufano.tufanomovil.database.tables.Productos.TABLA_PRODUCTOS;

public class Producto
{
    public static final String ALTER_TABLE_PRODUCTO_ADDDESTACADO = "ALTER TABLE " + TABLA_PRODUCTOS
            + " ADD COLUMN " + CN_DESTACADO_PRODUCTO + " INTEGER DEFAULT 0";
}
