package com.tufano.tufanomovil.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.tufano.tufanomovil.global.Funciones;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.tufano.tufanomovil.database.tables.Clientes.CN_DIRECCION_CLIENTE;
import static com.tufano.tufanomovil.database.tables.Clientes.CN_EMAIL_CLIENTE;
import static com.tufano.tufanomovil.database.tables.Clientes.CN_ESTADO_CLIENTE;
import static com.tufano.tufanomovil.database.tables.Clientes.CN_ESTATUS_CLIENTE;
import static com.tufano.tufanomovil.database.tables.Clientes.CN_ID_CLIENTE;
import static com.tufano.tufanomovil.database.tables.Clientes.CN_RAZON_SOCIAL_CLIENTE;
import static com.tufano.tufanomovil.database.tables.Clientes.CN_RIF_CLIENTE;
import static com.tufano.tufanomovil.database.tables.Clientes.CN_TELEFONO_CLIENTE;
import static com.tufano.tufanomovil.database.tables.Clientes.TABLA_CLIENTES;
import static com.tufano.tufanomovil.database.tables.Colores.CN_ID_COLOR;
import static com.tufano.tufanomovil.database.tables.Colores.CN_NOMBRE_COLOR;
import static com.tufano.tufanomovil.database.tables.Colores.TABLA_COLORES;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_APELLIDO_VENDEDOR_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_CEDULA_VENDEDOR_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_DIRECCION_CLIENTE_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_EMAIL_CLIENTE_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_EMAIL_VENDEDOR_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_ESTADO_CLIENTE_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_ESTADO_VENDEDOR_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_ESTATUS_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_FECHA_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_ID_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_MONTO_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_NOMBRE_VENDEDOR_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_OBSERVACIONES_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_RAZON_SOCIAL_CLIENTE_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_RIF_CLIENTE_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_TELEFONO_CLIENTE_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.CN_TELEFONO_VENDEDOR_PEDIDO;
import static com.tufano.tufanomovil.database.tables.Pedidos.TABLA_PEDIDOS;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_BULTOS_PEDIDOS_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_ID_PEDIDOS_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_ID_PEDIDO_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_NUMERACION_PEDIDOS_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_PARES_PEDIDOS_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_PRECIO_UNITARIO_PEDIDOS_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_PRODUCTO_COLOR_PEDIDO_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_PRODUCTO_MODELO_PEDIDO_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_PRODUCTO_TIPO_PEDIDO_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_SUBTOTAL_PEDIDOS_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.CN_TALLA_PEDIDOS_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosDetalles.TABLA_PEDIDOS_DETALLES;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_APELLIDO_VENDEDOR_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_CEDULA_VENDEDOR_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_DIRECCION_CLIENTE_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_EMAIL_CLIENTE_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_EMAIL_VENDEDOR_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_ESTADO_CLIENTE_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_ESTADO_VENDEDOR_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_ESTATUS_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_FECHA_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_MONTO_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_NOMBRE_VENDEDOR_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_OBSERVACIONES_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_RAZON_SOCIAL_CLIENTE_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_RIF_CLIENTE_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_TELEFONO_CLIENTE_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.CN_TELEFONO_VENDEDOR_PEDIDO_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditar.TABLA_PEDIDOS_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_BULTOS_PEDIDOS_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_ID_PEDIDOS_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_ID_PEDIDO_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_NUMERACION_PEDIDOS_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_PARES_PEDIDOS_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_PRECIO_UNITARIO_PEDIDOS_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_PRODUCTO_COLOR_PEDIDO_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_PRODUCTO_MODELO_PEDIDO_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_PRODUCTO_TIPO_PEDIDO_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_SUBTOTAL_PEDIDOS_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.CN_TALLA_PEDIDOS_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosEditarDetalles.TABLA_PEDIDOS_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_BULTOS_PEDIDOS_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_ID_PEDIDO_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_ID_PRODUCTO_PEDIDO_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_NUMERACION_PEDIDOS_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_PARES_PEDIDOS_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_PRECIO_UNITARIO_PEDIDOS_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_PRODUCTO_COLOR_PEDIDO_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_PRODUCTO_MODELO_PEDIDO_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_PRODUCTO_TIPO_PEDIDO_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_SUBTOTAL_PEDIDOS_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.CN_TALLA_PEDIDOS_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.PedidosTemporales.TABLA_PEDIDOS_TEMPORALES;
import static com.tufano.tufanomovil.database.tables.Productos.CN_COLOR_PRODUCTO;
import static com.tufano.tufanomovil.database.tables.Productos.CN_ESTATUS_PRODUCTO;
import static com.tufano.tufanomovil.database.tables.Productos.CN_ID_PRODUCTO;
import static com.tufano.tufanomovil.database.tables.Productos.CN_MODELO_PRODUCTO;
import static com.tufano.tufanomovil.database.tables.Productos.CN_NUMERACION_PRODUCTO;
import static com.tufano.tufanomovil.database.tables.Productos.CN_PARES_TALLAS_PRODUCTO;
import static com.tufano.tufanomovil.database.tables.Productos.CN_PRECIO_PRODUCTO;
import static com.tufano.tufanomovil.database.tables.Productos.CN_TALLA_PRODUCTO;
import static com.tufano.tufanomovil.database.tables.Productos.CN_TIPO_PRODUCTO;
import static com.tufano.tufanomovil.database.tables.Productos.TABLA_PRODUCTOS;
import static com.tufano.tufanomovil.database.tables.Tallas.CN_ID_TALLA;
import static com.tufano.tufanomovil.database.tables.Tallas.CN_NOMBRE_TALLA;
import static com.tufano.tufanomovil.database.tables.Tallas.CN_NUMERACION_TALLA;
import static com.tufano.tufanomovil.database.tables.Tallas.TABLA_TALLAS;
import static com.tufano.tufanomovil.database.tables.Tipos.CN_ID_TIPO;
import static com.tufano.tufanomovil.database.tables.Tipos.CN_NOMBRE_TIPO;
import static com.tufano.tufanomovil.database.tables.Tipos.TABLA_TIPOS;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_APELLIDO;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_CEDULA;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_EMAIL;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_ESTADO;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_ID_USUARIO;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_KEY;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_NOMBRE;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_NOMBRE_USUARIO;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_PASSWORD;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_TELEFONO;
import static com.tufano.tufanomovil.database.tables.Usuarios.TABLA_USUARIO;

/**
 * Creado por Gerson on 11/01/2016.
 */
public class DBAdapter
{
    private final String TAG = "DBAdapter";
    private final DBHelper BD;
    public static SQLiteDatabase db = null;

    /**
     * Constructor de la clase.
     * @param context Contexto de la aplicacion.
     */
    public DBAdapter(Context context)
    {
        BD = DBHelper.getInstance(context);
        db = BD.getWritableDatabase();
    }

    @SuppressWarnings("unused")
    public void cerrar()
    {
        db.close();
        BD.close();
    }

    /* ----------------------------------- FUNCIONES VARIAS ----------------------------------- */

    private boolean existe_dato(String[] ids_BD, String ids_insert)
    {
        // Verificare si dentro de mi array existe el valor que ando buscando, en caso de existir retorno true
        boolean res = false;

        for (String id : ids_BD)
        {
            if (id.equals(ids_insert))
            {
                res = true;
                break;
            }
        }
        return !res;
    }

    private boolean existe_dato(String[] datos1, String nombre1, String[] datos2, String nombre2)
    {
        // Verificare si dentro de mi array existe el valor que ando buscando, en caso de existir retorno true
        boolean res = false;

        for (String id : datos1)
        {
            if (id.equals(nombre1))
            {
                res = true;
                break;
            }
        }

        for (String id : datos2)
        {
            if (id.equals(nombre2))
            {
                res = true;
                break;
            }
        }
        return !res;
    }

    /**
     * get datetime
     * */
    private String getDateTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    /* ---------------------------------- FUNCIONES USUARIOS ---------------------------------- */

    private Cursor cargarCursorUsuario()
    {
        String[] columnas = new String[]{CN_ID_USUARIO, CN_NOMBRE_USUARIO, CN_NOMBRE, CN_APELLIDO, CN_CEDULA, CN_TELEFONO, CN_EMAIL, CN_PASSWORD};
        return db.query(TABLA_USUARIO,columnas,null,null,null,null,null);
    }

    public Cursor buscarUsuario(String nombre_usuario)
    {
        String[] columnas = new String[]{CN_ID_USUARIO, CN_NOMBRE_USUARIO, CN_NOMBRE, CN_APELLIDO, CN_CEDULA, CN_TELEFONO, CN_EMAIL, CN_PASSWORD, CN_PASSWORD, CN_ESTADO, CN_KEY};
        String[] args = { nombre_usuario };
        return db.query(TABLA_USUARIO,columnas,CN_NOMBRE_USUARIO+"=?",args,null,null,null);
    }

    public List<String> buscarUsuario_ID(String id_usuario)
    {
        List<String> contenedor = new ArrayList<>();

        String[] columnas = new String[]{ CN_NOMBRE_USUARIO, CN_NOMBRE, CN_APELLIDO, CN_CEDULA, CN_TELEFONO, CN_EMAIL, CN_ESTADO, CN_PASSWORD, CN_KEY};
        String[] args = { id_usuario };
        Cursor cursor = db.query(TABLA_USUARIO, columnas, CN_ID_USUARIO + "=?", args, null, null, null);

        if (cursor.moveToFirst())
        {
            contenedor.add(cursor.getString(0)); // username
            contenedor.add(cursor.getString(1)); // nombre
            contenedor.add(cursor.getString(2)); // apellido
            contenedor.add(cursor.getString(3)); // cedula
            contenedor.add(cursor.getString(4)); // telefono
            contenedor.add(cursor.getString(5)); // email
            contenedor.add(cursor.getString(6)); // estado
        }
        cursor.close();
        return contenedor;
    }

