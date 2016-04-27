package com.tufano.tufanomovil.gestion.productos;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.tufano.tufanomovil.global.FuncionesMenus;
import com.tufano.tufanomovil.global.FuncionesTablas;
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
    public static Activity fa;
    public static String   destacado_filtrado; // 1 new - 0 old - 2 all
    private final String TAG        = "ConsultarProductos";
    private final int    id_mensaje = Funciones.generateViewId();
    private Context          contexto;
    private DBAdapter        manager;
    private ListView         list;
    private productosAdapter adapter;
    private int              limit; // Numero total de elementos
    private String           usuario, columna_ordenada, orden, id_tipo_filtrado, id_talla_filtrado,
            id_color_filtrado, nombre_modelo_filtrado;
    private LinearLayout       layout;
    private List<List<String>> contenedor_colores;
    private TextView           cabecera_1, cabecera_2, cabecera_3, cabecera_4, cabecera_5, cabecera_6;
    private Menu menu = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_consultar);
        Log.d(TAG, "onCreate");
        destacado_filtrado = "2";

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);
        fa = this;

        getExtrasVar();
        createToolBar();
        noInitialFocus();
        initTextViewHeader();
        initRadioButton();
        initFloatingActionButton();

        columna_ordenada = "talla";
        orden = "ASC";
        id_tipo_filtrado = null;
        id_color_filtrado = null;
        id_talla_filtrado = null;
        nombre_modelo_filtrado = null;

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
                    case R.id.rb_old:
                        Log.i(TAG, "rb_old");
                        destacado_filtrado = "0";
                        break;
                    case R.id.rb_new:
                        Log.i(TAG, "rb_new");
                        destacado_filtrado = "1";
                        break;
                    case R.id.rb_all:
                        Log.i(TAG, "rb_all");
                        destacado_filtrado = "2";
                        break;
                }

                new reCargarDatos().execute();
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
     * Obtiene el ID del color a partir de contenedor con todos los colores.
     *
     * @return ID del color.
     */
    private String obtenerIDColor(String id)
    {
        return FuncionesTablas.obtenerIDColor(id, manager);
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
        new reCargarDatos().execute();
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

    private void loadData(boolean filtrando)
    {
        Log.i(TAG, "Inicializando tabla.. Ordenando por: " + columna_ordenada + ", orden: " + orden
                + " Filtrando por... Tipo: " + id_tipo_filtrado + ", Talla: " + id_talla_filtrado +
                ", Color: " + id_color_filtrado + ", Modelo: " + nombre_modelo_filtrado +
                ", Destacado: " + destacado_filtrado);

        eliminarMensajeInformativo();
        list = (ListView) findViewById(R.id.list);
        final List<Producto> datos = new ArrayList<>();
        final Activity       a     = this;

        // Numero de registros que existen con dichos parametros de filtrado..
        Cursor cursor2 = manager.cargarProductos_Filtrado_Ordenado(id_tipo_filtrado, id_talla_filtrado,
                id_color_filtrado, nombre_modelo_filtrado, columna_ordenada, orden, destacado_filtrado);
        limit = cursor2.getCount();
        cursor2.close();

        Cursor cursor = manager.cargarProductos_Filtrado_Ordenado(id_tipo_filtrado, id_talla_filtrado,
                id_color_filtrado, nombre_modelo_filtrado, columna_ordenada, orden,
                CANT_DATOS_MOSTRAR_INICIALMENTE, 0, destacado_filtrado);

        if (cursor.getCount() > 0)
        {
            mostrarTodo();
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
                // No tengo ningun registro en la BD.
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
                                    agregarMensaje(R.string.msj_producto_vacio);

                                    /*TextView mensaje = new TextView(contexto);
                                    mensaje.setText(R.string.msj_producto_vacio);
                                    mensaje.setGravity(Gravity.CENTER);
                                    mensaje.setTextSize(20f);
                                    mensaje.setId(id_mensaje);

                                    LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor_base);
                                    contenedor.addView(mensaje);*/
                                }
                            });
                        }
                    }
                };
                hilo.start();
            }
            else
            {
                // No hay productos que concuerden con dichos parametros de filtrado.
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
                                    //ocultarTodo();
                                    agregarMensaje(R.string.sin_resultados);

                                    /*
                                    TextView mensaje = new TextView(contexto);
                                    mensaje.setText(R.string.sin_resultados);
                                    mensaje.setGravity(Gravity.CENTER);
                                    mensaje.setTextSize(20f);
                                    mensaje.setId(id_mensaje);

                                    LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor_base);
                                    contenedor.addView(mensaje);*/
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

    private void agregarMensaje(final int msj)
    {
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
                            Log.i(TAG, "Agregando mensaje");
                            TextView mensaje = new TextView(contexto);
                            mensaje.setText(msj);
                            mensaje.setGravity(Gravity.CENTER);
                            mensaje.setTextSize(20f);
                            mensaje.setId(id_mensaje);
                            mensaje.setLayoutParams(
                                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT));

                            LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor_base);
                            contenedor.addView(mensaje);
                        }
                    });
                }
            }
        };
        hilo1.start();
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
            Cursor cursor = manager.cargarProductos_Filtrado_Ordenado(id_tipo_filtrado, id_talla_filtrado,
                    id_color_filtrado, nombre_modelo_filtrado, columna_ordenada, orden, CANT_DATOS_CARGAR,
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

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_consultar_productos, menu);

        this.menu = menu;

        // Associate searchable configuration with the SearchView
        SearchManager    searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView    = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setSubmitButtonEnabled(true); // add a submit button

        TextView searchText = (TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_GO)
                {
                    Log.i(TAG, "SEARCH PRESSED..");
                    if (searchView.getQuery().toString().equals(""))
                    {
                        noFilter();
                        return true;
                    }
                }
                return false;
            }
        });

        final MenuItem menuItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(menuItem,
                new MenuItemCompat.OnActionExpandListener()
                {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item)
                    {
                        // Do something when collapsed
                        /*if (temp_query.equals(""))
                        {
                            Log.i(TAG, "Clear filter..");
                            noFilter();
                        }*/
                        //Log.i(TAG, "collapsed");
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item)
                    {
                        // Do something when expanded
                        Log.i(TAG, "expanded");

                        if (nombre_modelo_filtrado != null)
                        {
                            Log.i(TAG, "default value " + nombre_modelo_filtrado);
                            // Vuelve a colocar el valor por defecto en mi barra de busqueda superior.
                            //EditText searchText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

                            searchView.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    //MenuItemCompat.expandActionView(menuItem); // expandir
                                    searchView.setQuery(nombre_modelo_filtrado, false);
                                }
                            });

                            searchView.clearFocus();
                        }

                        return true; // Return true to expand action view
                    }
                });

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextChange(String newText)
            {
                //Log.i(TAG, "onQueryTextChange: " + newText);
                // this is your adapter that will be filtered
                return false; // false = default behavior , true = user's defined behavior
            }

            @Override
            public boolean onQueryTextSubmit(String query)
            {
                //Log.i(TAG, "onQueryTextSubmit: " + query);
                //Here u can get the value "query" which is entered in the search box.
                return false; // false = default behavior , true = user's defined behavior
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            FuncionesMenus.mostrarConfiguracion(usuario, contexto, ConsultarProductos.this);
            return true;
        }
        else if (id == R.id.profile_settings)
        {
            FuncionesMenus.mostrarPerfil(usuario, contexto, ConsultarProductos.this);
            return true;
        }
        else if (id == R.id.action_filter)
        {
            mostrarFiltros();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        Log.i(TAG, "onNewIntent");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent)
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            nombre_modelo_filtrado = intent.getStringExtra(SearchManager.QUERY);
            //Log.i(TAG, "query: " + nombre_modelo_filtrado);
            hideSearchBar();
            new reCargarDatos().execute();
        }
    }

    private void noFilter()
    {
        Log.i(TAG, "Sin filtro de modelo");
        nombre_modelo_filtrado = null;
        hideSearchBar();
        new reCargarDatos().execute();
    }

    private void hideSearchBar()
    {
        layout.requestFocus();
        cerrarTeclado();

        // Collapse the search bar
        if (menu != null)
        {
            MenuItem menuItem = menu.findItem(R.id.action_search);
            menuItem.collapseActionView();
        }
    }

    /**
     * Funcion para cerrar el teclado.
     */
    private void cerrarTeclado()
    {
        InputMethodManager inputManager =
                (InputMethodManager) contexto.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null)
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        else
            Log.w(TAG, "No se pudo ocultar el Teclado");
    }

    private void mostrarFiltros()
    {
        final Dialog main_filters_dialog = new Dialog(ConsultarProductos.this);
        main_filters_dialog.setContentView(R.layout.custom_dialog_filters_productos);
        main_filters_dialog.setTitle("Opciones de filtrado");
        main_filters_dialog.setCanceledOnTouchOutside(true);

        /* Inicializando componentes internos del custom_dialog (EditText) */

        final EditText selected_color = (EditText) main_filters_dialog.findViewById(R.id.color_name_dialog);

        /* Inicializando componentes internos del custom_dialog (Spinners) */

        final Spinner tipo_dialog  = (Spinner) main_filters_dialog.findViewById(R.id.tipo_sp_dialog);
        final Spinner talla_dialog = (Spinner) main_filters_dialog.findViewById(R.id.talla_sp_dialog);

        final List<List<String>> contenedor_tipos = manager.cargarListaTipos();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item,
                contenedor_tipos.get(1));

        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        tipo_dialog.setAdapter(dataAdapter);

        if (id_tipo_filtrado != null)
        {
            String nombre_tipo = FuncionesTablas.obtenerNombreTipo(id_tipo_filtrado, manager);
            int    pos         = Funciones.buscarPosicionElemento(nombre_tipo, tipo_dialog);
            tipo_dialog.setSelection(pos);
        }

        final List<List<String>> contenedor_tallas = manager.cargarListaTallas();
        dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, contenedor_tallas.get(1));
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        talla_dialog.setAdapter(dataAdapter);

        if (id_talla_filtrado != null)
        {
            String nombre_talla = FuncionesTablas.obtenerNombreTalla_Numeracion(id_talla_filtrado, manager);
            int    pos          = Funciones.buscarPosicionElemento(nombre_talla, talla_dialog);
            talla_dialog.setSelection(pos);
        }

        if (id_color_filtrado != null)
        {
            String nombre_color = FuncionesTablas.obtenerNombreColor(id_color_filtrado, manager);
            selected_color.setText(nombre_color);
        }

        /* Inicializando componentes internos del custom_dialog (Buttons) */

        Button btn_buscar_color = (Button) main_filters_dialog.findViewById(R.id.select_color_btn_dialog);
        Button btn_cancelar     = (Button) main_filters_dialog.findViewById(R.id.btn_cancelar_dialog);
        Button btn_filtrar      = (Button) main_filters_dialog.findViewById(R.id.btn_filtrado_dialog);
        Button btn_nofiltros    = (Button) main_filters_dialog.findViewById(R.id.btn_nofilters_dialog);

        btn_nofiltros.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tipo_dialog.setSelection(0);
                talla_dialog.setSelection(0);
                selected_color.setText("Seleccione un color..");
                Toast.makeText(contexto, "Filtros reestablecidos por defecto!", Toast.LENGTH_LONG).show();
            }
        });

        btn_cancelar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                main_filters_dialog.dismiss();
            }
        });

        btn_filtrar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                main_filters_dialog.dismiss();

                String tipo  = tipo_dialog.getSelectedItem().toString();
                String talla = talla_dialog.getSelectedItem().toString();
                String color = selected_color.getText().toString();

                String defaultValueTipo  = tipo_dialog.getItemAtPosition(0).toString();
                String defaultValueTalla = talla_dialog.getItemAtPosition(0).toString();
                String defaultValueColor = "Seleccione un color..";

                int pos = tipo_dialog.getSelectedItemPosition();

                if (tipo.equals(defaultValueTipo)) tipo = null;
                else tipo = contenedor_tipos.get(0).get(pos - 1);

                pos = talla_dialog.getSelectedItemPosition();

                if (talla.equals(defaultValueTalla)) talla = null;
                else talla = contenedor_tallas.get(0).get(pos - 1);

                if (color.equals(defaultValueColor)) color = null;
                else color = obtenerIDColor(color);

                id_tipo_filtrado = tipo;
                id_talla_filtrado = talla;
                id_color_filtrado = color;

                Log.i(TAG, "Filtrare por.. Tipo: " + tipo + ", Talla: " + talla + ", Color: " + color);
                new reCargarDatos().execute();
            }
        });

        btn_buscar_color.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Dialog dialog = new Dialog(ConsultarProductos.this);
                dialog.setContentView(R.layout.custom_dialog_colores);
                dialog.setTitle("Por favor, seleccione un color.");
                dialog.setCanceledOnTouchOutside(true);

                AutoCompleteTextView autoComplete = (AutoCompleteTextView) dialog.findViewById(R.id.autoComplete);
                final ListView       list_data    = (ListView) dialog.findViewById(R.id.list_data);

                contenedor_colores = manager.cargarListaColores();

                ArrayAdapter<String> adapter = new ArrayAdapter<>(contexto,
                        R.layout.simple_list1, contenedor_colores.get(1));

                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

                list_data.setAdapter(adapter);
                list_data.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        Log.i(TAG, "Has seleccionado: " + list_data.getItemAtPosition(position) + ", position: " + position);
                        //Toast.makeText(contexto, "Has seleccionado: " + list_data.getItemAtPosition(position), Toast.LENGTH_LONG).show();
                        selected_color.setText(list_data.getItemAtPosition(position).toString());
                        dialog.dismiss();
                        //gestionarFiltrado();
                    }
                });

                autoComplete.setAdapter(adapter);
                autoComplete.setThreshold(1);
                autoComplete.setOnKeyListener(new View.OnKeyListener()
                {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event)
                    {
                        if (keyCode == 66)
                        {
                            layout.requestFocus();
                            //gestionarFiltrado();
                            return true;
                        }
                        return false;
                    }
                });

                autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        //gestionarFiltrado();
                    }
                });

                dialog.show();
            }
        });

        /* Mostrando dialogo */

        main_filters_dialog.show();
        LinearLayout focus = (LinearLayout) main_filters_dialog.findViewById(R.id.layout_dialog_focus);
        focus.requestFocus();
        //noInitialFocus();
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
            loadData(false);
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
            loadData(true);
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