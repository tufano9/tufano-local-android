package com.tufano.tufanomovil.gestion.productos;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.Constantes;
import com.tufano.tufanomovil.global.Funciones;
import com.tufano.tufanomovil.global.zoomImages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creado por Gerson el 13/01/2016.
 */
public class EditarProducto extends AppCompatActivity
{
    private String usuario;
    private Context contexto;
    private final String TAG = "EditarProducto";
    private ProgressDialog pDialog;
    private DBAdapter manager;
    public static Activity fa;
    private Spinner tipo, talla, color;
    private AutoCompleteTextView modelo_autoComplete;
    private LinearLayout layout;
    private boolean primerInicio1 = true, primerInicio2 = true, primerInicio3 = true;
    private ArrayList<View> filas;
    private List<List<String>> contenedor_colores, contenedor_tipos, contenedor_tallas;
    private TextView cabecera_1, cabecera_2, cabecera_3, cabecera_4, cabecera_5, cabecera_6;
    private String columna_ordenada, orden;
    private String tipo_filtrado, color_filtrado, talla_filtrado, modelo_filtrado;
    private final boolean DEBUG_MODE = false;
    private final int MAX_TABLE_RECORDS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_productos);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);
        fa = this;

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
        toolbar.setSubtitle(R.string.editar_producto_subtitulo);
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
     * Inicializando la cabecera de la tabla
     */
    private void initTextViewHeader()
    {
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
        cabecera_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
     * Funcion encargada de la gestion del click sobre las cabeceras.
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
            limpiarHeaders(cabeceras);
            colocarCompoundDrawable(cabecera);
            ordenarTabla(columnaBD, "ASC");
        }
    }

    /**
     * Metodo encargado de colocar el drawable sobre la cabecera.
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
     * @param cabecera Cabecera bajo la cual se ordenara.
     * @param orden Orden a utilizar (ASC o DESC)
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
     * Metodo utilizado cuando se presiona sobre una cabecera que esta actualmente presionada bajo
     * un cierto orden, dicho de otra forma, si se esta ordenando de forma ascendente un campo,
     * este metodo hara que se ordene de forma descendente.
     * @param cabecera La cabecera presionada.
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
     * Inicializador del autocompleteTextView
     */
    private void initAutoComplete()
    {
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
     * Inicializar Spinners de Estados.
     */
    private void initSpinners()
    {
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
     * Gestiona los parametros de filtrado de la tabla
     */
    private void gestionarFiltrado()
    {
        cerrarTeclado();
        String tipoFunction, tallaFunction, colorFunction, modeloFunction;
        String defaultValueTipo = tipo.getItemAtPosition(0).toString();
        String defaultValueTalla = talla.getItemAtPosition(0).toString();
        String defaultValueColor = color.getItemAtPosition(0).toString();

        // Si esta seleccionado la opcion por defecto, no filtrare con ese parametro
        if(tipo.getSelectedItem().toString().equals(defaultValueTipo))
        {
            tipoFunction = null;
        }
        else
        {
            int pos = tipo.getSelectedItemPosition();
            tipoFunction = contenedor_tipos.get(0).get(pos-1);
        }

        if(talla.getSelectedItem().toString().equals(defaultValueTalla))
        {
            tallaFunction = null;
        }
        else
        {
            int pos = talla.getSelectedItemPosition();
            tallaFunction = contenedor_tallas.get(0).get(pos - 1);
        }

        if(color.getSelectedItem().toString().equals(defaultValueColor))
        {
            colorFunction = null;
        }
        else
        {
            int pos = color.getSelectedItemPosition();
            colorFunction = contenedor_colores.get(0).get(pos - 1);
        }

        if(modelo_autoComplete.getText().toString().trim().equals(""))
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
     * @param tipo Tipo del producto para el filtrado. Por ej 'Torera'
     * @param talla Talla del producto para el filtrado.
     * @param color Color del producto para el filtrado.
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
     * Borra el contenido de la tabla
     */
    private void limpiarTabla()
    {
        Log.d(TAG, "Limpiando Tabla.. ("+filas.size()+" filas)");
        final TableLayout tabla = (TableLayout) findViewById(R.id.table_editar_productos);
        for (int i = 0; i < filas.size(); i++)
        {
            tabla.removeView(filas.get(i));
        }
    }

    /**
     * Obtiene una lista de todos los modelos existentes en la BD
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
            inputManager.hideSoftInputFromWindow( getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        else
            Log.w(TAG, "No se pudo ocultar el Teclado");
    }

    /**
     * Clase para la carga en 2do plano de los datos de la tabla (Solo 1era Ejecucion)
     */
    private class cargarDatos extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarProducto.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            loadSpinnerData();
            inicializarTabla(null, null, null, null, false);
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
        }
    }

    /**
     * Clase para la carga en 2do plano de los datos de la tabla (Con Filtros)
     */
    private class reCargarDatos extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarProducto.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            inicializarTabla(params[0], params[1], params[2], params[3], true);
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
        }

    }

    /**
     * Clase para compartir Via WhatsApp en 2do plano
     */
    private class compartirViaWhatsApp extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarProducto.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando imagen para compartirla...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(final String... params)
        {
            final Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            //share directly to WhatsApp and bypass the system picker
            sendIntent.setPackage("com.whatsapp");

            // modelo_nombre, color_producto, numeracion_producto

            final File archivo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
            {
                archivo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/" + params[0] + Constantes.EXTENSION_IMG);
            }
            else
            {
                archivo = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/" + params[0] + Constantes.EXTENSION_IMG);
            }

            try
            {
                if (archivo.exists())
                {
                    Log.d(TAG, "Compartiendo imagen " + archivo.getAbsolutePath());
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                    Bitmap bitmapImage = Funciones.decodeSampledBitmapFromResource(archivo, 2160, 1620);
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                    String path = MediaStore.Images.Media.insertImage(EditarProducto.this.contexto.getContentResolver(),
                            bitmapImage, "Descripcion", null);

                    final Uri imageUri2 = Uri.parse(path);

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
                                        final String[] opciones = getResources().getStringArray(R.array.extrasViaWhatsApp);
                                        final ArrayList<String> mSelectedItems = new ArrayList<>();  // Where we track the selected items
                                        final ArrayList<String> datos = new ArrayList<>();  // Where we track the selected items

                                        AlertDialog.Builder builder = new AlertDialog.Builder(EditarProducto.this);
                                        builder.setTitle(R.string.dialog_compartir_whatsapp_extra)
                                                // Specify the list array, the items to be selected by default (null for none),
                                                // and the listener through which to receive callbacks when items are selected
                                                .setMultiChoiceItems(R.array.extrasViaWhatsApp, null, new DialogInterface.OnMultiChoiceClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                                                    {
                                                        if (isChecked)
                                                        {
                                                            // If the user checked the item, add it to the selected items
                                                            mSelectedItems.add(opciones[which]);

                                                            switch (opciones[which])
                                                            {
                                                                case "Modelo":
                                                                {
                                                                    datos.add(params[0]);
                                                                    Log.d(TAG, "Modelo agregado " + params[0]);
                                                                }
                                                                break;
                                                                case "Color":
                                                                {
                                                                    datos.add(params[1]);
                                                                    Log.d(TAG, "Color agregado " + params[1]);
                                                                }
                                                                break;
                                                                case "Numeracion":
                                                                {
                                                                    datos.add(params[2]);
                                                                    Log.d(TAG, "Numeracion agregada " + params[2]);
                                                                }
                                                                break;
                                                            }

                                                            Log.d(TAG, "Seleccionaste " + opciones[which]);
                                                        }
                                                        else if (mSelectedItems.contains(opciones[which]))
                                                        {
                                                            int pos = mSelectedItems.indexOf(opciones[which]);
                                                            Log.d(TAG, "Eliminaste " + opciones[which] + ": " + datos.get(pos));
                                                            mSelectedItems.remove(opciones[which]);
                                                            datos.remove(pos);
                                                        }
                                                    }
                                                })
                                                .setPositiveButton("Compartir", new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id)
                                                    {
                                                        if(!mSelectedItems.isEmpty())
                                                        {
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
                                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener()
                                                {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id)
                                                    {
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
                else
                {
                    Toast.makeText(EditarProducto.this.contexto, "Ha ocurrido un error compartiendo la imagen.", Toast.LENGTH_LONG).show();
                }
            }
            catch (android.content.ActivityNotFoundException ex)
            {
                Toast.makeText(EditarProducto.this.contexto, "Whatsapp no esta instalado.", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
        }
    }

    /**
     * Genera el texto que se compartira con la imagen dentro del whatsapp.
     * @param datos Lista con los datos a compartir.
     * @param itemsSeleccionados Lista con los elementos que se van a compartir.
     * @return Cadena de texto que se va a compartir via WhatsApp
     */
    private String generarTextoWhatsAppCompartir(ArrayList<String> datos, ArrayList<String> itemsSeleccionados)
    {
        String texto = "";

        final String[] opciones = getResources().getStringArray(R.array.extrasViaWhatsApp);
        List<String> stringList = new ArrayList<>(Arrays.asList(opciones));

        if(itemsSeleccionados.contains("Numeracion"))
        {
            int pos = itemsSeleccionados.indexOf("Numeracion");
            int start = Funciones.buscarCaracter(datos.get(pos), '(');
            int end = Funciones.buscarCaracter(datos.get(pos), ')');
            datos.set(pos, datos.get(pos).substring(start + 1, end));
        }

        for(int i = 0; i<itemsSeleccionados.size(); i++)
        {
            if( stringList.contains(itemsSeleccionados.get(i)) )
            {
                if(texto.equals(""))
                    texto = itemsSeleccionados.get(i) + ": " + datos.get(i);
                else
                    texto += ", " + itemsSeleccionados.get(i) + ": " + datos.get(i);
            }
        }

        return texto;
    }

    /**
     * Function to load the spinner data from SQLite database
     * */
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

    /**
     * Metodo principal para inicializar la tabla y rellenarla con datos de la BD
     * @param tipo_filtrado Tipo del producto (Para el filtrado).
     * @param talla_filtrado Talla del producto (Para el filtrado).
     * @param color_filtrado Color del producto (Para el filtrado).
     * @param modelo_filtrado Modelo del producto (Para el filtrado).
     * @param filtrando True si se esta filtrando, False en caso contrario (1era Ejecucion)
     */
    private void inicializarTabla(String tipo_filtrado, String talla_filtrado, String color_filtrado, String modelo_filtrado, boolean filtrando)
    {
        Log.i(TAG, "Inicializando tabla.. Ordenando por: " + columna_ordenada + ", orden: " + orden);
        Log.i(TAG, "Filtrando por... Tipo: "+tipo_filtrado+", Talla: "+talla_filtrado+", Color: "+color_filtrado+", Modelo: "+modelo_filtrado);

        final TableLayout tabla = (TableLayout) findViewById(R.id.table_editar_productos);
        filas = new ArrayList<>();

        TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        int edit_image_width = 48;
        int edit_image_height = 48;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(edit_image_width, edit_image_height);

        // Llenando la tabla de forma iterativa
        Cursor cursor = manager.cargarProductos_Filtrado_Ordenado(tipo_filtrado, talla_filtrado, color_filtrado, modelo_filtrado, columna_ordenada, orden);

        if (cursor.getCount() > 0)
        {
            mostrarTodo(tabla);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                if(DEBUG_MODE && cursor.getPosition()>1)
                    break;
                Log.i(TAG, "Agregando fila..");
                final TableRow fila = new TableRow(contexto);

                final String id_producto = String.valueOf(cursor.getInt(0));
                final String id_talla = cursor.getString(1);
                final String id_tipo = cursor.getString(2);
                final String modelo_nombre = cursor.getString(3);
                final String talla_producto, tipo_producto, color_producto;
                final String modelo_producto = cursor.getString(3);
                final String id_color = cursor.getString(4);
                final String precio_producto = cursor.getString(5);
                final String numeracion_producto = cursor.getString(6);
                final String estatus_producto = cursor.getString(7);
                final String paresxtalla = cursor.getString(8);

                /* Imagen */
                ImageView imagen = generarImageViewTabla(modelo_producto, params, contexto);

                /* Talla */
                TextView talla = generarTalla(id_talla, params, contexto);
                talla_producto = obtenerNombreTalla(id_talla);

                /* Tipo */
                TextView tipo = generarTipo(id_tipo, params, contexto);
                tipo_producto = obtenerNombreTipo(id_tipo);

                /* Modelo */
                TextView modelo = generarModelo(modelo_nombre, params, contexto);

                /* Color */
                TextView color = generarColor(id_color, params, contexto);
                color_producto = obtenerNombreColor(id_color);

                /* Precio */
                TextView precio = generarPrecioFila(precio_producto, params, contexto);

                /* Numeracion */
                TextView numeracion = generarNumeracionFila(numeracion_producto, params, contexto);

                /* Opciones */
                ImageView editar = generarImageViewEditar(usuario, id_producto, talla_producto, tipo_producto, modelo_producto, color_producto, id_color, precio_producto, numeracion_producto, estatus_producto, paresxtalla, parms, contexto);
                ImageView btn_whatsapp = generarImageViewWhatsApp(modelo_nombre, color_producto, numeracion_producto, contexto, parms);

                LinearLayout opciones = new LinearLayout(contexto);
                opciones.setGravity(Gravity.CENTER);
                opciones.setLayoutParams(params);
                opciones.addView(editar);
                opciones.addView(btn_whatsapp);

                // Llenando la fila con data
                fila.setBackgroundColor(Color.WHITE);
                fila.addView(imagen);
                fila.addView(talla);
                fila.addView(tipo);
                fila.addView(modelo);
                fila.addView(color);
                fila.addView(precio);
                fila.addView(numeracion);
                fila.addView(opciones);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    fila.setBackground(Funciones.intToDrawable(contexto, R.drawable.table_border));
                else
                    //noinspection deprecation
                    fila.setBackgroundDrawable(Funciones.intToDrawable(contexto, R.drawable.table_border));

                if( cursor.getPosition() == MAX_TABLE_RECORDS)
                {
                    // Ya no cargo n la U.I el resto de la data, la almaceno para mostrarla luego.
                }

                filas.add(fila);

                final Thread hilo1 = new Thread() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    tabla.addView(fila);
                                }
                            });
                        }
                    }
                };
                hilo1.start();
            }
        }
        else
        {
            if(!filtrando)
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
                                    //Toast.makeText(contexto, "Pedido vacio, por favor agregue algun producto utilizando el boton inferior", Toast.LENGTH_LONG).show();
                                    ocultarTodo(tabla);

                                    TextView mensaje = new TextView(contexto);
                                    mensaje.setText(R.string.msj_producto_vacio);
                                    mensaje.setGravity(Gravity.CENTER);
                                    mensaje.setTextSize(20f);

                                    LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor);
                                    contenedor.addView(mensaje);
                                    // Pedido vacio, por favor agregue algun producto utilizando el boton inferior
                                }
                            });
                        }
                    }
                };
                hilo.start();
            }
            else
            {
                TableRow.LayoutParams parametros = new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

                // Con esto hago que el mensaje ocupe toda la fila completa, sin alargar la cabecera
                // que se alarga automaticamente debido al stretchColumns del tableLayout
                TableRow cabecera = (TableRow) findViewById(R.id.cabecera);
                parametros.span = cabecera.getChildCount();

                TextView mensaje = new TextView(contexto);
                mensaje.setText(R.string.sin_resultados);
                mensaje.setGravity(Gravity.CENTER);
                mensaje.setTextSize(20f);
                mensaje.setLayoutParams(parametros);

                final TableRow fila = new TableRow(contexto);
                fila.addView(mensaje);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    fila.setBackground(Funciones.intToDrawable(contexto, R.drawable.table_border));
                else
                    //noinspection deprecation
                    fila.setBackgroundDrawable(Funciones.intToDrawable(contexto, R.drawable.table_border));

                filas.add(fila);

                final Thread hilo1 = new Thread() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    tabla.addView(fila);
                                }
                            });
                        }
                    }
                };
                hilo1.start();
            }
        }

        cursor.close();
    }

    /**
     * Metodo que genera el imageView utilizado para editar un producto seleccionado.
     * @param usuario El ID del usuario que va a editar.
     * @param id_producto ID del producto a ser editado.
     * @param talla_producto Talla del producto a ser editado.
     * @param tipo_producto Tipo del producto a ser editado.
     * @param modelo_producto Modelo del producto a ser editado.
     * @param color_producto Color del producto a ser editado.
     * @param id_color ID del color del producto del producto a ser editado.
     * @param precio_producto Precio del producto a ser editado.
     * @param numeracion_producto Numeracion del producto a ser editado.
     * @param estatus_producto Estatus del producto a ser editado.
     * @param paresxtalla Cantidad de pares por talla del producto a ser editado.
     * @param parms Parametros de la tabla.
     * @param contexto Contexto de la aplicacion
     * @return ImageView para la edicion del producto una vez sea este clickeado.
     */
    private ImageView generarImageViewEditar(final String usuario, final String id_producto, final String talla_producto, final String tipo_producto, final String modelo_producto, final String color_producto, final String id_color, final String precio_producto, final String numeracion_producto, final String estatus_producto, final String paresxtalla, LinearLayout.LayoutParams parms, Context contexto)
    {
        ImageView editar = new ImageView(contexto);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent c = new Intent(EditarProducto.this, EditarProductoDetalles.class);
                c.putExtra("usuario", usuario);
                c.putExtra("id_producto", id_producto);
                c.putExtra("talla_producto", talla_producto);
                c.putExtra("tipo_producto", tipo_producto);
                c.putExtra("modelo_producto", modelo_producto);
                c.putExtra("color_producto", color_producto);
                c.putExtra("id_color", id_color);
                c.putExtra("precio_producto", precio_producto);
                c.putExtra("numeracion_producto", numeracion_producto);
                c.putExtra("estatus_producto", estatus_producto);
                c.putExtra("paresxtalla", paresxtalla);
                startActivity(c);
            }
        });

        editar.setBackgroundResource(R.drawable.ic_edit);
        editar.setLayoutParams(parms);
        editar.setPadding(2, 10, 2, 10);

        return editar;
    }

    /**
     * Genera un imageView el cual contendra el enlace para compartir via WhatsApp
     * @param modelo_nombre Nombre del modelo del producto.
     * @param color_producto Color del producto.
     * @param numeracion_producto Numeracion del producto.
     * @param contexto Contexto de la aplicacion.
     * @param parms Parametros de la tabla.
     * @return ImageView armado con el enlace para compartir via WhatsApp
     */
    private ImageView generarImageViewWhatsApp(final String modelo_nombre, final String color_producto, final String numeracion_producto, Context contexto, LinearLayout.LayoutParams parms)
    {
        final ImageView btn_whatsapp = new ImageView(contexto);
        btn_whatsapp.setBackgroundResource(R.drawable.icn_whatsapp);
        btn_whatsapp.setLayoutParams(parms);
        btn_whatsapp.setPadding(2, 10, 2, 10);

        btn_whatsapp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new compartirViaWhatsApp().execute(modelo_nombre, color_producto, numeracion_producto);
            }
        });

        return btn_whatsapp;
    }

    /**
     * Genera un textView para la numeracion del producto.
     * @param numeracion_producto Numeracion del producto.
     * @param params Parametros de la tabla.
     * @param contexto Contexto de la aplicacion.
     * @return TextView que contiene la numeracion del producto.
     */
    private TextView generarNumeracionFila(String numeracion_producto, TableRow.LayoutParams params, Context contexto)
    {
        TextView numeracion = new TextView(contexto);

        int start = Funciones.buscarCaracter(numeracion_producto, '(');
        int end = Funciones.buscarCaracter(numeracion_producto, ')');

        numeracion_producto = numeracion_producto.substring(start + 1, end);

        numeracion.setText(numeracion_producto);
        numeracion.setTextColor(Color.DKGRAY);
        numeracion.setGravity(Gravity.CENTER);
        numeracion.setLayoutParams(params);
        numeracion.setTextSize(16f);
        return numeracion;
    }

    /**
     * Genera un textView que contiene el precio del producto.
     * @param precio_producto Precio del producto.
     * @param params Parametros de la tabla.
     * @param contexto Contexto de la aplicacion.
     * @return TextView preparado con el precio del producto.
     */
    private TextView generarPrecioFila(String precio_producto, TableRow.LayoutParams params, Context contexto)
    {
        TextView precio = new TextView(contexto);
        DecimalFormat priceFormat = new DecimalFormat("###,###.##");
        String output = priceFormat.format(Double.parseDouble(precio_producto));
        precio.setText(output);
        precio.setTextColor(Color.DKGRAY);
        precio.setGravity(Gravity.CENTER);
        precio.setLayoutParams(params);
        precio.setTextSize(16f);
        return precio;
    }

    /**
     * Genera el textView que contiene el color del producto.
     * @param id_color ID del color.
     * @param params Parametros de la tabla.
     * @param contexto Contexto de la aplicacion.
     * @return TextView preparado con el color del producto.
     */
    private TextView generarColor(String id_color, TableRow.LayoutParams params, Context contexto)
    {
        TextView color = new TextView(contexto);
        String color_producto = obtenerNombreColor(id_color);
        color.setText(color_producto);
        color.setTextColor(Color.DKGRAY);
        color.setGravity(Gravity.CENTER);
        color.setLayoutParams(params);
        color.setTextSize(16f);
        return color;
    }

    /**
     * Genera un textView para mostrar el modelo del producto.
     * @param modelo_nombre Nombre del modelo del producto.
     * @param params Parametros de la tabla.
     * @param contexto Contexto
     * @return TextView generado con el modelo del producto.
     */
    private TextView generarModelo(String modelo_nombre, TableRow.LayoutParams params, Context contexto)
    {
        TextView modelo = new TextView(contexto);
        modelo.setText(modelo_nombre);
        modelo.setTextColor(Color.DKGRAY);
        modelo.setGravity(Gravity.CENTER);
        modelo.setLayoutParams(params);
        modelo.setTextSize(16f);
        return modelo;
    }

    /**
     * Genera un textView para mostrar el tipo del producto.
     * @param id_tipo ID del tipo del producto.
     * @param params Parametros de la tabla.
     * @param contexto Contexto de la aplicacion.
     * @return TextView generado con el tipo del producto.
     */
    private TextView generarTipo(String id_tipo, TableRow.LayoutParams params, Context contexto)
    {
        TextView tipo = new TextView(contexto);
        String tipo_producto = obtenerNombreTipo(id_tipo);
        tipo.setText(tipo_producto);
        tipo.setTextColor(Color.DKGRAY);
        tipo.setGravity(Gravity.CENTER);
        tipo.setLayoutParams(params);
        tipo.setTextSize(16f);

        return tipo;
    }

    /**
     * Genera un textView para mostrar la talla del producto.
     * @param id_talla ID de la talla.
     * @param params Parametros de la tabla.
     * @param contexto Contexto.
     * @return TextView generado con la talla.
     */
    private TextView generarTalla(String id_talla, TableRow.LayoutParams params, Context contexto)
    {
        TextView talla = new TextView(contexto);
        String talla_producto = obtenerNombreTalla(id_talla);
        talla.setText(talla_producto);
        talla.setTextColor(Color.DKGRAY);
        talla.setGravity(Gravity.CENTER);
        talla.setLayoutParams(params);
        talla.setTextSize(16f);
        return talla;
    }

    /**
     * Genera un imageView que contiene la imagen del producto.
     * @param modelo Modelo del producto.
     * @param params Parametros de la tabla.
     * @param contexto Contexto de la aplicacion.
     * @return ImageView con la imagen del producto.
     */
    private ImageView generarImageViewTabla(String modelo, final TableRow.LayoutParams params, Context contexto)
    {
        final ImageView imagen = new ImageView(contexto);
        final File file;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
        {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/" + modelo + Constantes.EXTENSION_IMG);
        }
        else
        {
            file = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/" + modelo + Constantes.EXTENSION_IMG);
        }

        if (file.exists())
        {
            imagen.setImageBitmap(Funciones.decodeSampledBitmapFromResource(file, 130, 100));
            imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imagen.setLayoutParams(params);
            imagen.setPadding(2, 10, 2, 10);

            imagen.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ImageView expandedImageView = (ImageView) findViewById(R.id.expanded_image);
                    View container = findViewById(R.id.contenedor_base);
                    ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
                    zoomImages.zoomImageFromThumb(imagen, file.getAbsolutePath(), getResources(),
                            expandedImageView, container, scroll);

                    /*Intent c = new Intent(EditarProducto.this, FullScreenImage.class);
                    c.putExtra("img_path",file.getAbsolutePath());
                    startActivity(c);*/
                }
            });
        }
        else {
            Log.e(TAG, "La imagen no pudo ser localizada..");
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.img_notfound);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 130, 100, true));
            imagen.setImageDrawable(d);
            imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imagen.setLayoutParams(params);
            imagen.setPadding(2, 2, 2, 2);
        }

        return imagen;
    }

    /**
     * Funcion encargada de mostrar el contenido relacionado a la tabla.
     * @param tabla Layout de la tabla a mostrar.
     */
    private void mostrarTodo(TableLayout tabla)
    {
        AutoCompleteTextView model = (AutoCompleteTextView) findViewById(R.id.autoC_modelo);
        Spinner tipo = (Spinner) findViewById(R.id.spTipo_editar_producto);
        Spinner color = (Spinner) findViewById(R.id.spColor_editar_producto);
        Spinner talla = (Spinner) findViewById(R.id.spTalla_editar_producto);

        model.setVisibility(View.VISIBLE);
        tabla.setVisibility(View.VISIBLE);
        tipo.setVisibility(View.VISIBLE);
        color.setVisibility(View.VISIBLE);
        talla.setVisibility(View.VISIBLE);
    }

    /**
     * Funcion encargada de ocultar el contenido relacionado a la tabla, para mostrar mensajes de
     * error.
     * @param tabla Layout de la tabla a ocultar.
     */
    private void ocultarTodo(TableLayout tabla)
    {
        AutoCompleteTextView model = (AutoCompleteTextView) findViewById(R.id.autoC_modelo);
        Spinner tipo = (Spinner) findViewById(R.id.spTipo_editar_producto);
        Spinner color = (Spinner) findViewById(R.id.spColor_editar_producto);
        Spinner talla = (Spinner) findViewById(R.id.spTalla_editar_producto);

        model.setVisibility(View.INVISIBLE);
        tabla.setVisibility(View.INVISIBLE);
        tipo.setVisibility(View.INVISIBLE);
        color.setVisibility(View.INVISIBLE);
        talla.setVisibility(View.INVISIBLE);
    }

    /**
     * Obtiene el nombre del color de la base de datos, a partir de su ID.
     * @param id_color ID del color a consultar.
     * @return Nombre del color.
     */
    private String obtenerNombreColor(String id_color)
    {
        //Log.i(TAG, "Buscando color con id: "+id_color);
        String nombre_color = null;
        Cursor cursor = manager.buscarColor_ID(id_color);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            nombre_color = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Color encontrado: "+nombre_color);
        }
        cursor.close();
        return nombre_color;
    }

    /**
     * Obtiene el nombre de la talla del producto de la BD a partir de su ID.
     * @param id_talla ID de la talla.
     * @return Nombre de la talla del producto.
     */
    private String obtenerNombreTalla(String id_talla)
    {
        //Log.i(TAG, "Buscando talla con id: "+id_talla);
        String nombre_talla = null;
        Cursor cursor = manager.buscarTalla_ID(id_talla);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            nombre_talla = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Talla encontrado: "+nombre_talla);
        }
        cursor.close();
        return nombre_talla;
    }

    /**
     * Obtiene el nombre del tipo de producto de la BD a partir de su ID.
     * @param id_tipo ID del tipo.
     * @return Nombre del tipo de producto.
     */
    private String obtenerNombreTipo(String id_tipo)
    {
        //Log.i(TAG, "Buscando tipo con id: "+id_tipo);
        String nombre_tipo = null;
        Cursor cursor = manager.buscarTipo_ID(id_tipo);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            nombre_tipo = String.valueOf(cursor.getString(0));
            //Log.i(TAG, "Tipo encontrado: "+nombre_tipo);
        }
        cursor.close();
        return nombre_tipo;
    }

    /*
    class async_eliminarProductoBD extends AsyncTask< String, String, String >
    {
        String id, nombre;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarProducto.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Eliminando el producto...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            id = params[0];
            nombre = params[1];

            try
            {
                //enviamos y recibimos y analizamos los datos en segundo plano.
                if (eliminarProducto(id, nombre))
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
                Toast.makeText(contexto, "Producto eliminado exitosamente!!", Toast.LENGTH_LONG).show();

                // Redirige a la pantalla de Home
                Intent c = new Intent(EditarProducto.this, EditarProducto.class);
                c.putExtra("usuario",usuario);
                startActivity(c);

                // Prevent the user to go back to this activity
                finish();
            }
            else
            {
                Toast.makeText(contexto, "Hubo un error eliminando el producto..", Toast.LENGTH_LONG).show();
            }
        }

        private boolean eliminarProducto(String id, String nombre)
        {
            long id_producto = manager.eliminarProducto(id);
            Log.d(TAG, "ID_producto: " + id_producto);
            // Si el producto fue eliminado exitosamente en BD, proceso a eliminar la imagen..
            return id_producto != -1 && eliminarImagen(nombre);
        }

        private boolean eliminarImagen(String nombre)
        {
            Log.i(TAG, "Se eliminara una imagen con el nombre de: " + nombre);
            File imagesFolder;

            if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                {
                    imagesFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/" + nombre + Constantes.EXTENSION_IMG);
                }
                else
                {
                    imagesFolder = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/" + nombre + Constantes.EXTENSION_IMG);
                }
                if(imagesFolder.exists())
                {
                    if (imagesFolder.delete())
                    {
                        Log.i(TAG, "File successfully deleted");
                    }
                    else
                    {
                        Log.e(TAG, "File could not be deleted");
                    }
                }

                return true;
            }

            return false;
        }
    }
    */

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            //mostrarConfiguracion();
            return true;
        }
        else if (id == R.id.profile_settings)
        {
            //mostrarPerfil();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}