package com.tufano.tufanomovil.gestion.productos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.Constantes;
import com.tufano.tufanomovil.global.EndlessScrollListener;
import com.tufano.tufanomovil.global.Funciones;
import com.tufano.tufanomovil.global.productosAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creado por Gerson el 13/04/2016..
 */
public class ConsultarProductos extends AppCompatActivity {
    private static final int CANT_DATOS_MOSTRAR_INICIALMENTE = 5;
    //private int CANT_DATOS_ACTUALES = CANT_DATOS_MOSTRAR_INICIALMENTE;
    private static final int CANT_DATOS_CARGAR               = 3;
    public static  Activity       fa;
    private static ProgressDialog pDialog;
    private final String TAG = "ConsultarProductos";
    private Context          contexto;
    private DBAdapter        manager;
    private ListView         list;
    private productosAdapter adapter;
    private int              limit; // Numero total de elementos
    private String           usuario;
    private String           columna_ordenada;
    private String           orden;
    private boolean primerInicio1 = true, primerInicio2 = true, primerInicio3 = true;
    private AutoCompleteTextView modelo_autoComplete;
    private LinearLayout         layout;
    private List<List<String>>   contenedor_colores, contenedor_tipos, contenedor_tallas;
    private Spinner tipo, talla, color;
    private TextView cabecera_1, cabecera_2, cabecera_3, cabecera_4, cabecera_5, cabecera_6;
    private String tipo_filtrado, color_filtrado, talla_filtrado, modelo_filtrado;
    private Activity a;
    //private ArrayList<View> filas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consultar_productos);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);
        fa = this;
        a = ConsultarProductos.this;

        getExtrasVar();
        createToolBar();
        noInitialFocus();
        initSpinners();
        initAutoComplete();
        initTextViewHeader();

        columna_ordenada = "talla";
        orden = "ASC";

