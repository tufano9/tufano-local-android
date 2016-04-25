package com.tufano.tufanomovil.gestion.pedidos;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.adapters.pedidosAdapter;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.EndlessScrollListener;
import com.tufano.tufanomovil.global.Funciones;
import com.tufano.tufanomovil.objetos.Pedido;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Usuario Tufano onn 01/02/2016.
 */
public class ConsultarPedidos extends AppCompatActivity
{
    private static final int CANT_DATOS_MOSTRAR_INICIALMENTE = 10;
    private static final int CANT_DATOS_CARGAR               = 5;
    public static Activity fa;
    private final int    id_mensaje = Funciones.generateViewId();
    private final String TAG        = "ConsultarPedidos";
    private ListView       list;
    private pedidosAdapter adapter;
    private int            limit; // Numero total de elementos
    private String         usuario;
    private Context        contexto;
    private ProgressDialog pDialog;
    private DBAdapter      manager;
    private Spinner        clientes, estatus;
    private TextView cabecera_1, cabecera_2, cabecera_3, cabecera_4, cabecera_5, cabecera_6;
    private String columna_ordenada, orden;
    private String cliente_filtrado = null, estatus_filtrado = null;
    //private ArrayList<View> filas;
    private boolean primerCargaTabla = true, primerInicio1 = true, primerInicio2 = true;
    private boolean filtrando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_pedidos);

        fa = this;
        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        getExtrasVar();
        createToolBar();
        initTextViewHeader();
        initFloatingActionButton();

        columna_ordenada = "_id_pedido";
        orden = "ASC";

        new cargarDatos().execute();
    }

    /**
     * Inicializa el FAB para agregar un producto..
     */
    private void initFloatingActionButton()
    {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent c = new Intent(ConsultarPedidos.this, SeleccionarCliente.class);
                c.putExtra("usuario", usuario);
                startActivity(c);
            }
        });
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        usuario = bundle.getString("usuario");
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.consultar_pedido_subtitulo);
        setSupportActionBar(toolbar);
    }

    /**
     * Inicializa la cabecera de la tabla
     */
    private void initTextViewHeader()
    {
        cabecera_1 = (TextView) findViewById(R.id.cabecera_1);
        cabecera_1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_1, "_id_pedido");
            }
        });

        cabecera_2 = (TextView) findViewById(R.id.cabecera_2);
        cabecera_2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_2, "nombre_vendedor");
            }
        });

        cabecera_3 = (TextView) findViewById(R.id.cabecera_3);
        cabecera_3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_3, "razon_social_cliente");
            }
        });

        cabecera_4 = (TextView) findViewById(R.id.cabecera_4);
        cabecera_4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_4, "fecha");
            }
        });

        cabecera_5 = (TextView) findViewById(R.id.cabecera_5);
        cabecera_5.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_5, "monto");
            }
        });

        cabecera_6 = (TextView) findViewById(R.id.cabecera_6);
        cabecera_6.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_6, "estatus");
            }
        });
    }

    /**
     * Funcion para la gestion de la cabecera de la tabla presionada.
     *
     * @param cabecera  Cabecera presionada.
     * @param columnaBD Columna de la base de datos a ordenar.
     */
    private void cabeceraPresionada(TextView cabecera, String columnaBD)
    {
        Drawable[] cd    = cabecera.getCompoundDrawables();
        boolean    lleno = false;

        for (Drawable aCd : cd)
        {
            if (aCd != null)
            {
                lleno = true;
                break;
            }
        }

        if (lleno)
        {
            // Invertir orden.. (Si estaba ordenando en ASC, ahora lo ahora DESC
            invertirCompoundDrawable(cabecera, columnaBD);
        }
        else
        {
            // Habia otro que ya estaba seleccionado, por lo tanto debo borrar todos los
            // headers y colocar el compoundDrawable sobre este que se acaba de seleccionar.
            limpiarHeaders();
            colocarCompoundDrawable(cabecera);
            ordenarTabla(columnaBD, "ASC");
        }
    }

    /**
     * Funcion que prepara el ordenamiento de la tabla, segun parametros de entrada.
     *
     * @param cabecera Cabebera de la tabla que sera ordenada.
     * @param orden    Orden en el cual estara (ASC o DESC)
     */
    private void ordenarTabla(String cabecera, String orden)
    {
        columna_ordenada = cabecera;
        this.orden = orden;
        limpiarTabla();
        new cargarDatos().execute();
    }

    /**
     * Invierte el drawable que exista actualmente en la cabecera de la tabla, es decir, si esta en
     * forma ASC, cambia a forma DESC y viceversa.
     *
     * @param cabecera  Cabecera afectada por la operacion.
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
        if (draw != null)
        {
            Drawable.ConstantState upArrow = draw.getConstantState();
            //Log.d(TAG, "if: " + upArrow);

            if (d[2].getConstantState().equals(upArrow))
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
     * Coloca el drawable sobre la cabecera presionada (Icono)
     *
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
     * Limpia todas las cabeceras (Elimina los drawables)
     */
    private void limpiarHeaders()
    {
        Log.d(TAG, "Limpiando..");
        // El tercer parametro es el RIGHT
        cabecera_1.setCompoundDrawables(null, null, null, null);
        cabecera_2.setCompoundDrawables(null, null, null, null);
        cabecera_3.setCompoundDrawables(null, null, null, null);
        cabecera_4.setCompoundDrawables(null, null, null, null);
        cabecera_5.setCompoundDrawables(null, null, null, null);
        cabecera_6.setCompoundDrawables(null, null, null, null);
        //cabecera_1.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
    }

    /**
     * Limpia la tabla y su contenido.
     */
    private void limpiarTabla()
    {
        /*final TableLayout tabla = (TableLayout) findViewById(R.id.table_consultar_pedidos);
        for (int i = 0; i < filas.size(); i++)
        {
            tabla.removeView(filas.get(i));
        }*/
    }

    /**
     * Inicializa los spinners de clientes y status
     */
    private void initSpinners()
    {
        Log.w(TAG, "initSpinners");
        clientes = (Spinner) findViewById(R.id.spCliente);
        estatus = (Spinner) findViewById(R.id.spEstatus);

        clientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (!primerInicio1)
                {
                    gestionarFiltrado();
                }
                else
                {
                    primerInicio1 = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        estatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (!primerInicio2)
                {
                    gestionarFiltrado();
                }
                else
                {
                    primerInicio2 = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //primerInicio = false;
    }

    /**
     * Carga de informacion en los Spinners de clientes y estatus.
     */
    private void loadSpinnerData()
    {
        List<List<String>>   contenedor_clientes = manager.cargarListaClientes();
        ArrayAdapter<String> dataAdapter         = new ArrayAdapter<>(this, R.layout.spinner_item, contenedor_clientes.get(1));
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        clientes.setAdapter(dataAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(contexto, R.array.estatus, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        estatus.setAdapter(adapter);
    }

    /**
     * Funcion que gestiona el filtrado, se selecciona bajo que parametros ocurrira el filtrado.
     */
    private void gestionarFiltrado()
    {
        //cerrarTeclado();
        String cliente, estatus;
        String defaultValueCliente = clientes.getItemAtPosition(0).toString();
        String defaultValueEstatus = this.estatus.getItemAtPosition(0).toString();
        filtrando = false;

        if (clientes.getSelectedItem().toString().equals(defaultValueCliente))
        {
            cliente = null;
        }
        else
        {
            //int pos = clientes.getSelectedItemPosition();
            cliente = clientes.getSelectedItem().toString();
            filtrando = true;
        }

        if (this.estatus.getSelectedItem().toString().equals(defaultValueEstatus))
        {
            estatus = null;
        }
        else
        {
            // Le paso como parametro el string..
            // En espera de aprobacion, Aprobado, Cancelado..
            // Y me debe regresar 1, 2, 0 (Respectivamente)..
            estatus = obtenerIDEstatus(this.estatus.getSelectedItem().toString());
            filtrando = true;
            //estatus = this.estatus.getSelectedItem().toString();
        }

        filtrarTabla(cliente, estatus);
    }

    /**
     * Obtiene el ID del estatus, a partir de una cadena, por ej. "Aprobado" = 2
     *
     * @param estatus Cadena con el estatus.
     * @return El ID del estatus.
     */
    private String obtenerIDEstatus(String estatus)
    {
        //final String[] estatus_internos = getResources().getStringArray(R.array.estatus);
        switch (estatus)
        {
            case "En espera de aprobacion":
                return "1";
            case "Aprobado":
                return "2";
            case "Cancelado":
                return "0";
            default:
                return null;
        }
    }

    /**
     * Prepara el filtrado de la tabla
     *
     * @param cliente Cliente con el cual se filtrara.
     * @param estatus Estatus con el cual se filtrara.
     */
    private void filtrarTabla(String cliente, String estatus)
    {
        Log.w(TAG, "Filtrando.. cliente: " + cliente + ", estatus: " + estatus);
        limpiarTabla();
        cliente_filtrado = cliente;
        estatus_filtrado = estatus;
        new cargarDatos().execute(cliente, estatus);
    }

    /**
     * Metodo principal con el cual se inicializara la tabla.
     */
    private void inicializarTabla()
    {
        Log.i(TAG, "Inicializando tabla.. Ordenando por: " + columna_ordenada + " de forma " + orden);

        eliminarMensajeInformativo();
        list = (ListView) findViewById(R.id.list);
        final List<Pedido> datos = new ArrayList<>();
        final Activity     a     = this;

        // Numero de registros que existen con dichos parametros de filtrado..
        Cursor cursor2 = manager.cargarPedidosOrdenadosPor(columna_ordenada, orden,
                cliente_filtrado, estatus_filtrado);
        limit = cursor2.getCount();
        cursor2.close();

        Cursor cursor = manager.cargarPedidosOrdenadosPor(columna_ordenada, orden,
                cliente_filtrado, estatus_filtrado, CANT_DATOS_MOSTRAR_INICIALMENTE, 0);

        if (cursor.getCount() > 0)
        {
            mostrarTodo();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                Log.i(TAG, "Agregando fila..");

                try
                {
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(cursor.getString(4));

                    final String id_pedido       = String.valueOf(cursor.getInt(0));
                    final String razon_social    = String.valueOf(cursor.getString(1));
                    final String nombre_vendedor = String.valueOf(cursor.getString(2));
                    final String monto_pedido    = Funciones.formatoPrecio(cursor.getString(3));
                    final String fecha_pedido    = new SimpleDateFormat("dd-MM-yyyy h:mm a", Locale.US).format(date);
                    final String estatus_pedido  = convertirEstatus(String.valueOf(cursor.getString(5)));
                    final String observaciones   = String.valueOf(cursor.getString(6));

                    Pedido pedido = new Pedido(id_pedido, razon_social, nombre_vendedor, monto_pedido,
                            fecha_pedido, estatus_pedido, observaciones);

                    datos.add(pedido);
                }
                catch (ParseException e)
                {
                    Log.e(TAG, "Ha ocurrido un error egregando la fila.. (" + e + ")");
                    //e.printStackTrace();
                }
            }
        }
        else
        {
            Log.d(TAG, "No encontre nada..");
            // Se realiza un condicional para saber si estoy filtrando, de estar filtrando o no, el
            // mensaje obtenido cambiara.
            if (!filtrando)
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
                                    ocultarTodo();
                                    agregarMensaje(R.string.msj_sin_pedidos);
                                }
                            });
                        }
                    }
                };
                hilo.start();
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
                                    ocultarTodo();
                                    agregarMensaje(R.string.sin_resultados);
                                }
                            });
                        }
                    }
                };
                hilo.start();
            }
        }
        cursor.close();

        final Thread hilo1 = new Thread()
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
                            adapter = new pedidosAdapter(a, datos, contexto, usuario);

                            list.setAdapter(adapter);
                            list.setOnScrollListener(new EndlessScrollListener()
                            {
                                @Override
                                public boolean onLoadMore(int page, int totalItemsCount)
                                {
                                    String msj = "onLoadMore : page = " + page + "," +
                                            " totalItemsCount = " + totalItemsCount;
                                    Log.i(TAG, msj);

                                    // Triggered only when new data needs to be appended to the list
                                    // Add whatever code is needed to append new items to your
                                    // AdapterView
                                    customLoadMoreDataFromApi(totalItemsCount);

                                    // ONLY if more data is actually being loaded; false otherwise.
                                    return true;
                                }
                            });
                        }
                    });
                }
            }
        };
        hilo1.start();
    }

    private void agregarMensaje(int msj)
    {
        TextView mensaje = new TextView(contexto);
        mensaje.setText(msj);
        mensaje.setGravity(Gravity.CENTER);
        mensaje.setTextSize(20f);
        mensaje.setId(id_mensaje);

        LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor_base);
        contenedor.addView(mensaje);
    }

    // Append more data into the adapter
    public void customLoadMoreDataFromApi(int totalItemsCount)
    {
        // This method probably sends out a network request and appends new data items to your adapter.
        // Use the page value and add it as a parameter to your API request to retrieve paginated data.
        // Deserialize API response and then construct new objects to append to the adapter

        if (totalItemsCount >= limit)
        {
            Toast.makeText(getApplicationContext(), "No hay mas elementos que mostrar.",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            Cursor cursor = manager.cargarPedidosOrdenadosPor(columna_ordenada, orden,
                    cliente_filtrado, estatus_filtrado, CANT_DATOS_CARGAR, totalItemsCount);

            if (cursor.getCount() > 0)
            {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
                {
                    Log.i(TAG, "Creando Data.. (Añadido)");
                    try
                    {
                        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                Locale.US).parse(cursor.getString(4));

                        final String id_pedido       = String.valueOf(cursor.getInt(0));
                        final String razon_social    = String.valueOf(cursor.getString(1));
                        final String nombre_vendedor = String.valueOf(cursor.getString(2));
                        final String monto_pedido    = Funciones.formatoPrecio(cursor.getString(3));
                        final String fecha_pedido = new SimpleDateFormat("dd-MM-yyyy h:mm a",
                                Locale.US).format(date);
                        final String estatus_pedido = convertirEstatus(cursor.getString(5));
                        final String observaciones  = String.valueOf(cursor.getString(6));

                        Pedido pedido = new Pedido(id_pedido, razon_social, nombre_vendedor, monto_pedido,
                                fecha_pedido, estatus_pedido, observaciones);

                        adapter.add(pedido);
                    }
                    catch (ParseException e)
                    {
                        Log.e(TAG, "Ha ocurrido un error egregando la fila.. (" + e + ")");
                        //e.printStackTrace();
                    }
                }
            }
            cursor.close();

            ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
        }
    }

    /**
     * Asigna un equivalente al codigo del estatus de entrada, por ej: 0 = Cancelado, 2 = Aprobado
     *
     * @param codigo_estatus Codigo del estatus (Numerico)
     * @return El estatus equivalente al codigo.
     */
    private String convertirEstatus(String codigo_estatus)
    {
        switch (codigo_estatus)
        {
            case "0":
                return "Cancelado";
            case "1":
                return "En espera de aprobacion";
            case "2":
                return "Aprobado";
            default:
                return "Error";
        }
    }

    private void eliminarMensajeInformativo()
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
                            // Elimina el mensaje (De haberlo) que muestra que no se encontraron registros en la BD.
                            TextView     mensaje    = (TextView) findViewById(id_mensaje);
                            LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor_base);
                            contenedor.removeView(mensaje);
                        }
                    });
                }
            }
        };
        hilo.start();
    }

    /**
     * Funcion encargada de ocultar los elementos de la tabla.
     */
    private void ocultarTodo()
    {
        findViewById(R.id.contenedor).setVisibility(View.GONE);
    }

    /**
     * Funcion encargada de mostrar los elementos de la tabla.
     */
    private void mostrarTodo()
    {
        findViewById(R.id.contenedor).setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    /**
     * Carga los datos de la tabla en 2do plano.
     */
    private class cargarDatos extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            if (primerCargaTabla)
            {
                initSpinners();
                loadSpinnerData();
                primerCargaTabla = false;
            }
            inicializarTabla();
            return null;
        }

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(ConsultarPedidos.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
        }
    }
}