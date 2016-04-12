package com.tufano.tufanomovil.gestion.pedidos;

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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

/**
 * Created by Usuario Tufano onn 01/02/2016.
 */
public class DetallesPedido extends AppCompatActivity
{
    private String usuario, id_pedido;
    private Context contexto;
    private final String TAG = "DetallesPedido";
    private ProgressDialog pDialog;
    private DBAdapter manager;
    //public static Activity fa;
    private static final int IMG_WIDTH = 130;
    private static final int IMG_HEIGHT = 50;
    private static final ImageView.ScaleType ESCALADO = ImageView.ScaleType.CENTER_INSIDE;
    private Button cancelar, aprobar, editar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_pedidos);

        //fa = this;
        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        getExtrasVar();
        createToolBar();

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
     * Funcion encargada de inicializar los botones
     */
    private void inicializarBotones()
    {
        cancelar = (Button) findViewById(R.id.btn_cancelar_pedido);
        aprobar = (Button) findViewById(R.id.btn_aprobar_pedido);
        editar = (Button) findViewById(R.id.btn_editar_pedido);

        cancelar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder dialog = new AlertDialog.Builder(DetallesPedido.this);

                dialog.setMessage(R.string.confirmacion_cancelar_pedido);
                dialog.setCancelable(false);
                dialog.setPositiveButton("SI", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new async_cancelarPedido().execute();
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
        });

        aprobar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder dialog = new AlertDialog.Builder(DetallesPedido.this);

                dialog.setMessage(R.string.confirmacion_aprobar_pedido);
                dialog.setCancelable(false);
                dialog.setPositiveButton("SI", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new async_aprobarPedido().execute();
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
        });

        editar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent c = new Intent(DetallesPedido.this, EditarPedido.class);
                c.putExtra("usuario", usuario);
                c.putExtra("id_pedido", id_pedido);
                startActivity(c);
            }
        });
    }

    /**
     * Busca y asigna los datos de los clientes a los campos.
     */
    private void cargarDatosCliente()
    {
        TextView razonSocial = (TextView) findViewById(R.id.rs_cliente_pedido);
        TextView rifCliente = (TextView) findViewById(R.id.rif_cliente_pedido);
        TextView estadoCliente = (TextView) findViewById(R.id.estado_cliente_pedido);

        // Obtiene los datos del cliente del pedido actual
        List<String> datos_clientes = obtenerDatosCliente(id_pedido);

        // Asigna los datos del cliente a los campos correspondientes.
        razonSocial.setText(datos_clientes.get(0));
        rifCliente.setText(datos_clientes.get(1));
        estadoCliente.setText(datos_clientes.get(2));
    }

    /**
     * Se obtienen los datos del cliente mediante un id proporcionado.
     * @param id ID del cliente.
     * @return Lista con los datos del cliente. (Razon social, Rif y Estado)
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
        }
        return datos_clientes;
    }

    /**
     * Clase encargada de realizar la carga en 2do plano de los datos del pedido.
     */
    class cargarDatos extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(DetallesPedido.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            inicializarTabla();
            inicializarBotones();
            cargarDatosCliente();
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
            bloquearBotones();
        }

        /**
         * Se bloquean los botones si el pedido ya fue gestionado (aprobado o rechazado), esto para
         * evitar que se modifique nuevamente.
         */
        private void bloquearBotones()
        {
            Cursor cursor = manager.cargarIDClientePedido(id_pedido);
            String estatus = "1";

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                estatus = cursor.getString(4); // id_pedido
                Log.d(TAG, "estatus: "+estatus);
            }

            if(! estatus.equals("1") )
            {
                cancelar.setEnabled(false);
                aprobar.setEnabled(false);
                editar.setEnabled(false);
            }
        }

        /**
         * Funcion principal encargada de la carga de informacion a la tabla.
         */
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
                                Cursor cursor = manager.cargarPedidosDetalles(id_pedido);

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

                                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
                                    {
                                        Log.i(TAG, "Agregando fila..");


                                        final TableRow fila = new TableRow(contexto);
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

                                        Log.i(TAG, "Producto: " + nombre_modelo + " Precio: " + Double.parseDouble(precio) + "*" + Integer.parseInt(pares) + "*" + Integer.parseInt(bultos) + " = "+Double.parseDouble(precio)*Integer.parseInt(pares)*Integer.parseInt(bultos));

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

                                        /* Modelo */
                                        TextView modelo = new TextView(contexto);
                                        modelo.setText(nombre_modelo);
                                        modelo.setTextColor(Color.DKGRAY);
                                        modelo.setGravity(Gravity.CENTER);
                                        modelo.setLayoutParams(params);
                                        modelo.setTextSize(16f);

                                        /* Numeracion */
                                        TextView numeracion_producto = new TextView(contexto);
                                        numeracion_producto.setText(numeracion);
                                        numeracion_producto.setTextColor(Color.DKGRAY);
                                        numeracion_producto.setGravity(Gravity.CENTER);
                                        numeracion_producto.setLayoutParams(params);
                                        numeracion_producto.setTextSize(16f);

                                        /* Pares */
                                        TextView pares_producto = new TextView(contexto);
                                        pares_producto.setText(pares);
                                        pares_producto.setTextColor(Color.DKGRAY);
                                        pares_producto.setGravity(Gravity.CENTER);
                                        pares_producto.setLayoutParams(params);
                                        pares_producto.setTextSize(16f);

                                        /* Precio */
                                        TextView precio_producto = new TextView(contexto);
                                        DecimalFormat priceFormat = new DecimalFormat("###,###.##");
                                        String output = priceFormat.format(Double.parseDouble(precio));
                                        precio_producto.setText(output);
                                        precio_producto.setTextColor(Color.DKGRAY);
                                        precio_producto.setGravity(Gravity.CENTER);
                                        precio_producto.setLayoutParams(params);
                                        precio_producto.setTextSize(16f);

                                        /* Bultos */
                                        TextView cantidad_bultos = new TextView(contexto);
                                        cantidad_bultos.setText(bultos);
                                        cantidad_bultos.setTextColor(Color.DKGRAY);
                                        cantidad_bultos.setGravity(Gravity.CENTER);
                                        cantidad_bultos.setLayoutParams(params);
                                        cantidad_bultos.setTextSize(16f);

                                        /* Subtotal */
                                        final TextView sub_total = new TextView(contexto);
                                        sub_total.setText(Funciones.formatoPrecio(subtotal));
                                        sub_total.setTextColor(Color.DKGRAY);
                                        sub_total.setGravity(Gravity.CENTER);
                                        sub_total.setLayoutParams(params);
                                        sub_total.setTextSize(16f);

                                        TableRow.LayoutParams layout = new TableRow.LayoutParams(0,TableRow.LayoutParams.MATCH_PARENT);
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

        /*

        private void actualizarInformacionInferior(final Double precio_total, final String cant_bultos, final String cant_pares)
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
                                TextView precioFinal = (TextView) findViewById(R.id.precioFinal);
                                TextView cantidadBultos = (TextView) findViewById(R.id.cantidad_bultos);
                                TextView cantidadPares = (TextView) findViewById(R.id.cantidad_pares);

                                DecimalFormat priceFormat = new DecimalFormat("###,###.##");
                                String output = priceFormat.format(precio_total);
                                precioFinal.setText(output);
                                cantidadBultos.setText(cant_bultos);
                                cantidadPares.setText(cant_pares);
                            }
                        });

                    }
                }
            };
            hilo.start();
        }

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

        /*

        private void eliminarProductoPedido(String id_producto)
        {
            int filas_afectadas = manager.eliminarProductoPedidoTemporal(id_producto);

            if(filas_afectadas>0)
            {
                Toast.makeText(contexto, "¡Se ha eliminado correctamente el producto del pedido!", Toast.LENGTH_LONG).show();
                Intent c = new Intent(DetallesPedido.this, DetallesPedido.class);
                c.putExtra("usuario", usuario);
                c.putExtra("id_pedido", id_pedido);
                startActivity(c);
                fa.finish();
                finish();
            }
            else
            {
                Toast.makeText(contexto, "¡Ha ocurrido un error eliminando el producto del pedido!", Toast.LENGTH_LONG).show();
            }
        }

        */

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
     * Clase encargada de cancelar un pedido.
     */
    private class async_cancelarPedido extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(DetallesPedido.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cancelando el pedido...");
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
                if (cancelarPedido())
                {
                    return "ok"; //login valido
                }
                else
                {
                    Log.d(TAG, "err");
                    return "err"; //login invalido
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
                // Muestra al usuario un mensaje de operacion exitosa
                Toast.makeText(contexto, "Pedido cancelado exitosamente!!", Toast.LENGTH_LONG).show();

                // Redirige a la pantalla de Home
                Intent c = new Intent(DetallesPedido.this, Consultar.class);
                c.putExtra("usuario",usuario);
                startActivity(c);

                Consultar.fa.finish();

                // Prevent the user to go back to this activity
                finish();
            }
            else
            {
                Toast.makeText(contexto, "Hubo un error cancelando el pedido..", Toast.LENGTH_LONG).show();
            }
        }


    }

    /**
     * Clase encargada de aprobar un pedido.
     */
    private class async_aprobarPedido extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(DetallesPedido.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Aprobando el pedido...");
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
                if (aprobarPedido())
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
                // Muestra al usuario un mensaje de operacion exitosa
                Toast.makeText(contexto, "Pedido aprobado exitosamente!!", Toast.LENGTH_LONG).show();

                // Redirige a la pantalla de Home
                Intent c = new Intent(DetallesPedido.this, Consultar.class);
                c.putExtra("usuario",usuario);
                startActivity(c);

                Consultar.fa.finish();

                // Prevent the user to go back to this activity
                finish();
            }
            else
            {
                Toast.makeText(contexto, "Hubo un error cancelando el pedido..", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Cancela el pedido
     * @return True si el pedido fue cancelado exitosamente, false en caso contrario.
     */
    private boolean cancelarPedido()
    {
        return manager.cancelarPedido(id_pedido) > 0;
    }

    /**
     * Aprueba el pedido
     * @return True si el pedido fue aprobado exitosamente, false en caso contrario.
     */
    private boolean aprobarPedido()
    {
        return manager.aprobarPedido(id_pedido) > 0;
    }
}