        new cargarDatos().execute();
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar() {
        Bundle bundle = getIntent().getExtras();
        usuario = bundle.getString("usuario");
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.editar_producto_subtitulo);
        setSupportActionBar(toolbar);
    }

    /**
     * Evita el focus principal al abrir la activity, el cual despliega automaticamente el teclado
     */
    private void noInitialFocus() {
        layout = (LinearLayout) findViewById(R.id.LinearLayout_MainActivity);
        layout.requestFocus();
    }

    /**
     * Inicializador del autocompleteTextView
     */
    private void initAutoComplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, obtenerModelos());

        modelo_autoComplete = (AutoCompleteTextView) findViewById(R.id.autoC_modelo);
        modelo_autoComplete.setAdapter(adapter);
        modelo_autoComplete.setThreshold(1);
        modelo_autoComplete.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == 66) {
                    layout.requestFocus();
                    gestionarFiltrado();
                    return true;
                }
                return false;
            }
        });

        modelo_autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gestionarFiltrado();
            }
        });
    }

    /**
     * Obtiene una lista de todos los modelos existentes en la BD
     *
     * @return Lista con todos los modelos.
     */
    private ArrayList<String> obtenerModelos() {
        Cursor cursor = manager.cargarProductos();

        ArrayList<String> rs = new ArrayList<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            rs.add(cursor.getString(3));
        }
        return rs;
    }

    /**
     * Cierra el teclado del dispositivo
     */
    private void cerrarTeclado() {
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
    private void gestionarFiltrado() {
        cerrarTeclado();
        String tipoFunction, tallaFunction, colorFunction, modeloFunction;
        String defaultValueTipo  = tipo.getItemAtPosition(0).toString();
        String defaultValueTalla = talla.getItemAtPosition(0).toString();
        String defaultValueColor = color.getItemAtPosition(0).toString();

        // Si esta seleccionado la opcion por defecto, no filtrare con ese parametro
        if (tipo.getSelectedItem().toString().equals(defaultValueTipo)) {
            tipoFunction = null;
        }
        else {
            int pos = tipo.getSelectedItemPosition();
            tipoFunction = contenedor_tipos.get(0).get(pos - 1);
        }

        if (talla.getSelectedItem().toString().equals(defaultValueTalla)) {
            tallaFunction = null;
        }
        else {
            int pos = talla.getSelectedItemPosition();
            tallaFunction = contenedor_tallas.get(0).get(pos - 1);
        }

        if (color.getSelectedItem().toString().equals(defaultValueColor)) {
            colorFunction = null;
        }
        else {
            int pos = color.getSelectedItemPosition();
            colorFunction = contenedor_colores.get(0).get(pos - 1);
        }

        if (modelo_autoComplete.getText().toString().trim().equals("")) {
            modeloFunction = null;
        }
        else {
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
    private void filtrarTabla(String tipo, String talla, String color, String modelo) {
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
    private void cabeceraPresionada(TextView cabecera, String columnaBD) {
        Drawable[] cd    = cabecera.getCompoundDrawables();
        boolean    lleno = false;

        for (Drawable aCd : cd) {
            if (aCd != null) {
                lleno = true;
                break;
            }
        }

        if (lleno) {
            // Invertir orden.. (Si estaba ordenando en ASC, ahora lo ahora DESC
            invertirCompoundDrawable(cabecera, columnaBD);
        }
        else {
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
    private void colocarCompoundDrawable(TextView cabecera) {
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
    private void ordenarTabla(String cabecera, String orden) {
        columna_ordenada = cabecera;
        this.orden = orden;
        limpiarTabla();
        //tipo, talla, color, modelo
        new reCargarDatos().execute(tipo_filtrado, talla_filtrado, color_filtrado, modelo_filtrado);
    }

    /**
     * Borra el contenido de la tabla
     */
    private void limpiarTabla() {
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
    private void invertirCompoundDrawable(TextView cabecera, String columnaBD) {
        Log.d(TAG, "Invirtiendo..");
        Drawable[] d = cabecera.getCompoundDrawables();
        /*for (int i = 0; i < d.length; i++)
        {
            Log.d(TAG, i+": "+d[i]);
        }*/

        Drawable draw = ResourcesCompat.getDrawable(getResources(), R.drawable.arrow_up, null);
        if (draw != null) {
            Drawable.ConstantState upArrow = draw.getConstantState();
            //Log.d(TAG, "if: " + upArrow);

            if (d[2].getConstantState().equals(upArrow)) {
                Log.d(TAG, "Colocare DESC..");
                int drawable = Funciones.getDescDrawable();
                cabecera.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);
                cabecera.setTag("DESC");
                ordenarTabla(columnaBD, "DESC");
            }
            else {
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
    private void limpiarHeaders(ArrayList<TextView> cabeceras) {
        Log.d(TAG, "Limpiando..");
        for (int i = 0; i < cabeceras.size(); i++) {
            cabeceras.get(i).setCompoundDrawables(null, null, null, null);
        }
    }

    /**
     * Inicializar Spinners de Estados.
     */
    private void initSpinners() {
        Log.w(TAG, "initSpinners");
        tipo = (Spinner) findViewById(R.id.spTipo_editar_producto);
        talla = (Spinner) findViewById(R.id.spTalla_editar_producto);
        color = (Spinner) findViewById(R.id.spColor_editar_producto);

        tipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!primerInicio1)
                    gestionarFiltrado();
                else
                    primerInicio1 = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        talla.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!primerInicio2)
                    gestionarFiltrado();
                else
                    primerInicio2 = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!primerInicio3)
                    gestionarFiltrado();
                else
                    primerInicio3 = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //primerInicio = false;
    }

    /**
     * Inicializando la cabecera de la tabla
     */
    private void initTextViewHeader() {
        cabecera_1 = (TextView) findViewById(R.id.cabecera_1);
        cabecera_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_1, "talla");
            }
        });

        cabecera_2 = (TextView) findViewById(R.id.cabecera_2);
        cabecera_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_2, "tipo");
            }
        });

        cabecera_3 = (TextView) findViewById(R.id.cabecera_3);
        cabecera_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_3, "modelo");
            }
        });

        cabecera_4 = (TextView) findViewById(R.id.cabecera_4);
        cabecera_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_4, "color");
            }
        });

        cabecera_5 = (TextView) findViewById(R.id.cabecera_5);
        cabecera_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_5, "precio");
            }
        });

        cabecera_6 = (TextView) findViewById(R.id.cabecera_6);
        cabecera_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_6, "numeracion");
            }
        });
    }

    /**
     * Function to load the spinner data from SQLite database
     */
    private void loadSpinnerData() {
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

    private void loadData(String tipo_filtrado, String talla_filtrado, String color_filtrado, String modelo_filtrado, boolean filtrando) {
        Log.i(TAG, "Inicializando tabla.. Ordenando por: " + columna_ordenada + ", orden: " + orden
                + " Filtrando por... Tipo: " + tipo_filtrado + ", Talla: " + talla_filtrado + ", Color: "
                + color_filtrado + ", Modelo: " + modelo_filtrado);

        list = (ListView) findViewById(R.id.list);
        final List<List<String>> datos = new ArrayList<>();
        final Activity           a     = this;

        Cursor cursor2 = manager.cargarProductos_Filtrado_Ordenado(tipo_filtrado, talla_filtrado,
                color_filtrado, modelo_filtrado, columna_ordenada, orden);
        // Numero de registros que existen con dichos parametros de filtrado..
        limit = cursor2.getCount();
        cursor2.close();

        Cursor cursor = manager.cargarProductos_Filtrado_Ordenado(tipo_filtrado, talla_filtrado,
                color_filtrado, modelo_filtrado, columna_ordenada, orden,
                CANT_DATOS_MOSTRAR_INICIALMENTE, 0);

        //filas = new ArrayList<>();

        if (cursor.getCount() > 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Log.i(TAG, "Creando Data..");
                //final String id_producto         = String.valueOf(cursor.getInt(0));
                final String id_talla            = cursor.getString(1);
                final String id_tipo             = cursor.getString(2);
                final String modelo_nombre       = cursor.getString(3);
                final String id_color            = cursor.getString(4);
                final String precio_producto     = cursor.getString(5);
                final String numeracion_producto = cursor.getString(6);
                //final String estatus_producto    = cursor.getString(7);
                //final String paresxtalla         = cursor.getString(8);
                final String talla_producto, tipo_producto, color_producto;

                DecimalFormat priceFormat       = new DecimalFormat("###,###.##");
                String        precio_formateado = priceFormat.format(Double.parseDouble(precio_producto));

                talla_producto = obtenerNombreTalla(id_talla);
                tipo_producto = obtenerNombreTipo(id_tipo);
                color_producto = obtenerNombreColor(id_color);

                List<String> producto = new ArrayList<>();
                producto.add(modelo_nombre); // nombre de la img
                producto.add(talla_producto); // talla
                producto.add(tipo_producto); // tipo
                producto.add(modelo_nombre); // modelo
                producto.add(color_producto); // color
                producto.add(precio_formateado); // precio
                producto.add(numeracion_producto); // numeracion
                producto.add("1"); // editar parametros
                producto.add(modelo_nombre + "&" + color_producto + "&" + numeracion_producto); // compartir parametros

                datos.add(producto);
            }
        }
        cursor.close();

        final Thread hilo1 = new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new productosAdapter(a, datos);

                            list.setAdapter(adapter);
                            list.setOnScrollListener(new EndlessScrollListener() {
                                @Override
                                public boolean onLoadMore(int page, int totalItemsCount) {
                                    String msj = "onLoadMore : page = " + page + ", totalItemsCount = " + totalItemsCount;
                                    Toast.makeText(getApplicationContext(), msj, Toast.LENGTH_SHORT).show();
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

    // Append more data into the adapter
    public void customLoadMoreDataFromApi(int totalItemsCount) {
        // This method probably sends out a network request and appends new data items to your adapter.
        // Use the page value and add it as a parameter to your API request to retrieve paginated data.
        // Deserialize API response and then construct new objects to append to the adapter

        if (totalItemsCount >= limit) {
            Toast.makeText(getApplicationContext(), "YA ACABE!", Toast.LENGTH_SHORT).show();
        }
        else {
            int min = totalItemsCount + 1;
            int max = min + CANT_DATOS_CARGAR;

            //String msj = "min: "+min+", max: "+max;
            //Toast.makeText(getApplicationContext(), msj, Toast.LENGTH_LONG).show();

            Log.i(TAG, "min: " + min + ", max: " + max);

            //CANT_DATOS_ACTUALES += CANT_DATOS_CARGAR;

            Cursor cursor = manager.cargarProductos_Filtrado_Ordenado(null, null, null, null, columna_ordenada, orden, CANT_DATOS_CARGAR, totalItemsCount);

            if (cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Log.i(TAG, "Creando Data.. (Añadido)");
                    //final String id_producto         = String.valueOf(cursor.getInt(0));
                    final String id_talla            = cursor.getString(1);
                    final String id_tipo             = cursor.getString(2);
                    final String modelo_nombre       = cursor.getString(3);
                    final String id_color            = cursor.getString(4);
                    final String precio_producto     = cursor.getString(5);
                    final String numeracion_producto = cursor.getString(6);
                    //final String estatus_producto    = cursor.getString(7);
                    //final String paresxtalla         = cursor.getString(8);
                    final String talla_producto, tipo_producto, color_producto;

                    DecimalFormat priceFormat       = new DecimalFormat("###,###.##");
                    String        precio_formateado = priceFormat.format(Double.parseDouble(precio_producto));

                    talla_producto = obtenerNombreTalla(id_talla);
                    tipo_producto = obtenerNombreTipo(id_tipo);
                    color_producto = obtenerNombreColor(id_color);

                    List<String> producto = new ArrayList<>();
                    producto.add(modelo_nombre); // nombre de la img
                    producto.add(talla_producto); // talla
                    producto.add(tipo_producto); // tipo
                    producto.add(modelo_nombre); // modelo
                    producto.add(color_producto); // color
                    producto.add(precio_formateado); // precio
                    producto.add(numeracion_producto); // numeracion
                    producto.add("1"); // editar parametros
                    producto.add(modelo_nombre + "&" + color_producto +"&" + numeracion_producto); // compartir parametros

                    adapter.add(producto);
                }
            }
            cursor.close();

            //adapter.add(producto1);
            //adapter.add(producto2);

            ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
        }
    }

    /**
     * Obtiene el nombre del color de la base de datos, a partir de su ID.
     *
     * @param id_color ID del color a consultar.
     * @return Nombre del color.
     */
    private String obtenerNombreColor(String id_color) {
        //Log.i(TAG, "Buscando color con id: "+id_color);
        String nombre_color = null;
        Cursor cursor       = manager.buscarColor_ID(id_color);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            nombre_color = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Color encontrado: "+nombre_color);
        }
        cursor.close();
        return nombre_color;
    }

    /**
     * Obtiene el nombre de la talla del producto de la BD a partir de su ID.
     *
     * @param id_talla ID de la talla.
     * @return Nombre de la talla del producto.
     */
    private String obtenerNombreTalla(String id_talla) {
        //Log.i(TAG, "Buscando talla con id: "+id_talla);
        String nombre_talla = null;
        Cursor cursor       = manager.buscarTalla_ID(id_talla);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            nombre_talla = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Talla encontrado: "+nombre_talla);
        }
        cursor.close();
        return nombre_talla;
    }

    /**
     * Obtiene el nombre del tipo de producto de la BD a partir de su ID.
     *
     * @param id_tipo ID del tipo.
     * @return Nombre del tipo de producto.
     */
    private String obtenerNombreTipo(String id_tipo) {
        //Log.i(TAG, "Buscando tipo con id: "+id_tipo);
        String nombre_tipo = null;
        Cursor cursor      = manager.buscarTipo_ID(id_tipo);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            nombre_tipo = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Tipo encontrado: "+nombre_tipo);
        }
        cursor.close();
        return nombre_tipo;
    }

    public void shareButtonPressed(String s, String s1, String s2) {
        new compartirViaWhatsApp().execute(s, s1, s2);
    }

    /**
     * Clase para la carga en 2do plano de los datos de la tabla (Solo 1era Ejecucion)
     */
    private class cargarDatos extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(ConsultarProductos.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            loadSpinnerData();
            loadData(null, null, null, null, false);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
        }
    }

    /**
     * Clase para la carga en 2do plano de los datos de la tabla (Con Filtros)
     */
    private class reCargarDatos extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(ConsultarProductos.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            //CANT_DATOS_MOSTRAR_INICIALMENTE = CANT_DATOS_ACTUALES; // Cant.datos que tengo actualmente..
            loadData(params[0], params[1], params[2], params[3], true);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
        }
    }

    /**
     * Clase para compartir Via WhatsApp en 2do plano
     */
    public class compartirViaWhatsApp extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(ConsultarProductos.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando imagen para compartirla...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(final String... params) {
            final Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            //share directly to WhatsApp and bypass the system picker
            sendIntent.setPackage("com.whatsapp");

            // modelo_nombre, color_producto, numeracion_producto

            final File archivo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                archivo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/" + params[0] + Constantes.EXTENSION_IMG);
            }
            else {
                archivo = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/" + params[0] + Constantes.EXTENSION_IMG);
            }

            try {
                if (archivo.exists()) {
                    Log.d(TAG, "Compartiendo imagen " + archivo.getAbsolutePath());
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                    Bitmap bitmapImage = Funciones.decodeSampledBitmapFromResource(archivo, 2160, 1620);
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                    String path = MediaStore.Images.Media.insertImage(ConsultarProductos.this.contexto.getContentResolver(),
                            bitmapImage, "Descripcion", null);

                    final Uri imageUri2 = Uri.parse(path);

                    final Thread hilo = new Thread() {
                        @Override
                        public void run() {
                            synchronized (this) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final String[]          opciones       = getResources().getStringArray(R.array.extrasViaWhatsApp);
                                        final ArrayList<String> mSelectedItems = new ArrayList<>();  // Where we track the selected items
                                        final ArrayList<String> datos          = new ArrayList<>();  // Where we track the selected items

                                        AlertDialog.Builder builder = new AlertDialog.Builder(ConsultarProductos.this);
                                        builder.setTitle(R.string.dialog_compartir_whatsapp_extra)
                                                // Specify the list array, the items to be selected by default (null for none),
                                                // and the listener through which to receive callbacks when items are selected
                                                .setMultiChoiceItems(R.array.extrasViaWhatsApp, null, new DialogInterface.OnMultiChoiceClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                        if (isChecked) {
                                                            // If the user checked the item, add it to the selected items
                                                            mSelectedItems.add(opciones[which]);

                                                            switch (opciones[which]) {
                                                                case "Modelo": {
                                                                    datos.add(params[0]);
                                                                    Log.d(TAG, "Modelo agregado " + params[0]);
                                                                }
                                                                break;
                                                                case "Color": {
                                                                    datos.add(params[1]);
                                                                    Log.d(TAG, "Color agregado " + params[1]);
                                                                }
                                                                break;
                                                                case "Numeracion": {
                                                                    datos.add(params[2]);
                                                                    Log.d(TAG, "Numeracion agregada " + params[2]);
                                                                }
                                                                break;
                                                            }

                                                            Log.d(TAG, "Seleccionaste " + opciones[which]);
                                                        }
                                                        else if (mSelectedItems.contains(opciones[which])) {
                                                            int pos = mSelectedItems.indexOf(opciones[which]);
                                                            Log.d(TAG, "Eliminaste " + opciones[which] + ": " + datos.get(pos));
                                                            mSelectedItems.remove(opciones[which]);
                                                            datos.remove(pos);
                                                        }
                                                    }
                                                })
                                                .setPositiveButton("Compartir", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        if (!mSelectedItems.isEmpty()) {
                                                            String txt_whatsapp = generarTextoWhatsAppCompartir(datos, mSelectedItems);
                                                            sendIntent.putExtra(Intent.EXTRA_TEXT, txt_whatsapp);
                                                            sendIntent.setType("text/plain");
                                                        }

                                                        Log.d(TAG, "Enviando al whatsApp");
                                                        dialog.cancel();

                                                        sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri2);
                                                        sendIntent.setType("image/*");
                                                        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                        Log.d(TAG, "Starting activity ");
                                                        startActivity(sendIntent);
                                                    }
                                                })
                                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        Log.d(TAG, "Operacion cancelada");
                                                        dialog.cancel();
                                                    }
                                                });

                                        builder.show();
                                    }
                                });
                            }
                        }
                    };
                    hilo.start();
                }
                else {
                    Toast.makeText(ConsultarProductos.this.contexto, "Ha ocurrido un error compartiendo la imagen.", Toast.LENGTH_LONG).show();
                }
            }
            catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(ConsultarProductos.this.contexto, "Whatsapp no esta instalado.", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
        }

        /**
         * Genera el texto que se compartira con la imagen dentro del whatsapp.
         *
         * @param datos              Lista con los datos a compartir.
         * @param itemsSeleccionados Lista con los elementos que se van a compartir.
         * @return Cadena de texto que se va a compartir via WhatsApp
         */
        private String generarTextoWhatsAppCompartir(ArrayList<String> datos, ArrayList<String> itemsSeleccionados) {
            String texto = "";

            final String[] opciones   = getResources().getStringArray(R.array.extrasViaWhatsApp);
            List<String>   stringList = new ArrayList<>(Arrays.asList(opciones));

            if (itemsSeleccionados.contains("Numeracion")) {
                int pos   = itemsSeleccionados.indexOf("Numeracion");
                int start = Funciones.buscarCaracter(datos.get(pos), '(');
                int end   = Funciones.buscarCaracter(datos.get(pos), ')');
                datos.set(pos, datos.get(pos).substring(start + 1, end));
            }

            for (int i = 0; i < itemsSeleccionados.size(); i++) {
                if (stringList.contains(itemsSeleccionados.get(i))) {
                    if (texto.equals(""))
                        texto = itemsSeleccionados.get(i) + ": " + datos.get(i);
                    else
                        texto += ", " + itemsSeleccionados.get(i) + ": " + datos.get(i);
                }
            }

            return texto;
        }
    }
}