    public List<String> buscarUsuario_ID2(String id_usuario)
    {
        List<String> contenedor = new ArrayList<>();

        String[] columnas = new String[]{ CN_CEDULA, CN_NOMBRE, CN_APELLIDO, CN_EMAIL, CN_TELEFONO, CN_ESTADO};
        String[] args = { id_usuario };
        Cursor cursor = db.query(TABLA_USUARIO, columnas, CN_ID_USUARIO + "=?", args, null, null, null);

        if (cursor.moveToFirst())
        {
            contenedor.add(cursor.getString(0)); // CN_CEDULA
            contenedor.add(cursor.getString(1)); // CN_NOMBRE
            contenedor.add(cursor.getString(2)); // CN_APELLIDO
            contenedor.add(cursor.getString(3)); // CN_EMAIL
            contenedor.add(cursor.getString(4)); // CN_TELEFONO
            contenedor.add(cursor.getString(5)); // CN_ESTADO
        }
        cursor.close();
        return contenedor;
    }

    /**
     * Funcion encargada de conseguir los usuarios con una cedula igual a la indicada pero con un
     * id distinto, es decir, para saber si hay otro usuario aparte del que se esta indicando,
     * que posea la cedula.
     * @param cedula Cedula del usuario que se debe buscar.
     * @param id_usuario Id del cliente que se obviara para la busqueda
     * @return Cursor con los datos resultantes de la consulta a la BD.
     */
    public Cursor buscarUsuarioCedula_ID(String cedula, String id_usuario)
    {
        String[] columnas = new String[]{CN_ID_USUARIO};
        String[] args = { cedula, id_usuario };
        return db.query(TABLA_USUARIO, columnas, CN_CEDULA + "=? AND " + CN_ID_USUARIO + " !=?", args, null, null, null);
    }

    @SuppressWarnings("unused")
    public void insertar_usuario(String nombre_usuario, String cedula, String nombre, String apellido, String telefono, String email, String pass, String estado, String key)
    {
        Cursor c = cargarCursorUsuario();
        ArrayList<String> usernames = new ArrayList<>(); // nombre_usuario

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
            usernames.add(c.getString(1)); // nombre_usuario
        String[] ids = usernames.toArray(new String[usernames.size()]);

        //nullColumnHack = indicar que parametros seran opcionales (CN_Phone es opcional) de lo contrario se pone null
        if(existe_dato(ids, nombre_usuario))
        {
            ContentValues valores = new ContentValues();
            valores.put(CN_NOMBRE_USUARIO,nombre_usuario);
            valores.put(CN_CEDULA,cedula);
            valores.put(CN_NOMBRE,nombre);
            valores.put(CN_APELLIDO,apellido);
            valores.put(CN_TELEFONO,telefono);
            valores.put(CN_EMAIL,email);
            valores.put(CN_PASSWORD,pass);
            valores.put(CN_ESTADO,estado);
            valores.put(CN_KEY,key);

            if (db.insert(TABLA_USUARIO, null, valores)==-1)
                Log.e(TAG, "Ha ocurrido un error insertando el registro..");
            else
                Log.e(TAG, "Registro agregado exitosamente..");
        }
        else
        {
            Log.w("insertar_usuario", "El usuario ya existe, no se insertara en BD Local!");
        }
    }

    public long editarUsuario(String id_usuario, ArrayList<String> dato, boolean cambiarPass)
    {
        ContentValues valores_nuevos = new ContentValues();

        valores_nuevos.put(CN_NOMBRE, dato.get(0) );
        valores_nuevos.put(CN_APELLIDO, dato.get(1) );
        valores_nuevos.put(CN_EMAIL, dato.get(2) );
        valores_nuevos.put(CN_TELEFONO, dato.get(3) );
        valores_nuevos.put(CN_CEDULA, dato.get(4) );
        valores_nuevos.put(CN_ESTADO, dato.get(6));

        if(cambiarPass)
        {
            String key = getUserKey(id_usuario);
            String encryptedPassword = null;

            try
            {
                encryptedPassword = new String(Funciones.encrypt(key, dato.get(7)), "UTF-8");
            }
            catch (UnsupportedEncodingException | GeneralSecurityException e)
            {
                e.printStackTrace();
            }
            valores_nuevos.put(CN_PASSWORD, encryptedPassword );
        }

        return db.update(TABLA_USUARIO, valores_nuevos, CN_ID_USUARIO + "=?", new String[]{id_usuario});
    }

    public String getUserKey(String id)
    {
        String[] columnas = new String[]{ CN_KEY };
        String[] args = { id };
        String key = null;
        Cursor cursor = db.query(TABLA_USUARIO,columnas,CN_ID_USUARIO+"=?",args,null,null,null);
        if (cursor.moveToFirst())
        {
            key = cursor.getString(0);
        }
        cursor.close();
        return key;
    }

    public String obtenerPassword(String id)
    {
        String[] columnas = new String[]{ CN_PASSWORD };
        String[] args = { id };
        String password = null;
        Cursor cursor = db.query(TABLA_USUARIO, columnas, CN_ID_USUARIO + "=?", args, null, null, null);
        if (cursor.moveToFirst())
        {
            password = cursor.getString(0);
        }
        cursor.close();
        return password;
    }

    /* ----------------------------------- FUNCIONES PRODUCTOS ----------------------------------- */

    public Cursor cargarProductos()
    {
        String[] columnas = new String[]{CN_ID_PRODUCTO, CN_TALLA_PRODUCTO, CN_TIPO_PRODUCTO, CN_MODELO_PRODUCTO, CN_COLOR_PRODUCTO, CN_PRECIO_PRODUCTO, CN_NUMERACION_PRODUCTO, CN_ESTATUS_PRODUCTO, CN_PARES_TALLAS_PRODUCTO};
        return db.query(TABLA_PRODUCTOS, columnas, null, null, null, null, null);
    }

    public Cursor cargarProductos_Filtrado_Ordenado(String tipo, String talla, String color, String modelo, String columna_ordenada, String orden)
    {
        String where = null;
        String orderby;
        String join = null;

        final String columns = CN_ID_PRODUCTO+", "+CN_TALLA_PRODUCTO+", "+CN_TIPO_PRODUCTO+", "+CN_MODELO_PRODUCTO+", "+CN_COLOR_PRODUCTO+", "+CN_PRECIO_PRODUCTO+", "+CN_NUMERACION_PRODUCTO+", "+CN_ESTATUS_PRODUCTO+", "+CN_PARES_TALLAS_PRODUCTO;

        /* Generando el ORDER BY */
        switch (columna_ordenada)
        {
            case "monto":
                orderby = "CAST(" + columna_ordenada.toLowerCase() + " AS DECIMAL(15,2)) " + orden;
                break;
            case "color":
                join = TABLA_COLORES + " b ON a." + CN_COLOR_PRODUCTO + " = b." + CN_ID_COLOR;
                orderby = "b." + CN_NOMBRE_COLOR + " " + orden;
                break;
            default:
                orderby = columna_ordenada.toLowerCase() + " " + orden;
                break;
        }

        /* Generando el WHERE */

        ArrayList<String> argumentos = new ArrayList<>();

        if(tipo!=null)
        {
            where = CN_TIPO_PRODUCTO + "=?";
            argumentos.add(tipo);
        }
        if(talla!=null)
        {
            argumentos.add(talla);

            if(where!=null)
                where += " AND " + CN_TALLA_PRODUCTO + "=?";
            else
                where = CN_TALLA_PRODUCTO + "=?";
        }
        if(color!=null)
        {
            argumentos.add(color);

            if(where!=null)
                where += " AND " + CN_COLOR_PRODUCTO + "=?";
            else
                where = CN_COLOR_PRODUCTO + "=?";
        }
        if(modelo!=null)
        {
            argumentos.add("%" + modelo + "%");

            if(where!=null)
                where += " AND " + CN_MODELO_PRODUCTO + " LIKE ?";
            else
                where = CN_MODELO_PRODUCTO + " LIKE ?";
        }

        String[] args = argumentos.toArray(new String[argumentos.size()]);

        if(join!=null)
        {
            if(where != null)
            {
                String MY_QUERY = "SELECT "+columns+" FROM "+TABLA_PRODUCTOS+" a INNER JOIN "+join+" WHERE "+where+" ORDER BY "+orderby;
                Log.i(TAG, "Query + join: "+MY_QUERY);
                return db.rawQuery(MY_QUERY, args);
            }
            else
            {
                String MY_QUERY = "SELECT "+columns+" FROM "+TABLA_PRODUCTOS+" a INNER JOIN "+join+" ORDER BY "+orderby;
                Log.i(TAG, "Query + join: "+MY_QUERY);
                return db.rawQuery(MY_QUERY, args);
            }
        }
        else
        {
            if(where != null)
            {
                String MY_QUERY = "SELECT "+columns+" FROM "+TABLA_PRODUCTOS+" WHERE "+where+" ORDER BY "+orderby;
                Log.i(TAG, "Query: "+MY_QUERY);
                return db.rawQuery(MY_QUERY, args);
            }
            else
            {
                String MY_QUERY = "SELECT "+columns+" FROM "+TABLA_PRODUCTOS+" ORDER BY "+orderby;
                Log.i(TAG, "Query: "+MY_QUERY);
                return db.rawQuery(MY_QUERY, args);
            }
        }
        //return db.query(TABLA_PRODUCTOS, columnas, where, args, null, null, orderby);
    }

    public Cursor cargarProductosId(String id)
    {
        String[] columnas = new String[]{CN_MODELO_PRODUCTO};
        String[] args = { id };
        return db.query(TABLA_PRODUCTOS, columnas, CN_ID_PRODUCTO + "=?", args, null, null, null);
    }

    public Cursor cargarProductosModelo(String modelo)
    {
        String[] columnas = new String[]{CN_ID_PRODUCTO};
        String[] args = { modelo };
        return db.query(TABLA_PRODUCTOS, columnas, CN_MODELO_PRODUCTO + "=?", args, null, null, null);
    }

