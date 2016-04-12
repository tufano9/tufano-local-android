package com.tufano.tufanomovil.gestion.pedidos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.Constantes;
import com.tufano.tufanomovil.global.Funciones;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class EditarPedido extends AppCompatActivity
{
    private String usuario, id_pedido;
    private Context contexto;
    private final String TAG = "EditarPedido";
    private ProgressDialog pDialog;
    private DBAdapter manager;
    public static Activity fa;
    private static final int IMG_WIDTH = 130;
    private static final int IMG_HEIGHT = 50;
    private static final ImageView.ScaleType ESCALADO = ImageView.ScaleType.CENTER_INSIDE;
    private boolean borre_producto;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_pedidos);

        fa = this;
        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        getExtrasVar();
        createToolBar();
        cargarDatosCliente();

        new cargarDatos().execute();
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        usuario = bundle.getString("usuario");
        id_pedido = bundle.getString("id_pedido");
        borre_producto = bundle.getBoolean("borre_producto");
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.realizar_pedido_subtitulo);
        setSupportActionBar(toolbar);
    }

    /**
     * Inicializando los botones
     */
    private void inicializarBotones()
    {
        Button editar = (Button) findViewById(R.id.btn_editar_pedido);

        editar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if( !PedidoEditarVacio() )
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(EditarPedido.this);

                    dialog.setTitle(R.string.confirmacion);
                    dialog.setMessage(R.string.confirmacion_editar_pedido);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new async_editarPedido().execute();
                        }
                    });

                    dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                }
                else
                {
                    Toast.makeText(contexto, "El pedido esta vacio, no puede editarlo asi!", Toast.LENGTH_LONG).show();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent c = new Intent(EditarPedido.this, AgregarProductoPedidoEditar.class);
                c.putExtra("usuario", usuario);
                c.putExtra("id_pedido", id_pedido);
                startActivity(c);
            }
        });
    }

    /**
     * Indica si el la tabla pedidoEditar esta vacia.
     * @return True si esta vacia, false en caso contrario.
     */
    private boolean PedidoEditarVacio()
    {
        Cursor c = manager.cargarPedidosDetallesEditar();
        Log.d(TAG, "Productos pedido editar: " + c.getCount());
        return c.getCount()<=0;
    }

    /**
     * Obtiene los datos del cliente y rellena los campos correspondientes con dichos datos.
     */
    private void cargarDatosCliente()
    {
        TextView razonSocial = (TextView) findViewById(R.id.rs_cliente_pedido);
        TextView rifCliente = (TextView) findViewById(R.id.rif_cliente_pedido);
        TextView estadoCliente = (TextView) findViewById(R.id.estado_cliente_pedido);

        List<String> datos_clientes = obtenerDatosCliente(id_pedido);

        razonSocial.setText(datos_clientes.get(0));
        rifCliente.setText(datos_clientes.get(1));
        estadoCliente.setText(datos_clientes.get(2));
    }

    /**
     * Obtiene los datos del cliente indicado
     * @param id ID del cliente a consultar
     * @return List con los datos del cliente.
     */
    private List<String> obtenerDatosCliente(String id)
    {
        List<String> datos_clientes = new ArrayList<>();
        Cursor cursor = manager.cargarIDClientePedido(id);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            datos_clientes.add(cursor.getString(0));
            datos_clientes.add(cursor.getString(6));
            datos_clientes.add(cursor.getString(7));
            datos_clientes.add(cursor.getString(8));
            datos_clientes.add(cursor.getString(9));
            datos_clientes.add(cursor.getString(10));
        }
        return datos_clientes;
    }

    /**
     * Obtiene los datos del vendedor (usuario)
     * @param usuario ID del usuario a consultar
     * @return Lista con los datos del vendedor.
     */
    private List<String> obtenerDatosVendedor(String usuario)
    {
        return manager.buscarUsuario_ID2(usuario);
    }

    class cargarDatos extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarPedido.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            if (!borre_producto)
            {
                manager.borrarPedidoEditar();
                copiarTabla();
            }
            inicializarTabla();
            inicializarBotones();
            return null;
        }

        private void copiarTabla()
        {
            // Se copiara la informacion del pedido actual, en la tabla nueva pedido_editar
            Cursor cursor = manager.cargarPedidosDetalles(id_pedido);
            Double monto = 0.00;

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                final String pares = cursor.getString(6);
                final String bultos = cursor.getString(7);
                final String precio = cursor.getString(8);

                monto += Double.parseDouble(precio) * Integer.parseInt(pares) * Integer.parseInt(bultos);
            }

            cursor.close();
            String observaciones = "Prueba de una observacion..";

            ArrayList<String> datos = new ArrayList<>();
            List<String> datos_cliente = obtenerDatosCliente(id_pedido);
            List<String> datos_vendedor = obtenerDatosVendedor(usuario);

            for (int i = 0; i <datos_cliente.size(); i++)
            {
                Log.d(TAG, "Agregando 1: "+datos_cliente.get(i));
                datos.add(datos_cliente.get(i));
            }

            for (int i = 0; i <datos_vendedor.size(); i++)
            {
                Log.d(TAG, "Agregando 2: "+datos_vendedor.get(i));
                datos.add(datos_vendedor.get(i));
            }

            datos.add(String.valueOf(monto));
            datos.add("1");
            datos.add(observaciones);
            long res = manager.agregarPedidoEditado(datos, id_pedido);
            Log.d(TAG, "res: "+res);
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
        }

        private void inicializarTabla()
        {
            final Thread hilo_base = new Thread()
            {
                @Override
                public void run()
                {
                    synchronized (this)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Log.i(TAG, "Inicializando tabla..");
                                final TableLayout tabla = (TableLayout) findViewById(R.id.table_editar_pedidos);
                                final TableLayout contenido = (TableLayout) findViewById(R.id.tabla_contenido);
                                Double precio_total = 0.0;

                                int total_pares = 0;
                                int total_bultos = 0;

                                final TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

                                // Llenando la tabla de forma iterativa
                                Cursor cursor = manager.cargarPedidosDetallesEditar();

                                // 0 CN_ID_PEDIDO_DETALLES
                                // 1 CN_PRODUCTO_TIPO_PEDIDO_DETALLES
                                // 2 CN_PRODUCTO_MODELO_PEDIDO_DETALLES
                                // 3 CN_PRODUCTO_COLOR_PEDIDO_DETALLES
                                // 4 CN_TALLA_PEDIDOS_DETALLES
                                // 5 CN_NUMERACION_PEDIDOS_DETALLES
                                // 6 CN_PARES_PEDIDOS_DETALLES
                                // 7 CN_BULTOS_PEDIDOS_DETALLES
                                // 8 CN_PRECIO_UNITARIO_PEDIDOS_DETALLES
                                // 9 CN_SUBTOTAL_PEDIDOS_DETALLES
                                // 10 CN_ID_PEDIDOS_DETALLES

                                if (cursor.getCount() > 0)
                                {
                                    mostrarTodo(contenido, tabla);

                                    final int cantidad_productos = cursor.getCount();

                                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
                                    {
                                        Log.i(TAG, "Agregando fila..");

                                        final TableRow fila = new TableRow(contexto);
                                        //final String id_producto = String.valueOf(cursor.getInt(1));
                                        final String nombre_modelo = String.valueOf(cursor.getString(2));
                                        //final String talla = String.valueOf(cursor.getString(4));
                                        final String numeracion = cursor.getString(5);
                                        final String pares = cursor.getString(6);
                                        final String bultos = cursor.getString(7);
                                        final String precio = cursor.getString(8);
                                        final String subtotal = cursor.getString(9);

                                        precio_total += Double.parseDouble(precio) * Integer.parseInt(pares) * Integer.parseInt(bultos);
                                        total_pares += Integer.parseInt(pares);
                                        total_bultos += Integer.parseInt(bultos);

                                        Log.i(TAG, "Producto: " + nombre_modelo + " Precio: " + Double.parseDouble(precio) + "*" + Integer.parseInt(pares) + "*" + Integer.parseInt(bultos) + " = " + Double.parseDouble(precio) * Integer.parseInt(pares) * Integer.parseInt(bultos));

                                        ImageView imagen  = generarImageViewTabla(nombre_modelo, params, contexto);
                                        TextView modelo = generarTextViewModeloTabla(nombre_modelo, params, contexto);
                                        TextView numeracion_producto = generarTextViewNumeracionTabla(numeracion, params, contexto);
                                        TextView pares_producto = generarTextViewParesTabla(pares, params, contexto);
                                        TextView precio_producto = generarTextViewPrecioTabla(precio, params, contexto);
                                        final TextView sub_total = generarTextViewSubTotalTabla(subtotal, params, contexto);
                                        final NumberPicker cantidad_bultos = generarNumberPickerBultosTabla(sub_total, bultos, precio, pares, nombre_modelo, cantidad_productos, params, contexto);

                                        TableRow.LayoutParams layout = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT);
                                        imagen.setLayoutParams(layout);
                                        modelo.setLayoutParams(layout);
                                        pares_producto.setLayoutParams(layout);
                                        precio_producto.setLayoutParams(layout);
                                        numeracion_producto.setLayoutParams(layout);
                                        cantidad_bultos.setLayoutParams(layout);
                                        sub_total.setLayoutParams(layout);

                                        // Llenando la fila con data
                                        fila.addView(imagen);
                                        fila.addView(modelo);
                                        fila.addView(numeracion_producto);
                                        fila.addView(pares_producto);
                                        fila.addView(precio_producto);
                                        fila.addView(cantidad_bultos);
                                        fila.addView(sub_total);
                                        fila.setPadding(0, 2 , 0, 0);

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                            fila.setBackground(Funciones.intToDrawable(contexto, R.drawable.table_border));
                                        else
                                            //noinspection deprecation
                                            fila.setBackgroundDrawable(Funciones.intToDrawable(contexto, R.drawable.table_border));

                                        final Thread hilo1 = new Thread() {
                                            @Override
                                            public void run() {
                                                synchronized (this) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run()
                                                        {
                                                            contenido.addView(fila);
                                                        }
                                                    });
                                                }
                                            }
                                        };
                                        hilo1.start();

                                        //tabla.addView(fila);
                                    }

                                    actualizarInformacionInferior(precio_total, cursor.getCount(), total_bultos, total_pares);
                                }
                                else
                                {
                                    final Thread hilo = new Thread()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            synchronized (this)
                                            {
                                                runOnUiThread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {

                                                        ocultarTodo(contenido, tabla);

                                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                                                        TextView mensaje = new TextView(contexto);
                                                        mensaje.setText(R.string.msj_pedido_vacio);
                                                        mensaje.setGravity(Gravity.CENTER);
                                                        mensaje.setTextSize(20f);
                                                        mensaje.setLayoutParams(params);

                                                        LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor);
                                                        contenedor.removeAllViews();
                                                        contenedor.addView(mensaje);
                                                    }
                                                });

                                            }
                                        }
                                    };
                                    hilo.start();
                                }

                                cursor.close();
                            }
                        });

                    }
                }
            };
            hilo_base.start();

        }

        private void mostrarTodo(TableLayout tabla, TableLayout tab)
        {
            LinearLayout botones_opciones = (LinearLayout) findViewById(R.id.botones_opciones);
            LinearLayout datos_pedido = (LinearLayout) findViewById(R.id.datos_pedido);

            datos_pedido.setVisibility(View.VISIBLE);
            botones_opciones.setVisibility(View.VISIBLE);
            tabla.setVisibility(View.VISIBLE);
            tab.setVisibility(View.VISIBLE);
        }

        private void ocultarTodo(TableLayout tabla, TableLayout tab)
        {
            LinearLayout botones_opciones = (LinearLayout) findViewById(R.id.botones_opciones);
            LinearLayout datos_pedido = (LinearLayout) findViewById(R.id.datos_pedido);

            datos_pedido.setVisibility(View.INVISIBLE);
            botones_opciones.setVisibility(View.INVISIBLE);
            tabla.setVisibility(View.INVISIBLE);
            tab.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Actualiza los datos del inferior de la aplicacion. (Resumen)
     * @param precio_total Precio total del pedido.
     * @param cantidad_productos Cantidad de productos del pedido.
     * @param cantidad_bultos Cantidad de bultos del pedido.
     * @param cantidad_pares Cantidad de pares del pedido.
     */
    private void actualizarInformacionInferior(final Double precio_total, final int cantidad_productos, final int cantidad_bultos, final int cantidad_pares)
    {
        final Thread hilo = new Thread()
        {
            @Override
            public void run()
            {
                synchronized (this)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TextView cantidadProductos = (TextView) findViewById(R.id.cantidadFinal);
                            TextView precioFinal = (TextView) findViewById(R.id.precioFinal);
                            TextView cantidadBultos = (TextView) findViewById(R.id.cantidad_bultos);
                            TextView cantidadPares = (TextView) findViewById(R.id.cantidad_pares);

                            DecimalFormat priceFormat = new DecimalFormat("###,###.##");
                            String output = priceFormat.format(precio_total);
                            cantidadProductos.setText(String.valueOf(cantidad_productos));
                            precioFinal.setText(output);
                            cantidadBultos.setText(String.valueOf(cantidad_bultos));
                            cantidadPares.setText(String.valueOf(cantidad_pares));
                        }
                    });

                }
            }
        };
        hilo.start();
    }

    /**
     * Elimina un producto del pedido.
     * @param nombre_producto Nombre del producto a eliminar.
     */
    private void eliminarProductoPedido(String nombre_producto)
    {
        int filas_afectadas = manager.eliminarProductoPedidoEditar(nombre_producto);

        if(filas_afectadas>0)
        {
            Toast.makeText(contexto, "¡Se ha eliminado correctamente el producto del pedido!", Toast.LENGTH_LONG).show();
            Intent c = new Intent(EditarPedido.this, EditarPedido.class);
            c.putExtra("usuario", usuario);
            c.putExtra("id_pedido", id_pedido);
            c.putExtra("borre_producto", true);
            startActivity(c);
            fa.finish();
            finish();
        }
        else
        {
            Toast.makeText(contexto, "¡Ha ocurrido un error eliminando el producto del pedido!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Genera el textview del subtotal
     * @param subtotal Subtotal
     * @param params Parametros de la tabla
     * @param contexto Contexto de la aplicacion
     * @return TextView armado.
     */
    private TextView generarTextViewSubTotalTabla(String subtotal, TableRow.LayoutParams params, Context contexto)
    {
        final TextView sub_total = new TextView(contexto);
        sub_total.setText(Funciones.formatoPrecio(subtotal));
        sub_total.setTextColor(Color.DKGRAY);
        sub_total.setGravity(Gravity.CENTER);
        sub_total.setLayoutParams(params);
        sub_total.setTextSize(16f);
        return sub_total;
    }

    /**
     * Genera el numberPicker de los bultos.
     * @param sub_total TextView de subtotal. (Para actualizarlo autom.)
     * @param bultos Numero de bultos.
     * @param precio Precio del producto.
     * @param pares Pares del producto.
     * @param nombre_modelo Nombre del modelo.
     * @param cantidad_productos Cantidad de productos.
     * @param params Parametros de la tabla.
     * @param contexto Contexto de la aplicacion.
     * @return NumberPicker armado.
     */
    private NumberPicker generarNumberPickerBultosTabla(final TextView sub_total, String bultos, final String precio, final String pares, final String nombre_modelo, final int cantidad_productos, TableRow.LayoutParams params, Context contexto)
    {
        final NumberPicker cantidad_bultos = new NumberPicker(contexto);
        cantidad_bultos.setMinValue(1);
        cantidad_bultos.setMaxValue(99);
        cantidad_bultos.setValue(Integer.parseInt(bultos));
        cantidad_bultos.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        cantidad_bultos.setLayoutParams(params);

        cantidad_bultos.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int cantBultosViejos, int cantBultosNuevos) {
                String nuevo_subtotal = String.valueOf(Double.parseDouble(precio) * Integer.parseInt(pares) * cantBultosNuevos);

                if (manager.actualizarBultosBDPedidosEditar(nombre_modelo, cantBultosNuevos, nuevo_subtotal) > 0) {
                    // Actualizar Cant.Bultos / Pares y Total Bs.F (Parte Inf)
                    ArrayList<String> datosNuevos = manager.calcularTotalesNuevosPedidoEditar();

                    // final Double precio_total, final int cantidad_productos, final int cantidad_bultos, final int cantidad_pares
                    actualizarInformacionInferior(Double.parseDouble(datosNuevos.get(0)), cantidad_productos, Integer.parseInt(datosNuevos.get(1)), Integer.parseInt(datosNuevos.get(2)));
                    sub_total.setText(Funciones.formatoPrecio(nuevo_subtotal));
                } else {
                    Log.i(TAG, "BD NO ACTUALIZADA ( NUMBER PICKER BULTOS )");
                }
            }
        });

        cantidad_bultos.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        return cantidad_bultos;
    }

    private TextView generarTextViewPrecioTabla(String precio, TableRow.LayoutParams params, Context contexto)
    {
        TextView precio_producto = new TextView(contexto);
        DecimalFormat priceFormat = new DecimalFormat("###,###.##");
        String output = priceFormat.format(Double.parseDouble(precio));
        precio_producto.setText(output);
        precio_producto.setTextColor(Color.DKGRAY);
        precio_producto.setGravity(Gravity.CENTER);
        precio_producto.setLayoutParams(params);
        precio_producto.setTextSize(16f);
        return precio_producto;
    }

    private TextView generarTextViewParesTabla(String pares, TableRow.LayoutParams params, Context contexto)
    {
        TextView pares_producto = new TextView(contexto);
        pares_producto.setText(pares);
        pares_producto.setTextColor(Color.DKGRAY);
        pares_producto.setGravity(Gravity.CENTER);
        pares_producto.setLayoutParams(params);
        pares_producto.setTextSize(16f);
        return pares_producto;
    }

    private TextView generarTextViewNumeracionTabla(String numeracion, TableRow.LayoutParams params, Context contexto)
    {
        TextView numeracion_producto = new TextView(contexto);
        numeracion_producto.setText(numeracion);
        numeracion_producto.setTextColor(Color.DKGRAY);
        numeracion_producto.setGravity(Gravity.CENTER);
        numeracion_producto.setLayoutParams(params);
        numeracion_producto.setTextSize(16f);
        return numeracion_producto;
    }

    private TextView generarTextViewModeloTabla(String nombre_modelo, TableRow.LayoutParams params, Context contexto)
    {
        TextView modelo = new TextView(contexto);
        modelo.setText(nombre_modelo);
        modelo.setTextColor(Color.DKGRAY);
        modelo.setGravity(Gravity.CENTER);
        modelo.setLayoutParams(params);
        modelo.setTextSize(16f);
        return modelo;
    }

    private ImageView generarImageViewTabla(final String nombre_modelo, TableRow.LayoutParams params, Context contexto)
    {
        ImageView imagen = new ImageView(contexto);

        final File file;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
        {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/" + nombre_modelo + Constantes.EXTENSION_IMG);
        }
        else
        {
            file = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/" + nombre_modelo + Constantes.EXTENSION_IMG);
        }

        if (file.exists())
        {
            imagen.setImageBitmap(Funciones.decodeSampledBitmapFromResource(file, IMG_WIDTH, IMG_HEIGHT));
            imagen.setScaleType(ESCALADO);
            imagen.setLayoutParams(params);
            imagen.setPadding(2, 2, 2, 2);
        }
        else
        {
            Log.e(TAG, "La imagen no pudo ser localizada..");
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.img_notfound);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, IMG_WIDTH, IMG_HEIGHT, true));
            imagen.setImageDrawable(d);
            imagen.setScaleType(ESCALADO);
            imagen.setLayoutParams(params);
            imagen.setPadding(2, 2, 2, 2);
        }

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(EditarPedido.this);

                dialog.setTitle(R.string.confirmacion_eliminar_producto_pedido);
                dialog.setMessage("Producto seleccionado: " + nombre_modelo);
                dialog.setCancelable(false);
                dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eliminarProductoPedido(nombre_modelo);
                    }
                });

                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog.show();
            }
        });

        return imagen;
    }

    private class async_editarPedido extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarPedido.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Editando el pedido...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                //enviamos y recibimos y analizamos los datos en segundo plano.
                if (editarPedido())
                {
                    return "ok";
                }
                else
                {
                    Log.d(TAG, "err");
                    return "err";
                }
            }
            catch (RuntimeException e)
            {
                Log.d(TAG, "Error: " + e);
                return "err2";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();

            if (result.equals("ok"))
            {
                AlertDialog.Builder dialog = new AlertDialog.Builder(EditarPedido.this);

                dialog.setTitle(R.string.confirmacion);
                dialog.setMessage("¿Desea aprobar el pedido recien editado?");
                dialog.setCancelable(false);
                dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        // TODO: EDITAR EL ESTATUS DEL PEDIDO RECIEN CREADO A APROBADO..?

                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Pedido editado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige a la pantalla de Home
                        Intent c = new Intent(EditarPedido.this, Consultar.class);
                        c.putExtra("usuario",usuario);
                        startActivity(c);

                        Consultar.fa.finish();

                        // Prevent the user to go back to this activity
                        finish();
                    }
                });

                dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Pedido editado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige a la pantalla de Home
                        Intent c = new Intent(EditarPedido.this, Consultar.class);
                        c.putExtra("usuario", usuario);
                        startActivity(c);

                        Consultar.fa.finish();

                        // Prevent the user to go back to this activity
                        finish();
                    }
                });

                dialog.show();
            }
            else
            {
                Toast.makeText(contexto, "Hubo un error editando el pedido..", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Edita el pedido utilizando data de la tabla pedidos_editar
     * @return True si la operacion fue exitosa, false en caso contrario.
     */
    private boolean editarPedido()
    {
        return manager.editarPedido(id_pedido) > 0;
    }
}