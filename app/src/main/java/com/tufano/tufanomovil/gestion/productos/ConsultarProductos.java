package com.tufano.tufanomovil.gestion.productos;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.adapters.productosAdapter;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.EndlessScrollListener;
import com.tufano.tufanomovil.global.Funciones;
import com.tufano.tufanomovil.objetos.Producto;

import java.util.ArrayList;
import java.util.List;

/**
 * Creado por Gerson el 13/04/2016..
 */
public class ConsultarProductos extends AppCompatActivity
{
    private static final int CANT_DATOS_MOSTRAR_INICIALMENTE = 5;
    private static final int CANT_DATOS_CARGAR               = 3;
    public static  Activity       fa;
    public static String destacado_filtrado = "1";
    private final String TAG                = "ConsultarProductos";
    private final int    id_mensaje         = Funciones.generateViewId();
    private Context          contexto;
    private DBAdapter        manager;
    private ListView         list;
    private productosAdapter adapter;
    private int              limit; // Numero total de elementos
    private String usuario, columna_ordenada, orden, tipo_filtrado, color_filtrado,
            talla_filtrado, modelo_filtrado;
    private boolean primerInicio1 = true, primerInicio2 = true, primerInicio3 = true;
    private AutoCompleteTextView modelo_autoComplete;
    private LinearLayout         layout;
    private List<List<String>>   contenedor_colores, contenedor_tipos, contenedor_tallas;
    private Spinner tipo, talla, color;
    private TextView cabecera_1, cabecera_2, cabecera_3, cabecera_4, cabecera_5, cabecera_6;
    private boolean rotate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_consultar);

        rotate = savedInstanceState != null;

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);
        fa = this;

        getExtrasVar();
        createToolBar();
        noInitialFocus();
        initSpinners();
        initAutoComplete();
        initTextViewHeader();
        initRadioButton();
        initFloatingActionButton();

        columna_ordenada = "talla";
        orden = "ASC";

        new cargarDatos().execute();
    }

    private void initRadioButton()
    {
        selectDefaultRB();
        final RadioGroup rg = (RadioGroup) findViewById(R.id.radio_group_producto);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                //boolean checked = ((RadioButton) view).isChecked();
                switch (rg.getCheckedRadioButtonId())
                {
                    case R.id.rb_new:
                        Log.i(TAG, "rb_new");
                        destacado_filtrado = "1";
                        break;
                    case R.id.rb_old:
                        Log.i(TAG, "rb_old");
                        destacado_filtrado = "0";
                        break;
                    case R.id.rb_all:
                        Log.i(TAG, "rb_all");
                        destacado_filtrado = "2";
                        break;
                }

                new reCargarDatos().execute(tipo_filtrado, talla_filtrado, color_filtrado,
                        modelo_filtrado);

                /*if(!rotate)
                {
                    Log.i(TAG, "Rotate.. reloading");
                    new reCargarDatos().execute(tipo_filtrado, talla_filtrado, color_filtrado,
                            modelo_filtrado);
                }
                else
                {
                    rotate = false;
                }*/
            }
        });
    }

    private void selectDefaultRB()
    {
        // Selecciona por defecto el rb_new (Solo en el primer inicio)..
        switch (destacado_filtrado)
        {
            case "0":
            {
                Log.i(TAG, "SetDefaultChecked rb_old");
                ((RadioButton) findViewById(R.id.rb_old)).setChecked(true);
            }
            break;
            case "1":
            {
                Log.i(TAG, "SetDefaultChecked rb_new");
                ((RadioButton) findViewById(R.id.rb_new)).setChecked(true);
            }
            break;
            case "2":
            {
                Log.i(TAG, "SetDefaultChecked rb_all");
                ((RadioButton) findViewById(R.id.rb_all)).setChecked(true);
            }
            break;

        }
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
                Intent c = new Intent(ConsultarProductos.this, AgregarProducto.class);
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
        if (bundle.getString("selected_cb") != null)
        {
            Log.i(TAG, "getExtrasVar - destacado_filtrado: " + destacado_filtrado);
            destacado_filtrado = bundle.getString("selected_cb");
        }
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.consultar_producto_subtitulo);
        setSupportActionBar(toolbar);
    }

    /**
     * Evita el focus principal al abrir la activity, el cual despliega automaticamente el teclado
     */
    private void noInitialFocus()
    {
        layout = (LinearLayout) findViewById(R.id.LinearLayout_MainActivity);
        layout.requestFocus();
    }

    /**
     * Inicializador del autocompleteTextView
     */
    private void initAutoComplete()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, obtenerModelos());

        modelo_autoComplete = (AutoCompleteTextView) findViewById(R.id.autoC_modelo);
        modelo_autoComplete.setAdapter(adapter);
        modelo_autoComplete.setThreshold(1);
        modelo_autoComplete.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == 66)
                {
                    layout.requestFocus();
                    gestionarFiltrado();
                    return true;
                }
                return false;
            }
        });

        modelo_autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                gestionarFiltrado();
            }
        });
    }

    /**
     * Obtiene una lista de todos los modelos existentes en la BD
     *
     * @return Lista con todos los modelos.
     */
    private ArrayList<String> obtenerModelos()
    {
        Cursor cursor = manager.cargarProductos();

        ArrayList<String> rs = new ArrayList<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            rs.add(cursor.getString(3));
        }
        return rs;
    }

    /**
     * Cierra el teclado del dispositivo
     */
    private void cerrarTeclado()
    {
        InputMethodManager inputManager =
                (InputMethodManager) contexto.
                        getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null)
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        else
            Log.w(TAG, "No se pudo ocultar el Teclado");
    }

    /**
     * Gestiona los parametros de filtrado de la tabla
     */
    private void gestionarFiltrado()
    {
        Log.i(TAG, "Gestionando el filtrado");
        cerrarTeclado();
        String tipoFunction, tallaFunction, colorFunction, modeloFunction;
        String defaultValueTipo  = tipo.getItemAtPosition(0).toString();
        String defaultValueTalla = talla.getItemAtPosition(0).toString();
        String defaultValueColor = color.getItemAtPosition(0).toString();

        // Si esta seleccionado la opcion por defecto, no filtrare con ese parametro
        if (tipo.getSelectedItem().toString().equals(defaultValueTipo))
        {
            tipoFunction = null;
        }
        else
        {
            int pos = tipo.getSelectedItemPosition();
            tipoFunction = contenedor_tipos.get(0).get(pos - 1);
        }

        if (talla.getSelectedItem().toString().equals(defaultValueTalla))
        {
            tallaFunction = null;
        }
        else
        {
            int pos = talla.getSelectedItemPosition();
            tallaFunction = contenedor_tallas.get(0).get(pos - 1);
        }

        if (color.getSelectedItem().toString().equals(defaultValueColor))
        {
            colorFunction = null;
        }
        else
        {
            int pos = color.getSelectedItemPosition();
            colorFunction = contenedor_colores.get(0).get(pos - 1);
        }

        if (modelo_autoComplete.getText().toString().trim().equals(""))
        {
            modeloFunction = null;
        }
        else
        {
            modeloFunction = modelo_autoComplete.getText().toString().trim();
        }

        filtrarTabla(tipoFunction, tallaFunction, colorFunction, modeloFunction);
    }

    /**
     * Prepara el filtrado de la tabla
     *
     * @param tipo   Tipo del producto para el filtrado. Por ej 'Torera'
     * @param talla  Talla del producto para el filtrado.
     * @param color  Color del producto para el filtrado.
     * @param modelo Modelo del producto para el filtrado.
     */
    private void filtrarTabla(String tipo, String talla, String color, String modelo)
    {
        Log.w(TAG, "Filtrando.. Tipo: " + tipo + ", Color: " + color + ", Talla: " + talla + ", Modelo: " + modelo);
        limpiarTabla();
        tipo_filtrado = tipo;
        talla_filtrado = talla;
        color_filtrado = color;
        modelo_filtrado = modelo;
        new reCargarDatos().execute(tipo, talla, color, modelo);
    }

    /**
     * Funcion encargada de la gestion del click sobre las cabeceras.
     *
     * @param cabecera  TextView sobre la cual se le aplicara el CompoundDrawable resultante
     * @param columnaBD Nombre de la columna en Base de Datos sobre la cual se ordenara..
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
            ArrayList<TextView> cabeceras = new ArrayList<>();
            cabeceras.add(cabecera_1);
            cabeceras.add(cabecera_2);
            cabeceras.add(cabecera_3);
            cabeceras.add(cabecera_4);
            cabeceras.add(cabecera_5);
            cabeceras.add(cabecera_6);
            limpiarHeaders(cabeceras);
            colocarCompoundDrawable(cabecera);
            ordenarTabla(columnaBD, "ASC");
        }
    }

    /**
     * Metodo encargado de colocar el drawable sobre la cabecera.
     *
     * @param cabecera Cabecera a la cual se le colocara el drawable.
     */
    private void colocarCompoundDrawable(TextView cabecera)
    {
        Log.d(TAG, "Colocando..");
        int drawable = Funciones.getAscDrawable();
        cabecera.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);
        cabecera.setTag("ASC");
    }

    /**
     * Ordenar la tabla bajo los parametros indicados.
     *
     * @param cabecera Cabecera bajo la cual se ordenara.
     * @param orden    Orden a utilizar (ASC o DESC)
     */
    private void ordenarTabla(String cabecera, String orden)
    {
        columna_ordenada = cabecera;
        this.orden = orden;
        limpiarTabla();
        //tipo, talla, color, modelo
        new reCargarDatos().execute(tipo_filtrado, talla_filtrado, color_filtrado, modelo_filtrado);
    }

    /**
     * Borra el contenido de la tabla
     */
    private void limpiarTabla()
    {
        //list.setAdapter(null);
        //((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
        /*Log.d(TAG, "Limpiando Tabla.. ("+filas.size()+" filas)");
        final TableLayout tabla = (TableLayout) findViewById(R.id.table_editar_productos);
        for (int i = 0; i < filas.size(); i++)
        {
            tabla.removeView(filas.get(i));
        }*/
    }

    /**
     * Metodo utilizado cuando se presiona sobre una cabecera que esta actualmente presionada bajo
     * un cierto orden, dicho de otra forma, si se esta ordenando de forma ascendente un campo,
     * este metodo hara que se ordene de forma descendente.
     *
     * @param cabecera  La cabecera presionada.
     * @param columnaBD La columna de la BD afectada.
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
     * Elimina todos los compounds drawables de las cabeceras.
     */
    private void limpiarHeaders(ArrayList<TextView> cabeceras)
    {
        Log.d(TAG, "Limpiando..");
        for (int i = 0; i < cabeceras.size(); i++)
        {
            cabeceras.get(i).setCompoundDrawables(null, null, null, null);
        }
    }

    /**
     * Inicializar Spinners de Estados.
     */
    private void initSpinners()
    {
        Log.w(TAG, "initSpinners.. primerInicio1 " + primerInicio1);
        tipo = (Spinner) findViewById(R.id.spTipo_editar_producto);
        talla = (Spinner) findViewById(R.id.spTalla_editar_producto);
        color = (Spinner) findViewById(R.id.spColor_editar_producto);

        tipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (!primerInicio1)
                    gestionarFiltrado();
                else
                    primerInicio1 = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        talla.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (!primerInicio2)
                    gestionarFiltrado();
                else
                    primerInicio2 = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (!primerInicio3)
                    gestionarFiltrado();
                else
                    primerInicio3 = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //primerInicio = false;
    }

    /**
     * Inicializando la cabecera de la tabla
     */
    private void initTextViewHeader()
    {
        cabecera_1 = (TextView) findViewById(R.id.cabecera_1);
        cabecera_1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_1, "talla");
            }
        });

        cabecera_2 = (TextView) findViewById(R.id.cabecera_2);
        cabecera_2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_2, "tipo");
            }
        });

        cabecera_3 = (TextView) findViewById(R.id.cabecera_3);
        cabecera_3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_3, "modelo");
            }
        });

        cabecera_4 = (TextView) findViewById(R.id.cabecera_4);
        cabecera_4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_4, "color");
            }
        });

        cabecera_5 = (TextView) findViewById(R.id.cabecera_5);
        cabecera_5.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_5, "precio");
            }
        });

        cabecera_6 = (TextView) findViewById(R.id.cabecera_6);
        cabecera_6.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cabeceraPresionada(cabecera_6, "numeracion");
            }
        });
    }

    /**
     * Function to load the spinner data from SQLite database
     */
    private void loadSpinnerData()
    {
        /*final Thread hilo = new Thread()
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
                        }
                    });
                }
            }
        };
        hilo.start();*/

        Log.w(TAG, "loadSpinnerData");
        contenedor_colores = manager.cargarListaColores();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, contenedor_colores.get(1));
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        color.setAdapter(dataAdapter);

        contenedor_tipos = manager.cargarListaTipos();
        dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, contenedor_tipos.get(1));
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        tipo.setAdapter(dataAdapter);

        contenedor_tallas = manager.cargarListaTallas();
        dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, contenedor_tallas.get(1));
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        talla.setAdapter(dataAdapter);
    }

    private void loadData(String tipo_filtrado, String talla_filtrado, String color_filtrado,
                          String modelo_filtrado, boolean filtrando)
    {
        Log.i(TAG, "Inicializando tabla.. Ordenando por: " + columna_ordenada + ", orden: " + orden
                + " Filtrando por... Tipo: " + tipo_filtrado + ", Talla: " + talla_filtrado +
                ", Color: " + color_filtrado + ", Modelo: " + modelo_filtrado +
                ", Destacado: " + destacado_filtrado);

        eliminarMensajeInformativo();
        list = (ListView) findViewById(R.id.list);
        final List<Producto> datos = new ArrayList<>();
        final Activity       a     = this;

        // Numero de registros que existen con dichos parametros de filtrado..
        Cursor cursor2 = manager.cargarProductos_Filtrado_Ordenado(tipo_filtrado, talla_filtrado,
                color_filtrado, modelo_filtrado, columna_ordenada, orden, destacado_filtrado);
        limit = cursor2.getCount();
        cursor2.close();

        Cursor cursor = manager.cargarProductos_Filtrado_Ordenado(tipo_filtrado, talla_filtrado,
                color_filtrado, modelo_filtrado, columna_ordenada, orden,
                CANT_DATOS_MOSTRAR_INICIALMENTE, 0, destacado_filtrado);

        if (cursor.getCount() > 0)
        {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                Log.i(TAG, "Agregando fila..");
                final String id_producto         = String.valueOf(cursor.getInt(0));
                final String id_talla            = cursor.getString(1);
                final String id_tipo             = cursor.getString(2);
                final String modelo_nombre       = cursor.getString(3);
                final String id_color            = cursor.getString(4);
                final String precio_producto     = cursor.getString(5);
                final String numeracion_producto = cursor.getString(6);
                final String estatus_producto    = cursor.getString(7);
                final String paresxtalla         = cursor.getString(8);

                Producto p = new Producto(id_producto, id_talla, id_tipo, id_color, modelo_nombre,
                        precio_producto, numeracion_producto, estatus_producto, paresxtalla,
                        contexto);
                datos.add(p);
            }
        }
        else
        {
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
                                    TextView mensaje = new TextView(contexto);
                                    mensaje.setText(R.string.msj_producto_vacio);
                                    mensaje.setGravity(Gravity.CENTER);
                                    mensaje.setTextSize(20f);
                                    mensaje.setId(id_mensaje);

                                    LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor_base);
                                    contenedor.addView(mensaje);
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
                                    TextView mensaje = new TextView(contexto);
                                    mensaje.setText(R.string.sin_resultados);
                                    mensaje.setGravity(Gravity.CENTER);
                                    mensaje.setTextSize(20f);
                                    mensaje.setId(id_mensaje);

                                    LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor_base);
                                    contenedor.addView(mensaje);
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
                            adapter = new productosAdapter(a, datos, contexto, usuario);

                            list.setAdapter(adapter);
                            list.setOnScrollListener(new EndlessScrollListener()
                            {
                                @Override
                                public boolean onLoadMore(int page, int totalItemsCount)
                                {
                                    String msj = "onLoadMore : page = " + page + ", totalItemsCount = " + totalItemsCount;
                                    //Toast.makeText(getApplicationContext(), msj, Toast.LENGTH_SHORT).show();
                                    Log.i(TAG, msj);

                                    // Triggered only when new data needs to be appended to the list
                                    // Add whatever code is needed to append new items to your AdapterView
                                    customLoadMoreDataFromApi(totalItemsCount);

                                    return true; // ONLY if more data is actually being loaded; false otherwise.
                                }
                            });
                        }
                    });
                }
            }
        };
        hilo1.start();

        //adapter.imageLoader.clearCache();
        //adapter.notifyDataSetChanged();

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
            Cursor cursor = manager.cargarProductos_Filtrado_Ordenado(tipo_filtrado, talla_filtrado,
                    color_filtrado, modelo_filtrado, columna_ordenada, orden, CANT_DATOS_CARGAR,
                    totalItemsCount, destacado_filtrado);

            if (cursor.getCount() > 0)
            {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
                {
                    Log.i(TAG, "Creando Data.. (Añadido)");
                    final String id_producto         = String.valueOf(cursor.getInt(0));
                    final String id_talla            = cursor.getString(1);
                    final String id_tipo             = cursor.getString(2);
                    final String modelo_nombre       = cursor.getString(3);
                    final String id_color            = cursor.getString(4);
                    final String precio_producto     = cursor.getString(5);
                    final String numeracion_producto = cursor.getString(6);
                    final String estatus_producto    = cursor.getString(7);
                    final String paresxtalla         = cursor.getString(8);

                    Producto p = new Producto(id_producto, id_talla, id_tipo, id_color,
                            modelo_nombre, precio_producto, numeracion_producto, estatus_producto,
                            paresxtalla, contexto);

                    adapter.add(p);
                }
            }
            cursor.close();

            ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
        }
    }

    /**
     * Clase para la carga en 2do plano de los datos de la tabla (Solo 1era Ejecucion)
     */
    private class cargarDatos extends AsyncTask<String, String, String>
    {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute()
        {
            Log.d(TAG, "Cargando informacion");
            pDialog = new ProgressDialog(ConsultarProductos.this);
            pDialog.setTitle("Por favor espere..");
            pDialog.setMessage("Cargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            loadSpinnerData();
            loadData(null, null, null, null, false);
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
            Log.d(TAG, "Dismiss Dialog");
        }
    }

    /**
     * Clase para la carga en 2do plano de los datos de la tabla (Con Filtros)
     */
    private class reCargarDatos extends AsyncTask<String, String, String>
    {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute()
        {
            Log.d(TAG, "Recargando informacion");
            pDialog = new ProgressDialog(ConsultarProductos.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Recargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            loadData(params[0], params[1], params[2], params[3], true);
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
            Log.d(TAG, "Dismiss Dialog");
        }
    }
}