    public long agregarProducto(String[] datos)
    {
        Log.i(TAG, "Los datos del producto a agregar son los siguientes: -Talla: " + datos[0] + " -Tipo: " + datos[1] + " -Modelo: " + datos[4] + " -Precio: " + datos[2] + " -Color: " + datos[3] + " -Numeracion: " + datos[5]);
        ArrayList<String> modelos = new ArrayList<>(); // modelos

        Cursor c = cargarProductos();

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
            modelos.add(c.getString(3)); // CN_MODELO_PRODUCTO

        String[] ids = modelos.toArray(new String[modelos.size()]);

        if(existe_dato(ids, datos[4]))
        {
            ContentValues valores = new ContentValues();
            valores.put(CN_TALLA_PRODUCTO, datos[0]);
            valores.put(CN_TIPO_PRODUCTO, datos[1]);
            valores.put(CN_PRECIO_PRODUCTO, datos[2]);
            valores.put(CN_COLOR_PRODUCTO, datos[3]);
            valores.put(CN_MODELO_PRODUCTO, datos[4]);
            valores.put(CN_NUMERACION_PRODUCTO, datos[5]);
            valores.put(CN_PARES_TALLAS_PRODUCTO, datos [6]);
            valores.put(CN_ESTATUS_PRODUCTO, "1");

            return db.insert(TABLA_PRODUCTOS, null, valores);
        }
        else
        {
            Log.w("agregarProducto", "El producto ya existe, no se insertara en BD Local! (Revise el modelo)");
            return -1;
        }
    }

    public long editarProducto(String id, String talla, String tipo, String modelo, String color, String precio, String numeracion, String estatus, String paresxtalla)
    {
        ContentValues valores_nuevos = new ContentValues();
        valores_nuevos.put(CN_TALLA_PRODUCTO, talla);
        valores_nuevos.put(CN_TIPO_PRODUCTO, tipo);
        valores_nuevos.put(CN_MODELO_PRODUCTO, modelo);
        valores_nuevos.put(CN_COLOR_PRODUCTO, color);
        valores_nuevos.put(CN_PRECIO_PRODUCTO, precio);
        valores_nuevos.put(CN_NUMERACION_PRODUCTO, numeracion);
        valores_nuevos.put(CN_ESTATUS_PRODUCTO, estatus);
        valores_nuevos.put(CN_PARES_TALLAS_PRODUCTO, paresxtalla);

        return db.update(TABLA_PRODUCTOS, valores_nuevos, CN_ID_PRODUCTO + "=?", new String[]{id});
    }

    @SuppressWarnings("unused")
    public long eliminarProducto(String id)
    {
        return db.delete(TABLA_PRODUCTOS, CN_ID_PRODUCTO + "=?", new String[]{id});
    }

    /* ----------------------------------- FUNCIONES TALLAS ----------------------------------- */

