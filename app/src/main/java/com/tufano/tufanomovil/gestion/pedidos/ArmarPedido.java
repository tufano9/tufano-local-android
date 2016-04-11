package com.tufano.tufanomovil.gestion.pedidos;

import android.app.Activity;
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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created por Usuario Tufano on 20/01/2016.
 */
public class ArmarPedido extends AppCompatActivity
{
    private String usuario, id_cliente;
    private Context contexto;
    private final String TAG = "ArmarPedido";
    private ProgressDialog pDialog;
    private DBAdapter manager;
    public static Activity fa;
    private static final int IMG_WIDTH = 130;
    private static final int IMG_HEIGHT = 50;
    private static final ImageView.ScaleType ESCALADO = ImageView.ScaleType.CENTER_INSIDE;
    private TextView cabecera_1, cabecera_2, cabecera_3, cabecera_4, cabecera_5, cabecera_6, cabecera_7;
    private String columna_ordenada, orden;
    private ArrayList<View> filas;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_pedido);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);
        fa = this;

        createToolBar();
        getExtrasVar();
        inicializarBotones();
        cargarDatosCliente();
        initTextViewHeader();
        columna_ordenada = "producto_modelo";
        orden = "ASC";

        new cargarDatos().execute();
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.agregar_pedido_subtitulo);
        setSupportActionBar(toolbar);
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        usuario = bundle.getString("usuario");
        id_cliente = bundle.getString("id_cliente");
    }

    /**
     * Inicializacion de la cabecera de la tabla.
     */
    private void initTextViewHeader()
    {
        cabecera_1 = (TextView) findViewById(R.id.cabecera_1);
        cabecera_1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_1, "producto_modelo");
            }
        });

        cabecera_2 = (TextView) findViewById(R.id.cabecera_2);
        cabecera_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_2, "producto_color");
            }
        });

        cabecera_3 = (TextView) findViewById(R.id.cabecera_3);
        cabecera_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_3, "numeracion");
            }
        });

        cabecera_4 = (TextView) findViewById(R.id.cabecera_4);
        cabecera_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_4, "cantidad_pares");
            }
        });

        cabecera_5 = (TextView) findViewById(R.id.cabecera_5);
        cabecera_5.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_5, "precio_unitario");
            }
        });

        cabecera_6 = (TextView) findViewById(R.id.cabecera_6);
        cabecera_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_6, "cantidad_bultos");
            }
        });

        cabecera_7 = (TextView) findViewById(R.id.cabecera_7);
        cabecera_7.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_7, "subtotal");
            }
        });
    }

    /**
     *
     * @param cabecera TextView sobre la cual se le aplicara el CompoundDrawable resultante
     * @param columnaBD Nombre de la columna en Base de Datos sobre la cual se ordenara..
     */
    private void cabeceraPresionada(TextView cabecera, String columnaBD)
    {
        Drawable[] cd = cabecera.getCompoundDrawables();
        boolean lleno = false;

        for (Drawable aCd : cd)
        {
            if (aCd != null)
            {
                lleno = true;
                break;
            }
        }

        if(lleno)
        {
            // Invertir orden.. (Si estaba ordenando en ASC, ahora lo ahora DESC
            invertirCompoundDrawable(cabecera, columnaBD);
        }
        else
        {
            // Habia otro que ya estaba seleccionado, por lo tanto debo borrar todos los
            // headers y colocar el compoundDrawable sobre este que se acaba de seleccionar.
            ArrayList<TextView> cabeceras = new ArrayList<>();
            cabeceras.add(cabecera_1);
            cabeceras.add(cabecera_2);
            cabeceras.add(cabecera_3);
            cabeceras.add(cabecera_4);
            cabeceras.add(cabecera_5);
            cabeceras.add(cabecera_6);
            cabeceras.add(cabecera_7);
            limpiarHeaders(cabeceras);
            colocarCompoundDrawable(cabecera);
            ordenarTabla(columnaBD, "ASC");
        }
    }

    /**
     * Coloca el drawable sobre la cabecera presionada (Icono)
     * @param cabecera Cabecera presionada.
     */
    private void colocarCompoundDrawable(TextView cabecera)
    {
        Log.d(TAG, "Colocando..");
        int drawable = Funciones.getAscDrawable();
        cabecera.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);
        cabecera.setTag("ASC");
    }

    /**
     * Funcion que prepara el ordenamiento de la tabla, segun parametros de entrada.
     * @param cabecera Cabebera de la tabla que sera ordenada.
     * @param orden Orden en el cual estara (ASC o DESC)
     */
    private void ordenarTabla(String cabecera, String orden)
    {
        columna_ordenada = cabecera;
        this.orden = orden;
        limpiarTabla();
        new cargarDatos().execute();
    }

    /**
     * Limpia la tabla y su contenido.
     */
    private void limpiarTabla()
    {
        Log.d(TAG, "Limpiando Tabla.. ("+filas.size()+" filas)");
        final TableLayout tabla = (TableLayout) findViewById(R.id.tabla_contenido);
        for (int i = 0; i < filas.size(); i++)
        {
            Log.d(TAG, "Limpiando Tabla.. ("+(i+1)+"/"+filas.size()+" filas)");
            tabla.removeView(filas.get(i));
        }
    }

    /**
     * Invierte el drawable que exista actualmente en la cabecera de la tabla, es decir, si esta en
     * forma ASC, cambia a forma DESC y viceversa.
     * @param cabecera Cabecera afectada por la operacion.
     * @param columnaBD Columna de la base de datos que sera afectada (Orden).
     */
    private void invertirCompoundDrawable(TextView cabecera, String columnaBD)
    {
        Log.d(TAG, "Invirtiendo..");
        Drawable[] d = cabecera.getCompoundDrawables();
        /*for (int i = 0; i < d.length; i++)
        {
            Log.d(TAG, i+": "+d[i]);
        }*/

        Drawable draw = ResourcesCompat.getDrawable(getResources(), R.drawable.arrow_up, null);
        if(draw!=null)
        {
            Drawable.ConstantState upArrow = draw.getConstantState();
            //Log.d(TAG, "if: " + upArrow);

            if( d[2].getConstantState().equals( upArrow ) )
            {
                Log.d(TAG, "Colocare DESC..");
                int drawable = Funciones.getDescDrawable();
                cabecera.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);
                cabecera.setTag("DESC");
                ordenarTabla(columnaBD, "DESC");
            }
            else
            {
                Log.d(TAG, "Colocare ASC..");
                int drawable = Funciones.getAscDrawable();
                cabecera.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);
                cabecera.setTag("ASC");
                ordenarTabla(columnaBD, "ASC");
            }
        }

    }

    /**
     * Limpia todas las cabeceras (Elimina los drawables)
     */
    private void limpiarHeaders(ArrayList<TextView> cabeceras)
    {
        Log.d(TAG, "Limpiando Headers..");
        for (int i = 0; i < cabeceras.size(); i++)
        {
            cabeceras.get(i).setCompoundDrawables(null, null, null, null);
        }
    }

    /**
     * Carga los datos del cliente en cuestion y asigna el resultado a los campos correspondientes.
     */
    private void cargarDatosCliente()
    {
        TextView razonSocial = (TextView) findViewById(R.id.rs_cliente_pedido);
        TextView rifCliente = (TextView) findViewById(R.id.rif_cliente_pedido);
        TextView estadoCliente = (TextView) findViewById(R.id.estado_cliente_pedido);

        List<String> datos_clientes = obtenerDatosCliente(id_cliente);

        razonSocial.setText(datos_clientes.get(0));
        rifCliente.setText(datos_clientes.get(1));
        estadoCliente.setText(datos_clientes.get(2));
    }

    /**
     * Obtiene los datos del cliente de la BD.
     * @param id ID del cliente a consultar.
     * @return Lista con los datos del cliente. (Razon social, Rif, Estado, Telefono, Email,
     * Direccion, Estatus)
     */
    private List<String> obtenerDatosCliente(String id)
    {
        return manager.cargarDatosClientes(id);
    }

    /**
     * Obtiene del vendedor (usuario actual) de la BD.
     * @param usuario ID del usuario a consultar.
     * @return Lista con los datos del vendedor. (Cedula, Nombre, Apellido, Email, Telefono, Estado)
     */
    private List<String> obtenerDatosVendedor(String usuario)
    {
        return manager.buscarUsuario_ID2(usuario);
    }

    /**
     * Inicializa los botones de cancelar pedido, realizar pedido y agregar producto al pedido.
     */
    private void inicializarBotones()
    {
        //LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayout_MainActivity);
        //layout.requestFocus();

        Button btn_cancelar_agregar_pedido = (Button) findViewById(R.id.btn_cancelar_agregar_pedido);
        btn_cancelar_agregar_pedido.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ArmarPedido.this);

                dialog.setTitle("Confirmacion");
                dialog.setMessage(R.string.confirmacion_cancelar_pedido);
                dialog.setCancelable(false);
                dialog.setPositiveButton("Estoy seguro", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new async_cancelarPedidoTemporal().execute();
                    }
                });

                dialog.setNegativeButton("No", new DialogInterface.OnClickListener()
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

        Button btn_continuar_agregar_pedido = (Button) findViewById(R.id.btn_continuar_agregar_pedido);
        btn_continuar_agregar_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Cursor cursor = manager.cargarPedidoTemporal();

                if (cursor.getCount() > 0)
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ArmarPedido.this);

                    dialog.setMessage(R.string.confirmacion_realizar_pedido);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Estoy seguro", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            new realizarPedido().execute();
                        }
                    });

                    dialog.setNegativeButton("Volver", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                }
                else
                {
                    Toast.makeText(contexto, "¡Debe tener algun producto en el pedido!", Toast.LENGTH_LONG).show();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent c = new Intent(ArmarPedido.this, AgregarProductoPedido.class);
                c.putExtra("usuario", usuario);
                c.putExtra("id_cliente", id_cliente);
                startActivity(c);
            }
        });
    }

    /**
     * Carga los datos de la tabla en segundo plano
     */
    private class cargarDatos extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(ArmarPedido.this);
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
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
        }

    }

    /**
     * Inicializa la tabla, funcion principal.
     */
    private void inicializarTabla()
    {
        Log.d(TAG, "Reiniciando variable filas..");
        filas = new ArrayList<>();
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

                            Log.i(TAG, "Inicializando tabla.. Ordenando por: "+columna_ordenada+", orden: "+orden);
                            final TableLayout tabla = (TableLayout) findViewById(R.id.table_crear_pedidos);
                            final TableLayout contenido = (TableLayout) findViewById(R.id.tabla_contenido);
                            Double precio_total = 0.0;

                            int total_pares = 0;
                            int total_bultos = 0;

                            final TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

                            // Llenando la tabla de forma iterativa
                            Cursor cursor = manager.cargarPedidoTemporal_Ordenado(columna_ordenada, orden);

                            if (cursor.getCount() > 0)
                            {
                                mostrarTodo(contenido, tabla);

                                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
                                {
                                    Log.i(TAG, "Agregando fila..");

                                    final TableRow fila = new TableRow(contexto);
                                    //final String id_pedido = String.valueOf(cursor.getInt(0));
                                    final String id_producto = String.valueOf(cursor.getInt(1));
                                    //final String talla = String.valueOf(cursor.getString(2));
                                    final String numeracion = cursor.getString(3);
                                    final String pares = cursor.getString(4);
                                    final String bultos = cursor.getString(5);
                                    final String precio = cursor.getString(6);
                                    final String subtotal = cursor.getString(7);
                                    //final String tipo = cursor.getString(8);
                                    //final String nombre_modelo = cursor.getString(9);
                                    final String color = cursor.getString(10);

                                    precio_total += Double.parseDouble(precio) * Integer.parseInt(pares) * Integer.parseInt(bultos);
                                    total_pares += (Integer.parseInt(pares) * Integer.parseInt(bultos));
                                    total_bultos += Integer.parseInt(bultos);

                                    final String nombre_modelo = obtenerNombreProducto(id_producto);

                                    //Log.i(TAG, "Producto: " + nombre_modelo + " Precio: " + Double.parseDouble(precio) + "*" + Integer.parseInt(pares) + "*" + Integer.parseInt(bultos) + " = "+Double.parseDouble(precio)*Integer.parseInt(pares)*Integer.parseInt(bultos));

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
                                            AlertDialog.Builder dialog = new AlertDialog.Builder(ArmarPedido.this);

                                            dialog.setTitle(R.string.confirmacion_eliminar_producto_pedido);
                                            dialog.setMessage("Producto seleccionado: " + nombre_modelo);
                                            dialog.setCancelable(false);
                                            dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    eliminarProductoPedido(id_producto);
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

                                        /* Modelo */
                                    TextView modelo = new TextView(contexto);
                                    modelo.setText(nombre_modelo);
                                    modelo.setTextColor(Color.DKGRAY);
                                    modelo.setGravity(Gravity.CENTER);
                                    modelo.setLayoutParams(params);
                                    modelo.setTextSize(16f);

                                        /* Talla */
                                        /*TextView talla_producto = new TextView(contexto);
                                        talla_producto.setText(talla);
                                        talla_producto.setTextColor(Color.DKGRAY);
                                        talla_producto.setGravity(Gravity.CENTER);
                                        talla_producto.setLayoutParams(params);
                                        talla_producto.setTextSize(16f);*/

                                        /* Color */
                                    TextView color_producto = new TextView(contexto);
                                    color_producto.setText(color);
                                    color_producto.setTextColor(Color.DKGRAY);
                                    color_producto.setGravity(Gravity.CENTER);
                                    color_producto.setLayoutParams(params);
                                    color_producto.setTextSize(16f);

                                        /* Numeracion */
                                    TextView numeracion_producto = new TextView(contexto);
                                    numeracion_producto.setText(numeracion);
                                    numeracion_producto.setTextColor(Color.DKGRAY);
                                    numeracion_producto.setGravity(Gravity.CENTER);
                                    numeracion_producto.setLayoutParams(params);
                                    numeracion_producto.setTextSize(16f);

                                        /* Pares por bulto */
                                    TextView pares_bulto = new TextView(contexto);
                                    pares_bulto.setText(pares);
                                    pares_bulto.setTextColor(Color.DKGRAY);
                                    pares_bulto.setGravity(Gravity.CENTER);
                                    pares_bulto.setLayoutParams(params);
                                    pares_bulto.setTextSize(16f);

                                        /* Precio */
                                    TextView precio_producto = new TextView(contexto);
                                    precio_producto.setText( Funciones.formatoPrecio(precio) );
                                    precio_producto.setTextColor(Color.DKGRAY);
                                    precio_producto.setGravity(Gravity.CENTER);
                                    precio_producto.setLayoutParams(params);
                                    precio_producto.setTextSize(16f);

                                        /* Bultos */
                                    final NumberPicker cantidad_bultos = new NumberPicker(contexto);
                                    cantidad_bultos.setMinValue(1);
                                    cantidad_bultos.setMaxValue(99);
                                    cantidad_bultos.setValue(Integer.parseInt(bultos));
                                    cantidad_bultos.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                                    cantidad_bultos.setLayoutParams(params);

                                    final TextView sub_total = new TextView(contexto);
                                    sub_total.setText(Funciones.formatoPrecio(subtotal));
                                    sub_total.setTextColor(Color.DKGRAY);
                                    sub_total.setGravity(Gravity.CENTER);
                                    sub_total.setLayoutParams(params);
                                    sub_total.setTextSize(16f);

                                    cantidad_bultos.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

                                        @Override
                                        public void onValueChange(NumberPicker picker, int cantBultosViejos, int cantBultosNuevos) {
                                            // Actualizar BD

                                            String nuevo_subtotal = String.valueOf(Double.parseDouble(precio) * Integer.parseInt(pares) * cantBultosNuevos);

                                            if (manager.actualizarBultosBDTemporal(id_producto, cantBultosNuevos, nuevo_subtotal) > 0) {
                                                // Actualizar Cant.Bultos / Pares y Total Bs.F (Parte Inf)
                                                ArrayList<String> datosNuevos = manager.calcularTotalesNuevosPedidoTemp();
                                                actualizarInformacionInferior(Double.parseDouble(datosNuevos.get(0)), datosNuevos.get(1), datosNuevos.get(2));
                                                sub_total.setText(Funciones.formatoPrecio(nuevo_subtotal));
                                            } else {
                                                Log.i(TAG, "BD NO ACTUALIZADA ( NUMBER PICKER BULTOS )");
                                            }
                                        }
                                    });

                                    cantidad_bultos.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

                                        /* Opciones */
                                        /*Button eliminar = new Button(contexto);
                                        eliminar.setBackgroundResource(android.R.drawable.ic_delete);
                                        eliminar.setLayoutParams(new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                                        eliminar.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                AlertDialog.Builder dialog = new AlertDialog.Builder(ArmarPedido.this);

                                                dialog.setTitle(R.string.confirmacion_eliminar_producto_pedido);
                                                dialog.setMessage("Producto seleccionado: " + nombre_modelo);
                                                dialog.setCancelable(false);
                                                dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        eliminarProductoPedido(id_producto);
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
                                        });*/


                                    //LinearLayout opciones = new LinearLayout(contexto);
                                    //opciones.setGravity(Gravity.CENTER);
                                    //opciones.setLayoutParams(params);
                                    //opciones.addView(eliminar);

                                    TableRow.LayoutParams layout = new TableRow.LayoutParams(0,TableRow.LayoutParams.MATCH_PARENT);
                                    imagen.setLayoutParams(layout);
                                    modelo.setLayoutParams(layout);
                                    color_producto.setLayoutParams(layout);
                                    numeracion_producto.setLayoutParams(layout);
                                    pares_bulto.setLayoutParams(layout);
                                    precio_producto.setLayoutParams(layout);
                                    cantidad_bultos.setLayoutParams(layout);
                                    sub_total.setLayoutParams(layout);
                                    //opciones.setLayoutParams(layout);

                                    // Llenando la fila con data
                                    fila.addView(imagen);
                                    fila.addView(modelo);
                                    fila.addView(color_producto);
                                    fila.addView(numeracion_producto);
                                    fila.addView(pares_bulto);
                                    fila.addView(precio_producto);
                                    fila.addView(cantidad_bultos);
                                    fila.addView(sub_total);
                                    //fila.addView(opciones);
                                    fila.setPadding(0, 2 , 0, 0);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                        fila.setBackground(Funciones.intToDrawable(contexto, R.drawable.table_border));
                                    else
                                        //noinspection deprecation
                                        fila.setBackgroundDrawable(Funciones.intToDrawable(contexto, R.drawable.table_border));

                                    Log.d(TAG, "FILA ADD..");
                                    filas.add(fila);

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
                                Log.d(TAG, "Vacio..");
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

                                                    //filas.add(fila);

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

    /**
     * Elimina un producto del pedido.
     * @param id_producto ID del producto a eliminar.
     */
    private void eliminarProductoPedido(String id_producto)
    {
        int filas_afectadas = manager.eliminarProductoPedidoTemporal(id_producto);

        if(filas_afectadas>0)
        {
            Toast.makeText(contexto, "¡Se ha eliminado correctamente el producto del pedido!", Toast.LENGTH_LONG).show();
            Intent c = new Intent(ArmarPedido.this, ArmarPedido.class);
            c.putExtra("usuario", usuario);
            c.putExtra("id_cliente", id_cliente);
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
     * Muestra todos los componentes de la tabla.
     * @param tabla Tabla 1
     * @param tab Tabla 2
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

    /**
     * Oculta todos los componentes de la tabla.
     * @param tabla Tabla 1
     * @param tab Tabla 2
     */
    private void ocultarTodo(TableLayout tabla, TableLayout tab)
    {
        LinearLayout botones_opciones = (LinearLayout) findViewById(R.id.botones_opciones);
        LinearLayout datos_pedido = (LinearLayout) findViewById(R.id.datos_pedido);

        datos_pedido.setVisibility(View.INVISIBLE);
        botones_opciones.setVisibility(View.INVISIBLE);
        tabla.setVisibility(View.INVISIBLE);
        tab.setVisibility(View.INVISIBLE);
    }

    /**
     * Obtiene el nombre del producto a traves de su ID.
     * @param id ID del producto a consultar.
     * @return Nombre del producto.
     */
    private String obtenerNombreProducto(String id)
    {
        Cursor cursor = manager.cargarProductosId(id);
        if(cursor.moveToFirst())
            return cursor.getString(0);
        cursor.close();
        return null;
    }

    /**
     * Actualiza la informacion en la parte inferior de la tabla.
     * @param precio_total Precio total del pedido.
     * @param cant_bultos Cantidad total de bultos del pedido.
     * @param cant_pares Cantidad de pares total del pedido.
     */
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

                            precioFinal.setText( Funciones.formatoPrecio(String.valueOf(precio_total)) );
                            cantidadBultos.setText(cant_bultos);
                            cantidadPares.setText( Funciones.formatoPrecio(cant_pares) );
                        }
                    });

                }
            }
        };
        hilo.start();
    }

    /**
     * Actualiza la informacion en la parte inferior de la tabla.
     * @param precio_total Precio total del pedido.
     * @param cantidad_productos Cantidad total de productos del pedido.
     * @param cantidad_bultos Cantidad total de bultos del pedido.
     * @param cantidad_pares Cantidad de pares total del pedido.
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

                            cantidadProductos.setText(String.valueOf(cantidad_productos));
                            precioFinal.setText( Funciones.formatoPrecio(String.valueOf(precio_total)) );
                            cantidadBultos.setText( String.valueOf(cantidad_bultos) );
                            cantidadPares.setText( Funciones.formatoPrecio(String.valueOf(cantidad_pares)) );
                        }
                    });

                }
            }
        };
        hilo.start();
    }

    /**
     * Clase para cancelar en segundo plano un pedido temporal.
     */
    private class async_cancelarPedidoTemporal extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(ArmarPedido.this);
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
                if (cancelarPedidoTemporal())
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
                Intent c = new Intent(ArmarPedido.this, Menu.class);
                c.putExtra("usuario",usuario);
                startActivity(c);

                Menu.fa.finish();

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
     * Clase para realizar el pedido en segundo plano.
     */
    private class realizarPedido extends AsyncTask <String, String, String>
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(ArmarPedido.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Realizando el pedido...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            long res = realizarPedido(id_cliente);

            if (res == -1)
            {
                Log.d(TAG, "err");
                return "err";
            }
            else
            {
                return "ok";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            if(result.equals("ok"))
            {
                if (cancelarPedidoTemporal())
                {
                    Toast.makeText(contexto, "¡El pedido fue realizado exitosamente!", Toast.LENGTH_LONG).show();

                    Intent c = new Intent(ArmarPedido.this, Menu.class);
                    c.putExtra("usuario", usuario);
                    startActivity(c);
                    SeleccionarCliente.fa.finish();
                    Menu.fa.finish();
                    finish();
                }
                else
                {
                    Log.d(TAG, "err");
                    Toast.makeText(contexto, "¡Ha ocurrido un error eliminando el pedido!", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                Toast.makeText(contexto, "¡Ha ocurrido un error realizado el pedido!", Toast.LENGTH_LONG).show();
            }

            pDialog.dismiss();
        }
    }

    /**
     * Realiza el pedido al cliente indicado.
     * @param id_cliente ID del cliente al que se le realizara el pedido.
     * @return Id del pedido (de haberse realizado), -1 en caso contrario.
     */
    private long realizarPedido(String id_cliente)
    {
        // String array [5] con los valores del pedido en el siguiente orden: id_Cliente,
        // id_vendedor , monto, estatus, observaciones
        String monto = obtenerMontoPedidoLocal();
        String observaciones = "Prueba de una observacion..";

        ArrayList<String> datos = new ArrayList<>();
        List<String> datos_cliente = obtenerDatosCliente(id_cliente);
        List<String> datos_vendedor = obtenerDatosVendedor(usuario);

        for (int i = 0; i <datos_cliente.size()-1; i++)
        {
            datos.add(datos_cliente.get(i));
        }

        for (int i = 0; i <datos_vendedor.size(); i++)
        {
            datos.add(datos_vendedor.get(i));
        }

        datos.add(monto);
        datos.add("1");
        datos.add(observaciones);
        return manager.agregarPedido(datos);
    }

    /**
     * Cancela el pedido temporal
     * @return True si la operacion fue exitosa, False en caso contrario.
     */
    private boolean cancelarPedidoTemporal()
    {
        return manager.borrarPedidoTemporal() > 0;
    }

    /**
     * Calcula el monto del pedido local.
     * @return Monto del pedido local.
     */
    private String obtenerMontoPedidoLocal()
    {
        Cursor c = manager.cargarPedidoTemporal();
        double montos = 0.0;

        for ( c.moveToFirst(); !c.isAfterLast(); c.moveToNext() )
        {
            montos += Double.parseDouble(c.getString(7));
            //montos += Double.parseDouble(c.getString(4)) * Integer.parseInt(c.getString(5)) * Integer.parseInt(c.getString(6));
        }
        c.close();

        return String.valueOf(montos);
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
}