    /**
     * Getting all labels
     * returns list of labels
     * */
    public List<List<String>> cargarListaTallas()
    {
        List<String> tallas = new ArrayList<>();
        List<String> ids_tallas = new ArrayList<>();
        List<String> numeraciones = new ArrayList<>();
        List<List<String>> contenedor = new ArrayList<>();
        tallas.add("Seleccione una talla..");

        String[] columnas = new String[]{CN_ID_TALLA, CN_NOMBRE_TALLA, CN_NUMERACION_TALLA};
        Cursor cursor = db.query(TABLA_TALLAS, columnas, null, null, null, null, null);

        if (cursor.moveToFirst())
        {
            do
            {
                ids_tallas.add(cursor.getString(0));
                tallas.add(cursor.getString(1) + cursor.getString(2));
                numeraciones.add(cursor.getString(2));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        contenedor.add(ids_tallas);
        contenedor.add(tallas);
        contenedor.add(numeraciones);
        return contenedor;
    }

    public Cursor buscarTalla_ID(String id_talla)
    {
        String[] columnas = new String[]{CN_NOMBRE_TALLA, CN_NUMERACION_TALLA};
        String[] args = { id_talla };
        return db.query(TABLA_TALLAS, columnas, CN_ID_TALLA + "=?", args, null, null, null);
    }

    public Cursor cargarTallas()
    {
        String[] columnas = new String[]{CN_ID_TALLA, CN_NOMBRE_TALLA, CN_NUMERACION_TALLA};
        return db.query(TABLA_TALLAS, columnas, null, null, null, null, null);
    }

    public Cursor cargarTallas_nombre(String nombre)
    {
        String[] columnas = new String[]{CN_ID_TALLA};
        String[] args = { nombre };
        return db.query(TABLA_TALLAS, columnas, CN_NOMBRE_TALLA + "=?", args, null, null, null);
    }

    public Cursor cargarTallas_nombreID(String nombre, String id)
    {
        String[] columnas = new String[]{CN_ID_TALLA};
        String[] args = { nombre, id };
        return db.query(TABLA_TALLAS, columnas, CN_NOMBRE_TALLA + "=? AND " + CN_ID_TALLA + "!=?", args, null, null, null);
    }

    /**
     * Funcion encargada de agregar una talla a la base de datos local
     * @param nombre Nombre de la talla a agregar, por ej. "P"
     * @param numeracion Numeracion de la talla a agregar, por ej. "(18-25)"
     *              Array de los datos a insertar en la tabla datos [nombre_talla, numeracion]
     * @return
     *              Si la talla fue agregada exitosamente, retorna el ID de la talla insertada..
     *              Si el nombre de la talla (P, M, G) ya existe, retorna -2..
     *              Si ocurrio algun error insertando la talla, retorna -1..
     */
    public long agregarTallas(String nombre, String numeracion)
    {
        ArrayList<String> tallas = new ArrayList<>(); // tallas
        Cursor c = cargarTallas();

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
            tallas.add(c.getString(1));

        String[] ids = tallas.toArray(new String[tallas.size()]);

        // Verifico si la talla ya existe (nombre_talla)
        if(existe_dato(ids, nombre))
        {
            ContentValues valores = new ContentValues();
            valores.put(CN_NOMBRE_TALLA, nombre);
            valores.put(CN_NUMERACION_TALLA, numeracion);

            return db.insert(TABLA_TALLAS, null, valores);
        }
        else
        {
            Log.w("agregarProducto", "La talla ya existe, no se insertara en BD Local! (Revise el nombre de la talla)");
            return -2;
        }
    }

    public long editarTallas(String id, String nombre, String numeracion)
    {
        ContentValues valores_nuevos = new ContentValues();
        valores_nuevos.put(CN_NOMBRE_TALLA, nombre);
        valores_nuevos.put(CN_NUMERACION_TALLA, numeracion);

        return db.update(TABLA_TALLAS, valores_nuevos, CN_ID_TALLA + "=?", new String[]{id});
    }

    /* ----------------------------------- FUNCIONES TIPOS ----------------------------------- */

    /**
     * Getting all labels
     * returns list of labels
     * */
    public List<List<String>> cargarListaTipos()
    {
        List<String> tipos = new ArrayList<>();
        List<String> ids_tipos = new ArrayList<>();
        List<List<String>> contenedor = new ArrayList<>();
        tipos.add("Seleccione un tipo..");
        String orderBy = CN_NOMBRE_TIPO + " ASC";

        String[] columnas = new String[]{CN_ID_TIPO, CN_NOMBRE_TIPO};
        Cursor cursor = db.query(TABLA_TIPOS, columnas, null, null, null, null, orderBy);

        if (cursor.moveToFirst())
        {
            do
            {
                ids_tipos.add(cursor.getString(0));
                tipos.add(cursor.getString(1));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        contenedor.add(ids_tipos);
        contenedor.add(tipos);
        return contenedor;
    }

    public Cursor buscarTipo_ID(String id_tipo)
    {
        String[] columnas = new String[]{CN_NOMBRE_TIPO};
        String[] args = { id_tipo };
        return db.query(TABLA_TIPOS, columnas, CN_ID_TIPO + "=?", args, null, null, null);
    }

    public Cursor cargarTipos()
    {
        String[] columnas = new String[]{CN_ID_TIPO, CN_NOMBRE_TIPO};
        String orderBy = CN_NOMBRE_TIPO + " ASC";
        return db.query(TABLA_TIPOS, columnas, null, null, null, null, orderBy);
    }

    public Cursor cargarTipos_nombre(String nombre)
    {
        String[] columnas = new String[]{CN_ID_TIPO};
        String[] args = { nombre };
        return db.query(TABLA_TIPOS, columnas, CN_NOMBRE_TIPO + "=?", args, null, null, null);
    }

    public Cursor cargarTipos_nombreID(String nombre, String id)
    {
        String[] columnas = new String[]{CN_ID_TIPO};
        String[] args = { nombre, id };
        return db.query(TABLA_TIPOS, columnas, CN_NOMBRE_TIPO + "=? AND " + CN_ID_TIPO + "!=?", args, null, null, null);
    }

    public long agregarTipos(String nombre)
    {
        ArrayList<String> tallas = new ArrayList<>(); // tallas
        Cursor c = cargarTipos();

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
            tallas.add(c.getString(1));

        String[] ids = tallas.toArray(new String[tallas.size()]);

        // Verifico si el tipo ya existe (nombre_tipo)
        if(existe_dato(ids, nombre))
        {
            ContentValues valores = new ContentValues();
            valores.put(CN_NOMBRE_TIPO, nombre);
            return db.insert(TABLA_TIPOS, null, valores);
        }
        else
        {
            Log.w(TAG, "El tipo ya existe, no se insertara en BD Local! (Revise el nombre del tipo)");
            return -2;
        }
    }

    public long editarTipos(String id, String nombre)
    {
        ContentValues valores_nuevos = new ContentValues();
        valores_nuevos.put(CN_NOMBRE_TIPO, nombre);

        return db.update(TABLA_TIPOS, valores_nuevos, CN_ID_TIPO + "=?", new String[]{id});
    }

    /* ----------------------------------- FUNCIONES COLORES ----------------------------------- */

    /**
     * Getting all labels
     * returns list of labels
     * */
    public List<List<String>> cargarListaColores()
    {
        List<String> colores = new ArrayList<>();
        List<String> ids_colores = new ArrayList<>();
        List<List<String>> contenedor = new ArrayList<>();
        colores.add("Seleccione un color..");
        String orderBy = CN_NOMBRE_COLOR + " ASC";

        String[] columnas = new String[]{CN_ID_COLOR, CN_NOMBRE_COLOR};
        Cursor cursor = db.query(TABLA_COLORES, columnas, null, null, null, null, orderBy);

        if (cursor.moveToFirst())
        {
            do
            {
                ids_colores.add(cursor.getString(0));
                colores.add(cursor.getString(1));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        contenedor.add(ids_colores);
        contenedor.add(colores);
        return contenedor;
    }

    public Cursor buscarColor_ID(String id_color)
    {
        String[] columnas = new String[]{CN_NOMBRE_COLOR};
        String[] args = { id_color };
        return db.query(TABLA_COLORES, columnas, CN_ID_COLOR + "=?", args, null, null, null);
    }

    public Cursor cargarColores()
    {
        String[] columnas = new String[]{CN_ID_COLOR, CN_NOMBRE_COLOR};
        String orderBy = CN_NOMBRE_COLOR + " ASC";
        return db.query(TABLA_COLORES, columnas, null, null, null, null, orderBy);
    }

    public Cursor cargarColores_nombre(String nombre)
    {
        String[] columnas = new String[]{CN_ID_COLOR};
        String[] args = { nombre };
        return db.query(TABLA_COLORES, columnas, CN_NOMBRE_COLOR + "=?", args, null, null, null);
    }

    public Cursor cargarColores_nombreID(String nombre, String id)
    {
        String[] columnas = new String[]{CN_ID_COLOR};
        String[] args = { nombre, id };
        return db.query(TABLA_COLORES, columnas, CN_NOMBRE_COLOR + "=? AND " + CN_ID_COLOR + "!=?", args, null, null, null);
    }

    public long agregarColores(String nombre)
    {
        ArrayList<String> tallas = new ArrayList<>(); // colores
        Cursor c = cargarColores();

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
            tallas.add(c.getString(1));

        String[] ids = tallas.toArray(new String[tallas.size()]);

        // Verifico si el tipo ya existe (nombre_tipo)
        if(existe_dato(ids, nombre))
        {
            ContentValues valores = new ContentValues();
            valores.put(CN_NOMBRE_COLOR, nombre);
            return db.insert(TABLA_COLORES, null, valores);
        }
        else
        {
            Log.w(TAG, "El color ya existe, no se insertara en BD Local! (Revise el nombre del color)");
            return -2;
        }
    }

    public long editarColores(String id, String nombre)
    {
        ContentValues valores_nuevos = new ContentValues();
        valores_nuevos.put(CN_NOMBRE_COLOR, nombre);

        return db.update(TABLA_COLORES, valores_nuevos, CN_ID_COLOR + "=?", new String[]{id});
    }

    /* ----------------------------------- FUNCIONES CLIENTES ----------------------------------- */

    public List<List<String>> cargarListaClientes()
    {
        List<String> razones_sociales = new ArrayList<>();
        List<String> ids_clientes = new ArrayList<>();
        List<List<String>> contenedor = new ArrayList<>();
        razones_sociales.add("Seleccione un cliente..");
        String orderBy = CN_RAZON_SOCIAL_CLIENTE + " ASC";

        String[] columnas = new String[]{CN_ID_CLIENTE, CN_RAZON_SOCIAL_CLIENTE};
        Cursor cursor = db.query(TABLA_CLIENTES, columnas, null, null, null, null, orderBy);

        if (cursor.moveToFirst())
        {
            do
            {
                ids_clientes.add(cursor.getString(0));
                razones_sociales.add(cursor.getString(1));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        contenedor.add(ids_clientes);
        contenedor.add(razones_sociales);
        return contenedor;
    }

    public List<String> cargarDatosClientes(String id)
    {
        String razones_sociales = null;
        String rif = null;
        String estado = null;
        String telefono = null;
        String email = null;
        String direccion = null;
        String estatus = null;
        List<String> contenedor = new ArrayList<>();

        String[] columnas = new String[]{ CN_RAZON_SOCIAL_CLIENTE, CN_RIF_CLIENTE, CN_ESTADO_CLIENTE, CN_TELEFONO_CLIENTE, CN_EMAIL_CLIENTE, CN_DIRECCION_CLIENTE, CN_ESTATUS_CLIENTE};
        Cursor cursor = db.query(TABLA_CLIENTES, columnas, CN_ID_CLIENTE + "=?", new String[]{id}, null, null, null);

        if (cursor.moveToFirst())
        {
            razones_sociales = cursor.getString(0);
            rif = cursor.getString(1);
            estado = cursor.getString(2);
            telefono = cursor.getString(3);
            email = cursor.getString(4);
            direccion = cursor.getString(5);
            estatus = cursor.getString(6);
        }

        cursor.close();
        contenedor.add(razones_sociales);
        contenedor.add(rif);
        contenedor.add(estado);
        contenedor.add(telefono);
        contenedor.add(email);
        contenedor.add(direccion);
        contenedor.add(estatus);
        return contenedor;
    }

    public Cursor cargarClientes()
    {
        String[] columnas = new String[]{ CN_ID_CLIENTE, CN_RAZON_SOCIAL_CLIENTE, CN_RIF_CLIENTE,
                CN_ESTADO_CLIENTE, CN_TELEFONO_CLIENTE, CN_EMAIL_CLIENTE, CN_DIRECCION_CLIENTE,
                CN_ESTATUS_CLIENTE };
        String orderBy = CN_RAZON_SOCIAL_CLIENTE + " ASC";
        return db.query(TABLA_CLIENTES, columnas, null, null, null, null, orderBy);
    }

    public Cursor cargarClientesFiltrado(String estado, String razon_social, String columna_ordenada, String orden)
    {
        String[] columnas = new String[]{ CN_ID_CLIENTE, CN_RAZON_SOCIAL_CLIENTE, CN_RIF_CLIENTE,
                CN_ESTADO_CLIENTE, CN_TELEFONO_CLIENTE, CN_EMAIL_CLIENTE, CN_DIRECCION_CLIENTE,
                CN_ESTATUS_CLIENTE };

        String selection = null;
        ArrayList<String> argumentos = new ArrayList<>();
        String orderby = columna_ordenada.toLowerCase() + " " + orden;

        if(estado!=null)
        {
            selection = CN_ESTADO_CLIENTE + " LIKE ?";
            argumentos.add("%" + estado + "%");
        }
        if(razon_social!=null)
        {
            argumentos.add("%" + razon_social + "%");

            if(selection!=null)
                selection += " AND " + CN_RAZON_SOCIAL_CLIENTE + " LIKE ?";
            else
                selection = CN_RAZON_SOCIAL_CLIENTE + " LIKE ?";
        }

        String[] args = argumentos.toArray(new String[argumentos.size()]);

        return db.query(TABLA_CLIENTES, columnas, selection, args, null, null, orderby);
    }

    public Cursor cargarClientesNombre(String rs, String rif, String id_cliente)
    {
        String[] columnas = new String[]{ CN_ID_CLIENTE };
        String[] args = new String[]{rs, rif, id_cliente};
        return db.query(TABLA_CLIENTES, columnas, "(" + CN_RAZON_SOCIAL_CLIENTE + "=? OR " + CN_RIF_CLIENTE + "=?) AND " + CN_ID_CLIENTE + " !=?", args, null, null, null);
    }

    public long agregarCliente(String[] datos)
    {
        ArrayList<String> rs = new ArrayList<>(); // Razon social
        ArrayList<String> rif = new ArrayList<>(); // Rif
        Cursor c = cargarClientes();

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
        {
            rs.add(c.getString(1));
            rif.add(c.getString(2));
        }

        String[] razones_sociales = rs.toArray(new String[rs.size()]);
        String[] rifs = rs.toArray(new String[rif.size()]);

        // Verifico si el tipo ya existe (nombre_tipo)
        if( existe_dato( razones_sociales, datos[1], rifs, datos[2] ) )
        {
            ContentValues valores = new ContentValues();
            valores.put(CN_RAZON_SOCIAL_CLIENTE, datos[0]);
            valores.put(CN_RIF_CLIENTE, datos[1]);
            valores.put(CN_ESTADO_CLIENTE, datos[2]);
            valores.put(CN_TELEFONO_CLIENTE, datos[3]);
            valores.put(CN_EMAIL_CLIENTE, datos[4]);
            valores.put(CN_DIRECCION_CLIENTE, datos[5]);
            valores.put(CN_ESTATUS_CLIENTE, 1);
            return db.insert(TABLA_CLIENTES, null, valores);
        }
        else
        {
            Log.w(TAG, "El cliente ya existe, no se insertara en BD Local!");
            return -2;
        }
    }

    public long editarCliente(String id, String[] datos)
    {
        ContentValues valores_nuevos = new ContentValues();
        valores_nuevos.put(CN_RAZON_SOCIAL_CLIENTE, datos[0]);
        valores_nuevos.put(CN_RIF_CLIENTE, datos[1]);
        valores_nuevos.put(CN_ESTADO_CLIENTE, datos[2]);
        valores_nuevos.put(CN_TELEFONO_CLIENTE, datos[3]);
        valores_nuevos.put(CN_EMAIL_CLIENTE, datos[4]);
        valores_nuevos.put(CN_DIRECCION_CLIENTE, datos[5]);
        valores_nuevos.put(CN_ESTATUS_CLIENTE, datos[6]);

        return db.update(TABLA_CLIENTES, valores_nuevos, CN_ID_CLIENTE + "=?", new String[]{id});
    }

    public Cursor cargarClientesNombre(String rs, String rif)
    {
        String[] columnas = new String[]{ CN_ID_CLIENTE };
        String[] args = new String[]{rs, rif};
        return db.query(TABLA_CLIENTES, columnas, CN_RAZON_SOCIAL_CLIENTE + "=? OR " + CN_RIF_CLIENTE + "=?", args, null, null, null);

    }

    /* ----------------------------------- FUNCIONES PEDIDOS ----------------------------------- */

    @SuppressWarnings("unused")
    public Cursor cargarPedidos()
    {
        String[] columnas = new String[]{ CN_ID_PEDIDO, CN_RAZON_SOCIAL_CLIENTE_PEDIDO, CN_NOMBRE_VENDEDOR_PEDIDO, CN_MONTO_PEDIDO, CN_FECHA_PEDIDO, CN_ESTATUS_PEDIDO, CN_OBSERVACIONES_PEDIDO };
        return db.query(TABLA_PEDIDOS, columnas, null, null, null, null, null);
    }

    public Cursor cargarPedidosOrdenadosPor(String columna_ordenada, String orden, String cliente_filtrado, String estatus_filtrado)
    {
        String[] columnas = new String[]{ CN_ID_PEDIDO, CN_RAZON_SOCIAL_CLIENTE_PEDIDO , CN_NOMBRE_VENDEDOR_PEDIDO , CN_MONTO_PEDIDO, CN_FECHA_PEDIDO, CN_ESTATUS_PEDIDO, CN_OBSERVACIONES_PEDIDO };
        String orderby;
        String selection = null;
        ArrayList<String> argumentos = new ArrayList<>();

        if(cliente_filtrado!=null)
        {
            selection = CN_RAZON_SOCIAL_CLIENTE_PEDIDO + "=?";
            argumentos.add(cliente_filtrado);
        }
        if(estatus_filtrado!=null)
        {
            argumentos.add(estatus_filtrado);

            if(selection!=null)
                selection += " AND " + CN_ESTATUS_PEDIDO + "=?";
            else
                selection = CN_ESTATUS_PEDIDO + "=?";
        }

        if (columna_ordenada.equals("monto"))
            orderby = "CAST("+columna_ordenada.toLowerCase()+" AS DECIMAL(15,2)) " + orden;
        else
            orderby = columna_ordenada.toLowerCase() + " " + orden;

        String[] args = argumentos.toArray(new String[argumentos.size()]);

        return db.query(TABLA_PEDIDOS, columnas, selection, args, null, null, orderby);
    }

    /**
     * Metodo para agregar un pedido nuevo a la BD.
     * @param datos String array [5] con los valores del pedido en el siguiente orden: id_Cliente,
     *              id_vendedor , monto, estatus, observaciones
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long agregarPedido(ArrayList<String> datos)
    {
        ContentValues valores = new ContentValues();
        valores.put(CN_RAZON_SOCIAL_CLIENTE_PEDIDO, datos.get(0));
        valores.put(CN_RIF_CLIENTE_PEDIDO, datos.get(1));
        valores.put(CN_ESTADO_CLIENTE_PEDIDO, datos.get(2));
        valores.put(CN_TELEFONO_CLIENTE_PEDIDO, datos.get(3));
        valores.put(CN_EMAIL_CLIENTE_PEDIDO, datos.get(4));
        valores.put(CN_DIRECCION_CLIENTE_PEDIDO, datos.get(5));

        valores.put(CN_CEDULA_VENDEDOR_PEDIDO, datos.get(6));
        valores.put(CN_NOMBRE_VENDEDOR_PEDIDO, datos.get(7));
        valores.put(CN_APELLIDO_VENDEDOR_PEDIDO, datos.get(8));
        valores.put(CN_EMAIL_VENDEDOR_PEDIDO, datos.get(9));
        valores.put(CN_TELEFONO_VENDEDOR_PEDIDO, datos.get(10));
        valores.put(CN_ESTADO_VENDEDOR_PEDIDO, datos.get(11));
        valores.put(CN_MONTO_PEDIDO, datos.get(12) );
        valores.put(CN_FECHA_PEDIDO, getDateTime() );
        valores.put(CN_ESTATUS_PEDIDO, datos.get(13));
        valores.put(CN_OBSERVACIONES_PEDIDO, datos.get(14));
        long id =  db.insert(TABLA_PEDIDOS, null, valores);

        if (id!= -1)
        {
            return agregarPedidoDetalles(String.valueOf(id));
        }
        else
            return -1;
    }

    @SuppressWarnings("unused")
    public int borrarPedidos()
    {
        if(db.delete(TABLA_PEDIDOS, "1", null)>0 && db.delete(TABLA_PEDIDOS_DETALLES, "1", null)>0)
        {
            Log.w(TAG, "Pedidos borrados exitosamente");
            return 1;
        }
        else return -1;
    }

    public Cursor cargarIDClientePedido(String id_pedido)
    {
        String[] columnas = new String[]{ CN_RAZON_SOCIAL_CLIENTE_PEDIDO, CN_NOMBRE_VENDEDOR_PEDIDO, CN_MONTO_PEDIDO, CN_FECHA_PEDIDO, CN_ESTATUS_PEDIDO, CN_OBSERVACIONES_PEDIDO, CN_RIF_CLIENTE_PEDIDO, CN_ESTADO_CLIENTE_PEDIDO, CN_TELEFONO_CLIENTE_PEDIDO , CN_EMAIL_CLIENTE_PEDIDO , CN_DIRECCION_CLIENTE_PEDIDO };
        return db.query(TABLA_PEDIDOS, columnas, CN_ID_PEDIDO + "=?", new String[]{id_pedido}, null, null, null);
    }

    public int cancelarPedido(String id_pedido)
    {
        ContentValues valores_nuevos = new ContentValues();
        valores_nuevos.put(CN_ESTATUS_PEDIDO, "0");
        return db.update(TABLA_PEDIDOS, valores_nuevos, CN_ID_PEDIDO + "=?", new String[]{id_pedido});
    }

    public int aprobarPedido(String id_pedido)
    {
        ContentValues valores_nuevos = new ContentValues();
        valores_nuevos.put(CN_ESTATUS_PEDIDO, "2");
        return db.update(TABLA_PEDIDOS, valores_nuevos, CN_ID_PEDIDO + "=?", new String[]{id_pedido});
    }

    private long insertar_pedido(ArrayList<String> datos)
    {
        ContentValues valores = new ContentValues();
        valores.put(CN_RAZON_SOCIAL_CLIENTE_PEDIDO, datos.get(0));
        valores.put(CN_RIF_CLIENTE_PEDIDO, datos.get(1));
        valores.put(CN_ESTADO_CLIENTE_PEDIDO, datos.get(2));
        valores.put(CN_TELEFONO_CLIENTE_PEDIDO, datos.get(3));
        valores.put(CN_EMAIL_CLIENTE_PEDIDO, datos.get(4));
        valores.put(CN_DIRECCION_CLIENTE_PEDIDO, datos.get(5));
        valores.put(CN_CEDULA_VENDEDOR_PEDIDO, datos.get(6));
        valores.put(CN_NOMBRE_VENDEDOR_PEDIDO, datos.get(7));
        valores.put(CN_APELLIDO_VENDEDOR_PEDIDO, datos.get(8));
        valores.put(CN_EMAIL_VENDEDOR_PEDIDO, datos.get(9));
        valores.put(CN_TELEFONO_VENDEDOR_PEDIDO, datos.get(10));
        valores.put(CN_ESTADO_VENDEDOR_PEDIDO, datos.get(11));
        valores.put(CN_MONTO_PEDIDO, datos.get(12));
        valores.put(CN_FECHA_PEDIDO, getDateTime());
        valores.put(CN_ESTATUS_PEDIDO, datos.get(14));
        valores.put(CN_OBSERVACIONES_PEDIDO, datos.get(15));

        long id =  db.insert(TABLA_PEDIDOS, null, valores);

        if (id!= -1)
        {
            Log.d(TAG, "Pedido Insertado (COPIA)... id generado: "+id);
            return id;
        }
        else
        {
            Log.d(TAG, "Pedido NO Insertado (ERROR)");
            return -1;
        }
    }

    public long eliminarDataPedido(String id)
    {
        long col = db.delete(TABLA_PEDIDOS, CN_ID_PEDIDO + "=?", new String[]{id});
        long col2 = db.delete(TABLA_PEDIDOS_DETALLES, CN_ID_PEDIDO_DETALLES + "=?", new String[]{id});

        if(col>0 && col2>0)
            return col + col2;
        else
            return -1;
    }

    /* ------------------------------- FUNCIONES PEDIDOS DETALLES ------------------------------- */

    public Cursor cargarPedidosDetalles(String id_pedido)
    {
        String[] columnas = new String[]{ CN_ID_PEDIDO_DETALLES, CN_PRODUCTO_TIPO_PEDIDO_DETALLES, CN_PRODUCTO_MODELO_PEDIDO_DETALLES, CN_PRODUCTO_COLOR_PEDIDO_DETALLES, CN_TALLA_PEDIDOS_DETALLES, CN_NUMERACION_PEDIDOS_DETALLES, CN_PARES_PEDIDOS_DETALLES, CN_BULTOS_PEDIDOS_DETALLES, CN_PRECIO_UNITARIO_PEDIDOS_DETALLES, CN_SUBTOTAL_PEDIDOS_DETALLES, CN_ID_PEDIDOS_DETALLES };
        return db.query(TABLA_PEDIDOS_DETALLES, columnas, CN_ID_PEDIDOS_DETALLES + "=?", new String[]{ id_pedido }, null, null, null);
    }

    private long agregarPedidoDetalles(final String id_pedido)
    {
        String sql = "INSERT INTO "+TABLA_PEDIDOS_DETALLES  +" ( "+CN_ID_PEDIDOS_DETALLES+",  "+CN_PRODUCTO_TIPO_PEDIDO_DETALLES+", "+CN_PRODUCTO_MODELO_PEDIDO_DETALLES+", "+CN_PRODUCTO_COLOR_PEDIDO_DETALLES+", "+CN_TALLA_PEDIDOS_DETALLES+", "+CN_NUMERACION_PEDIDOS_DETALLES+", "+CN_PARES_PEDIDOS_DETALLES+", "+CN_BULTOS_PEDIDOS_DETALLES+", "+CN_PRECIO_UNITARIO_PEDIDOS_DETALLES+", "+CN_SUBTOTAL_PEDIDOS_DETALLES+") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        db.beginTransaction();
        Log.d("INSERTANDO", "PEDIDOS_LOCALES_DETALLES: BEGIN");

        Cursor c = cargarPedidoTemporal();
        ArrayList<String> tallas = new ArrayList<>();
        ArrayList<String> numeracion = new ArrayList<>();
        ArrayList<String> pares = new ArrayList<>();
        ArrayList<String> bultos = new ArrayList<>();
        ArrayList<String> precio = new ArrayList<>();
        ArrayList<String> subtotal = new ArrayList<>();
        ArrayList<String> tipo = new ArrayList<>();
        ArrayList<String> modelo = new ArrayList<>();
        ArrayList<String> color = new ArrayList<>();

        for ( c.moveToFirst(); !c.isAfterLast(); c.moveToNext() )
        {
            tallas.add(c.getString(2));
            numeracion.add(c.getString(3));
            pares.add(c.getString(4));
            bultos.add(c.getString(5));
            precio.add(c.getString(6));
            subtotal.add(c.getString(7));

            tipo.add(c.getString(8));
            modelo.add(c.getString(9));
            color.add(c.getString(10));
        }
        c.close();

        /* Iniciando procesado por lotes ( INSERTS ) */

        SQLiteStatement stmt = db.compileStatement(sql);
        long cont = 0;

        for (int i = 0; i < tallas.size(); i++)
        {
            stmt.bindString(1, id_pedido);
            stmt.bindString(2, tipo.get(i));
            stmt.bindString(3, modelo.get(i));
            stmt.bindString(4, color.get(i));
            stmt.bindString(5, tallas.get(i));
            stmt.bindString(6, numeracion.get(i));
            stmt.bindString(7, pares.get(i));
            stmt.bindString(8, bultos.get(i));
            stmt.bindString(9, precio.get(i));
            stmt.bindString(10, subtotal.get(i));
            stmt.executeInsert();
            stmt.clearBindings();
            cont++;
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        Log.d("INSERTANDO", "(" + cont + " productos agregados) PEDIDOS_LOCALES_DETALLES: END");
        return cont;
    }

    /* ------------------------------- FUNCIONES PEDIDOS TEMPORAL ------------------------------- */

    public Cursor cargarPedidoTemporal()
    {
        String[] columnas = new String[]{ CN_ID_PEDIDO_TEMPORALES, CN_ID_PRODUCTO_PEDIDO_TEMPORALES, CN_TALLA_PEDIDOS_TEMPORALES, CN_NUMERACION_PEDIDOS_TEMPORALES, CN_PARES_PEDIDOS_TEMPORALES, CN_BULTOS_PEDIDOS_TEMPORALES, CN_PRECIO_UNITARIO_PEDIDOS_TEMPORALES, CN_SUBTOTAL_PEDIDOS_TEMPORALES, CN_PRODUCTO_TIPO_PEDIDO_TEMPORALES, CN_PRODUCTO_MODELO_PEDIDO_TEMPORALES, CN_PRODUCTO_COLOR_PEDIDO_TEMPORALES  };
        return db.query(TABLA_PEDIDOS_TEMPORALES, columnas, null, null, null, null, null);
    }

    public Cursor cargarPedidoTemporal_Ordenado( String columna_ordenada, String orden )
    {
        String[] columnas = new String[]{ CN_ID_PEDIDO_TEMPORALES, CN_ID_PRODUCTO_PEDIDO_TEMPORALES, CN_TALLA_PEDIDOS_TEMPORALES, CN_NUMERACION_PEDIDOS_TEMPORALES, CN_PARES_PEDIDOS_TEMPORALES, CN_BULTOS_PEDIDOS_TEMPORALES, CN_PRECIO_UNITARIO_PEDIDOS_TEMPORALES, CN_SUBTOTAL_PEDIDOS_TEMPORALES, CN_PRODUCTO_TIPO_PEDIDO_TEMPORALES, CN_PRODUCTO_MODELO_PEDIDO_TEMPORALES, CN_PRODUCTO_COLOR_PEDIDO_TEMPORALES  };
        String orderby;

        if (columna_ordenada.equals("precio_unitario") || columna_ordenada.equals("subtotal"))
            orderby = "CAST("+columna_ordenada.toLowerCase()+" AS DECIMAL(15,2)) " + orden;
        else
            orderby = columna_ordenada.toLowerCase() + " " + orden;

        return db.query(TABLA_PEDIDOS_TEMPORALES, columnas, null, null, null, null, orderby);
    }

    public void agregarPedidoTemporal(ArrayList<ArrayList<String>> datos)
    {
        String sql = "INSERT INTO "+TABLA_PEDIDOS_TEMPORALES +" ("+CN_ID_PRODUCTO_PEDIDO_TEMPORALES +", "+CN_TALLA_PEDIDOS_TEMPORALES +", "+CN_NUMERACION_PEDIDOS_TEMPORALES +", "+CN_PARES_PEDIDOS_TEMPORALES +", "+CN_BULTOS_PEDIDOS_TEMPORALES  +", "+CN_PRECIO_UNITARIO_PEDIDOS_TEMPORALES  +", "+CN_SUBTOTAL_PEDIDOS_TEMPORALES  +", "+CN_PRODUCTO_TIPO_PEDIDO_TEMPORALES+", "+CN_PRODUCTO_MODELO_PEDIDO_TEMPORALES+", "+CN_PRODUCTO_COLOR_PEDIDO_TEMPORALES+" ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        db.beginTransaction();
        Log.d("INSERTANDO PEDIDOS TEMP", "BEGIN");
        SQLiteStatement stmt = db.compileStatement(sql);

        for (int i = 0; i < datos.size(); i++)
        {
            stmt.bindString(1, String.valueOf(datos.get(i).get(0)));
            stmt.bindString(2, String.valueOf(datos.get(i).get(1)));
            stmt.bindString(3, String.valueOf(datos.get(i).get(2)));
            stmt.bindString(4, String.valueOf(datos.get(i).get(3)));
            stmt.bindString(5, String.valueOf(datos.get(i).get(4)));
            stmt.bindString(6, String.valueOf(datos.get(i).get(5)));
            stmt.bindString(7, String.valueOf(datos.get(i).get(6)));

            stmt.bindString(8, String.valueOf(datos.get(i).get(7)));
            stmt.bindString(9, String.valueOf(datos.get(i).get(8)));
            stmt.bindString(10, String.valueOf(datos.get(i).get(9)));
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful(); // Si no le pongo esto hara un rollback..
        db.endTransaction();
        Log.d("INSERTANDO PEDIDOS TEMP", "FIN");
    }

    public int borrarPedidoTemporal()
    {
        return db.delete(TABLA_PEDIDOS_TEMPORALES, "1", null);
    }

    public int eliminarProductoPedidoTemporal(String id)
    {
        String[] args = { id };
        return db.delete(TABLA_PEDIDOS_TEMPORALES, CN_ID_PRODUCTO_PEDIDO_TEMPORALES + "=?", args);
    }

    public long actualizarBultosBDTemporal(String id, int bultos, String nuevo_subtotal)
    {
        ContentValues valores_nuevos = new ContentValues();
        valores_nuevos.put(CN_BULTOS_PEDIDOS_TEMPORALES, bultos);
        valores_nuevos.put(CN_SUBTOTAL_PEDIDOS_TEMPORALES, nuevo_subtotal);
        return db.update(TABLA_PEDIDOS_TEMPORALES, valores_nuevos, CN_ID_PRODUCTO_PEDIDO_TEMPORALES + "=?", new String[]{id});
    }

    public ArrayList<String> calcularTotalesNuevosPedidoTemp()
    {
        Cursor c = cargarPedidoTemporal();
        int bultos = 0;
        int pares = 0;
        Double precioProductoViejo = 0.0;

        for ( c.moveToFirst(); !c.isAfterLast(); c.moveToNext() )
        {
            precioProductoViejo += ( Integer.parseInt(c.getString(4)) * Integer.parseInt(c.getString(5)) * Double.parseDouble(c.getString(6)) );
            pares += ( Integer.parseInt(c.getString(4)) * Integer.parseInt(c.getString(5)) );
            bultos += Integer.parseInt(c.getString(5));
        }
        c.close();

        ArrayList<String> datos = new ArrayList<>();
        datos.add(String.valueOf(precioProductoViejo));
        datos.add(String.valueOf(bultos));
        datos.add(String.valueOf(pares));
        return datos;
    }

    /* ----------------------- FUNCIONES PEDIDOS EDITAR ---------------------------- */
    public long agregarPedidoEditado(ArrayList<String> datos, String id_pedido)
    {
        ContentValues valores = new ContentValues();
        valores.put(CN_RAZON_SOCIAL_CLIENTE_PEDIDO_EDITAR, datos.get(0));
        valores.put(CN_RIF_CLIENTE_PEDIDO_EDITAR, datos.get(1));
        valores.put(CN_ESTADO_CLIENTE_PEDIDO_EDITAR, datos.get(2));
        valores.put(CN_TELEFONO_CLIENTE_PEDIDO_EDITAR, datos.get(3));
        valores.put(CN_EMAIL_CLIENTE_PEDIDO_EDITAR, datos.get(4));
        valores.put(CN_DIRECCION_CLIENTE_PEDIDO_EDITAR, datos.get(5));

        valores.put(CN_CEDULA_VENDEDOR_PEDIDO_EDITAR, datos.get(6));
        valores.put(CN_NOMBRE_VENDEDOR_PEDIDO_EDITAR, datos.get(7));
        valores.put(CN_APELLIDO_VENDEDOR_PEDIDO_EDITAR, datos.get(8));
        valores.put(CN_EMAIL_VENDEDOR_PEDIDO_EDITAR, datos.get(9));
        valores.put(CN_TELEFONO_VENDEDOR_PEDIDO_EDITAR, datos.get(10));
        valores.put(CN_ESTADO_VENDEDOR_PEDIDO_EDITAR, datos.get(11));
        valores.put(CN_MONTO_PEDIDO_EDITAR, datos.get(12));
        valores.put(CN_FECHA_PEDIDO_EDITAR, getDateTime());
        valores.put(CN_ESTATUS_PEDIDO_EDITAR, datos.get(13));
        valores.put(CN_OBSERVACIONES_PEDIDO_EDITAR, datos.get(14));
        long id =  db.insert(TABLA_PEDIDOS_EDITAR, null, valores);

        if (id!= -1)
        {
            Log.d(TAG, "id generado: "+id);
            return agregarPedidoDetallesEditado(String.valueOf(id_pedido));
        }
        else
            return -1;
    }

    private long agregarPedidoDetallesEditado(final String id_pedido)
    {
        String sql = "INSERT INTO "+TABLA_PEDIDOS_DETALLES_EDITAR  +" ( "+CN_ID_PEDIDOS_DETALLES_EDITAR+",  "+CN_PRODUCTO_TIPO_PEDIDO_DETALLES_EDITAR+", "+CN_PRODUCTO_MODELO_PEDIDO_DETALLES_EDITAR+", "+CN_PRODUCTO_COLOR_PEDIDO_DETALLES_EDITAR+", "+CN_TALLA_PEDIDOS_DETALLES_EDITAR+", "+CN_NUMERACION_PEDIDOS_DETALLES_EDITAR+", "+CN_PARES_PEDIDOS_DETALLES_EDITAR+", "+CN_BULTOS_PEDIDOS_DETALLES_EDITAR+", "+CN_PRECIO_UNITARIO_PEDIDOS_DETALLES_EDITAR+", "+CN_SUBTOTAL_PEDIDOS_DETALLES_EDITAR+") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        db.beginTransaction();
        Log.d("INSERTANDO", "PEDIDOS_LOCALES_DETALLES: BEGIN - id_pedido:" + id_pedido);

        Cursor c = cargarPedidosDetalles(id_pedido);
        ArrayList<String> tallas = new ArrayList<>();
        ArrayList<String> numeracion = new ArrayList<>();
        ArrayList<String> pares = new ArrayList<>();
        ArrayList<String> bultos = new ArrayList<>();
        ArrayList<String> precio = new ArrayList<>();
        ArrayList<String> subtotal = new ArrayList<>();
        ArrayList<String> tipo = new ArrayList<>();
        ArrayList<String> modelo = new ArrayList<>();
        ArrayList<String> color = new ArrayList<>();

        for ( c.moveToFirst(); !c.isAfterLast(); c.moveToNext() )
        {
            // 0  CN_ID_PEDIDO_DETALLES,
            // 1  CN_PRODUCTO_TIPO_PEDIDO_DETALLES,
            // 2  CN_PRODUCTO_MODELO_PEDIDO_DETALLES,
            // 3  CN_PRODUCTO_COLOR_PEDIDO_DETALLES,
            // 4  CN_TALLA_PEDIDOS_DETALLES,
            // 5  CN_NUMERACION_PEDIDOS_DETALLES,
            // 6  CN_PARES_PEDIDOS_DETALLES,
            // 7  CN_BULTOS_PEDIDOS_DETALLES,
            // 8  CN_PRECIO_UNITARIO_PEDIDOS_DETALLES,
            // 9  CN_SUBTOTAL_PEDIDOS_DETALLES,
            // 10 CN_ID_PEDIDOS_DETALLES

            tipo.add(c.getString(1));
            modelo.add(c.getString(2));
            color.add(c.getString(3));
            tallas.add(c.getString(4));
            numeracion.add(c.getString(5));
            pares.add(c.getString(6));
            bultos.add(c.getString(7));
            precio.add(c.getString(8));
            subtotal.add(c.getString(9));
        }
        c.close();

        /* Iniciando procesado por lotes ( INSERTS ) */

        SQLiteStatement stmt = db.compileStatement(sql);
        long cont = 0;

        for (int i = 0; i < tallas.size(); i++)
        {
            stmt.bindString(1, id_pedido);
            stmt.bindString(2, tipo.get(i));
            stmt.bindString(3, modelo.get(i));
            stmt.bindString(4, color.get(i));
            stmt.bindString(5, tallas.get(i));
            stmt.bindString(6, numeracion.get(i));
            stmt.bindString(7, pares.get(i));
            stmt.bindString(8, bultos.get(i));
            stmt.bindString(9, precio.get(i));
            stmt.bindString(10, subtotal.get(i));
            stmt.executeInsert();
            stmt.clearBindings();
            cont++;
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        Log.d("INSERTANDO", "(" + cont + " productos agregados) PEDIDOS_LOCALES_DETALLES: END");
        return cont;
    }

    public int borrarPedidoEditar()
    {
        if ( db.delete(TABLA_PEDIDOS_EDITAR, "1", null) >0 && db.delete(TABLA_PEDIDOS_DETALLES_EDITAR, "1", null) >0 )
        {
            Log.w(TAG, "Borrado exitoso de pedidos editar..");
            return 2;
        }
        else
            return 0;
    }

    public Cursor cargarPedidosDetallesEditar()
    {
        String[] columnas = new String[]{ CN_ID_PEDIDO_DETALLES_EDITAR, CN_PRODUCTO_TIPO_PEDIDO_DETALLES_EDITAR, CN_PRODUCTO_MODELO_PEDIDO_DETALLES_EDITAR, CN_PRODUCTO_COLOR_PEDIDO_DETALLES_EDITAR, CN_TALLA_PEDIDOS_DETALLES_EDITAR, CN_NUMERACION_PEDIDOS_DETALLES_EDITAR, CN_PARES_PEDIDOS_DETALLES_EDITAR, CN_BULTOS_PEDIDOS_DETALLES_EDITAR, CN_PRECIO_UNITARIO_PEDIDOS_DETALLES_EDITAR, CN_SUBTOTAL_PEDIDOS_DETALLES_EDITAR, CN_ID_PEDIDOS_DETALLES_EDITAR };
        return db.query(TABLA_PEDIDOS_DETALLES_EDITAR, columnas, null, null, null, null, null);
    }

    public long actualizarBultosBDPedidosEditar(String modelo, int bultos, String nuevo_subtotal)
    {
        ContentValues valores_nuevos = new ContentValues();
        valores_nuevos.put(CN_BULTOS_PEDIDOS_DETALLES_EDITAR, bultos);
        valores_nuevos.put(CN_SUBTOTAL_PEDIDOS_DETALLES_EDITAR, nuevo_subtotal);
        int res1 = db.update(TABLA_PEDIDOS_DETALLES_EDITAR, valores_nuevos, CN_PRODUCTO_MODELO_PEDIDO_DETALLES_EDITAR + "=?", new String[]{modelo});

        if(res1>0)
        {
            // Actualizar el nuevo subtotal de la tabla pedidos editar
            Double total_pedido = 0.00;

            Cursor cursor = cargarPedidosDetallesEditar();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                total_pedido += Double.parseDouble(cursor.getString(9));
            }

            ContentValues valores_nuevos2 = new ContentValues();
            valores_nuevos2.put(CN_MONTO_PEDIDO_EDITAR, total_pedido);
            int res2 = db.update(TABLA_PEDIDOS_EDITAR, valores_nuevos2, null, null);

            if(res2>0) return res1 + res2;
            else return -1;
        }
        else return -1;
    }

    public ArrayList<String> calcularTotalesNuevosPedidoEditar()
    {
        Cursor c = cargarPedidoDetalleEditar();
        int bultos = 0;
        int pares = 0;
        Double precioProductoViejo = 0.0;

        for ( c.moveToFirst(); !c.isAfterLast(); c.moveToNext() )
        {
            precioProductoViejo += ( Integer.parseInt(c.getString(0)) * Integer.parseInt(c.getString(1)) * Double.parseDouble(c.getString(2)) );
            pares += ( Integer.parseInt(c.getString(0)) * Integer.parseInt(c.getString(1)) );
            bultos += Integer.parseInt(c.getString(1));
        }
        c.close();

        ArrayList<String> datos = new ArrayList<>();
        datos.add( String.valueOf(precioProductoViejo) );
        datos.add(String.valueOf(bultos));
        datos.add(String.valueOf(pares));
        return datos;
    }

    public Cursor cargarPedidoDetalleEditar()
    {
        String[] columnas = new String[]{ CN_PARES_PEDIDOS_DETALLES_EDITAR, CN_BULTOS_PEDIDOS_DETALLES_EDITAR, CN_PRECIO_UNITARIO_PEDIDOS_DETALLES_EDITAR };
        return db.query(TABLA_PEDIDOS_DETALLES_EDITAR, columnas, null, null, null, null, null);
    }

    public Cursor cargarPedidosEditar()
    {
        String[] columnas = new String[]{ CN_RAZON_SOCIAL_CLIENTE_PEDIDO_EDITAR, CN_RIF_CLIENTE_PEDIDO_EDITAR, CN_ESTADO_CLIENTE_PEDIDO_EDITAR, CN_TELEFONO_CLIENTE_PEDIDO_EDITAR, CN_EMAIL_CLIENTE_PEDIDO_EDITAR, CN_DIRECCION_CLIENTE_PEDIDO_EDITAR, CN_CEDULA_VENDEDOR_PEDIDO_EDITAR, CN_NOMBRE_VENDEDOR_PEDIDO_EDITAR, CN_APELLIDO_VENDEDOR_PEDIDO_EDITAR, CN_EMAIL_VENDEDOR_PEDIDO_EDITAR, CN_TELEFONO_VENDEDOR_PEDIDO_EDITAR, CN_ESTADO_VENDEDOR_PEDIDO_EDITAR, CN_MONTO_PEDIDO_EDITAR, CN_FECHA_PEDIDO_EDITAR, CN_ESTATUS_PEDIDO_EDITAR, CN_OBSERVACIONES_PEDIDO_EDITAR};
        return db.query(TABLA_PEDIDOS_EDITAR, columnas, null, null, null, null, null);
    }

    public long editarPedido(String id_pedido)
    {
        // Agregar informacion nueva, de la Tabla Pedidos Editar a la Tabla Pedidos
        Log.d(TAG, "Extrayendo datos de la tabla pedidos_editar..");

        Cursor cursor = cargarPedidosEditar();
        ArrayList<String> datos_pedidos = new ArrayList<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            Log.d(TAG, "Razon Social: "+cursor.getString(0));
            Log.d(TAG, "Rif: "+cursor.getString(1));
            Log.d(TAG, "Estado: "+cursor.getString(2));
            Log.d(TAG, "Telefono: "+cursor.getString(3));
            Log.d(TAG, "Email: "+cursor.getString(4));
            Log.d(TAG, "Direccion: "+cursor.getString(5));
            Log.d(TAG, "Cedula Vendedor: "+cursor.getString(6));
            Log.d(TAG, "Nombre Vendedor: "+cursor.getString(7));
            Log.d(TAG, "Apellido Vendedor: "+cursor.getString(8));
            Log.d(TAG, "Email Vendedor: "+cursor.getString(9));
            Log.d(TAG, "Telefono Vendedor: "+cursor.getString(10));
            Log.d(TAG, "Estado Vendedor: "+cursor.getString(11));
            Log.d(TAG, "Monto: "+cursor.getString(12));
            Log.d(TAG, "Fecha: "+cursor.getString(13));
            Log.d(TAG, "Estatus: "+cursor.getString(14));
            Log.d(TAG, "Observaciones: "+cursor.getString(15));
            datos_pedidos.add( cursor.getString(0) );
            datos_pedidos.add( cursor.getString(1) );
            datos_pedidos.add( cursor.getString(2) );
            datos_pedidos.add( cursor.getString(3) );
            datos_pedidos.add( cursor.getString(4) );
            datos_pedidos.add( cursor.getString(5) );
            datos_pedidos.add( cursor.getString(6) );
            datos_pedidos.add( cursor.getString(7) );
            datos_pedidos.add( cursor.getString(8) );
            datos_pedidos.add( cursor.getString(9) );
            datos_pedidos.add( cursor.getString(10) );
            datos_pedidos.add( cursor.getString(11) );
            datos_pedidos.add( cursor.getString(12) );
            datos_pedidos.add( cursor.getString(13) );
            datos_pedidos.add( cursor.getString(14) );
            datos_pedidos.add( cursor.getString(15) );
        }
        cursor.close();

        long res1 = insertar_pedido(datos_pedidos);
        if (res1==-1) return -1;
        else Log.d(TAG, "OK");

        // Agregar informacion nueva, de la Tabla Pedidos Detalles Editar a la Tabla Pedidos Detalles
        Log.d(TAG, "Extrayendo datos de la tabla pedidos_editar_detalles..");
        Cursor cursor2 = cargarPedidosDetallesEditar();
        ArrayList<ArrayList<String>> datos_pedidos_detalles = new ArrayList<>();

        for (cursor2.moveToFirst(); !cursor2.isAfterLast(); cursor2.moveToNext())
        {
            Log.d(TAG, "Tipo: "+cursor2.getString(1));
            Log.d(TAG, "Modelo: "+cursor2.getString(2));
            Log.d(TAG, "Color: "+cursor2.getString(3));
            Log.d(TAG, "Talla: "+cursor2.getString(4));
            Log.d(TAG, "Numeracion: "+cursor2.getString(5));
            Log.d(TAG, "Pares: "+cursor2.getString(6));
            Log.d(TAG, "Bultos: "+cursor2.getString(7));
            Log.d(TAG, "Precio Unitario: "+cursor2.getString(8));
            Log.d(TAG, "Subtotal: " + cursor2.getString(9));

            ArrayList<String> producto = new ArrayList<>();
            producto.add( cursor2.getString(1) );
            producto.add( cursor2.getString(2) );
            producto.add( cursor2.getString(3) );
            producto.add( cursor2.getString(4) );
            producto.add( cursor2.getString(5) );
            producto.add( cursor2.getString(6) );
            producto.add( cursor2.getString(7) );
            producto.add( cursor2.getString(8) );
            producto.add( cursor2.getString(9) );

            datos_pedidos_detalles.add( producto );
        }
        cursor2.close();

        long res2 = insertar_pedido_detalles(datos_pedidos_detalles, String.valueOf(res1));
        if (res2==-1) { eliminarDataPedido(String.valueOf(res1)); return -1; }
        else Log.d(TAG, "OK");

        // Borrar la informacion vieja de la tabla pedidos y pedidos detalles
        long res3 = eliminarDataPedido(id_pedido);
        if (res3==-1) return -1;
        else { Log.d(TAG, "OK FINAL.. ID_Pedido_Nuevo: "+res1); return res1; }
    }

    private long insertar_pedido_detalles(ArrayList<ArrayList<String>> datos_pedidos_detalles, String id_pedido)
    {
        String sql = "INSERT INTO "+TABLA_PEDIDOS_DETALLES  +" ( "+CN_ID_PEDIDOS_DETALLES+",  "+CN_PRODUCTO_TIPO_PEDIDO_DETALLES+", "+CN_PRODUCTO_MODELO_PEDIDO_DETALLES+", "+CN_PRODUCTO_COLOR_PEDIDO_DETALLES+", "+CN_TALLA_PEDIDOS_DETALLES+", "+CN_NUMERACION_PEDIDOS_DETALLES+", "+CN_PARES_PEDIDOS_DETALLES+", "+CN_BULTOS_PEDIDOS_DETALLES+", "+CN_PRECIO_UNITARIO_PEDIDOS_DETALLES+", "+CN_SUBTOTAL_PEDIDOS_DETALLES+") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        db.beginTransaction();
        Log.d("INSERTANDO", "PEDIDOS_LOCALES_DETALLES: BEGIN");

        /* Iniciando procesado por lotes ( INSERTS ) */

        SQLiteStatement stmt = db.compileStatement(sql);
        long cont = 0;

        for (int i = 0; i < datos_pedidos_detalles.size(); i++)
        {
            stmt.bindString(1, id_pedido);
            stmt.bindString(2, datos_pedidos_detalles.get(i).get(0));
            stmt.bindString(3, datos_pedidos_detalles.get(i).get(1));
            stmt.bindString(4, datos_pedidos_detalles.get(i).get(2));
            stmt.bindString(5, datos_pedidos_detalles.get(i).get(3));
            stmt.bindString(6, datos_pedidos_detalles.get(i).get(4));
            stmt.bindString(7, datos_pedidos_detalles.get(i).get(5));
            stmt.bindString(8, datos_pedidos_detalles.get(i).get(6));
            stmt.bindString(9, datos_pedidos_detalles.get(i).get(7));
            stmt.bindString(10, datos_pedidos_detalles.get(i).get(8));
            stmt.executeInsert();
            stmt.clearBindings();
            cont++;
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        Log.d("INSERTANDO", "(" + cont + " productos agregados) PEDIDOS_LOCALES_DETALLES: END");
        return cont;
    }

    public int eliminarProductoPedidoEditar(String nombre_producto)
    {
        String[] args = { nombre_producto };
        int res = db.delete(TABLA_PEDIDOS_DETALLES_EDITAR, CN_PRODUCTO_MODELO_PEDIDO_DETALLES_EDITAR + "=?", args);

        if (res>0)
        {
            // Actualizar el nuevo subtotal de la tabla pedidos editar
            Double total_pedido = 0.00;

            Cursor cursor = cargarPedidosDetallesEditar();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                total_pedido += Double.parseDouble(cursor.getString(9));
            }
            cursor.close();

            ContentValues valores_nuevos2 = new ContentValues();
            valores_nuevos2.put(CN_MONTO_PEDIDO_EDITAR, total_pedido);
            int res2 = db.update(TABLA_PEDIDOS_EDITAR, valores_nuevos2, null, null);

            if(res2>0) return res;
            else return -1;
        }
        else
            return -1;
    }

    public int agregarProductoPedidoEditar(ArrayList<ArrayList<String>> datos, String id_pedido)
    {
        String sql = "INSERT INTO "+TABLA_PEDIDOS_DETALLES_EDITAR +" ("+CN_ID_PEDIDOS_DETALLES_EDITAR+", "+CN_PRODUCTO_TIPO_PEDIDO_DETALLES_EDITAR+", "+CN_PRODUCTO_MODELO_PEDIDO_DETALLES_EDITAR+", "+CN_PRODUCTO_COLOR_PEDIDO_DETALLES_EDITAR+", "+CN_TALLA_PEDIDOS_DETALLES_EDITAR+", "+CN_NUMERACION_PEDIDOS_DETALLES_EDITAR+", "+CN_PARES_PEDIDOS_DETALLES_EDITAR+", "+CN_BULTOS_PEDIDOS_DETALLES_EDITAR+", "+CN_PRECIO_UNITARIO_PEDIDOS_DETALLES_EDITAR+", "+CN_SUBTOTAL_PEDIDOS_DETALLES_EDITAR+") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        db.beginTransaction();
        Log.d("INSERTANDO PEDIDOS EDIT", "BEGIN");
        SQLiteStatement stmt = db.compileStatement(sql);

        for (int i = 0; i < datos.size(); i++)
        {
            stmt.bindString(1, id_pedido);
            stmt.bindString(2, String.valueOf(datos.get(i).get(0)));
            stmt.bindString(3, String.valueOf(datos.get(i).get(1)));
            stmt.bindString(4, String.valueOf(datos.get(i).get(2)));
            stmt.bindString(5, String.valueOf(datos.get(i).get(3)));
            stmt.bindString(6, String.valueOf(datos.get(i).get(4)));
            stmt.bindString(7, String.valueOf(datos.get(i).get(5)));
            stmt.bindString(8, String.valueOf(datos.get(i).get(6)));
            stmt.bindString(9, String.valueOf(datos.get(i).get(7)));
            stmt.bindString(10, String.valueOf(datos.get(i).get(8)));
            //stmt.bindString(10, String.valueOf(datos.get(i).get(9)));
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful(); // Si no le pongo esto hara un rollback..
        db.endTransaction();

        Log.d("INSERTANDO PEDIDOS EDIT", "FIN");

        // Actualizar el total del pedido editar
        // Actualizar el nuevo subtotal de la tabla pedidos editar
        Double total_pedido = 0.00;

        Cursor cursor = cargarPedidosDetallesEditar();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            total_pedido += Double.parseDouble(cursor.getString(9));
        }
        cursor.close();

        ContentValues valores_nuevos2 = new ContentValues();
        valores_nuevos2.put(CN_MONTO_PEDIDO_EDITAR, total_pedido);
        int res2 = db.update(TABLA_PEDIDOS_EDITAR, valores_nuevos2, null, null);

        if(res2>0) return res2;
        else return -1;
    }
}