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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.Constantes;
import com.tufano.tufanomovil.global.Funciones;
import com.tufano.tufanomovil.global.FuncionesTablas;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created por Usuario Tufano on 20/01/2016.
 */
public class AgregarProductoPedidoEditar extends AppCompatActivity
{
    private String usuario, id_pedido;
    private Context contexto;
    private final String TAG = "AgregarProductoPedido";
    private ProgressDialog pDialog;
    private DBAdapter manager;
    private int productos_agregados;
    private ArrayList<ArrayList<String>> productos;
    private static final int IMG_WIDTH = 130;
    private static final int IMG_HEIGHT = 50;
    private static final ImageView.ScaleType ESCALADO = ImageView.ScaleType.CENTER_INSIDE;
    private Spinner tipo, talla, color;
    private AutoCompleteTextView modelo_autoComplete;
    private LinearLayout layout;
    private boolean primerInicio1 = true, primerInicio2 = true, primerInicio3 = true;
    private ArrayList<View> filas;
    private List<List<String>> contenedor_colores, contenedor_tipos, contenedor_tallas;
    private TextView cabecera_1, cabecera_2, cabecera_3, cabecera_4, cabecera_5, cabecera_6;
    private String columna_ordenada, orden;
    private String tipo_filtrado, color_filtrado, talla_filtrado, modelo_filtrado;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_productos_pedido_editar);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);
        productos = new ArrayList<>();

        getExtrasVar();
        createToolBar();
        noInitialFocus();
        initButtons();
        initSpinners();
        initAutoComplete();
        initTextViewHeader();

        columna_ordenada = "talla";
        orden = "ASC";

        new cargarDatos().execute();
    }

    /**
     * Inicializar botones.
     */
    private void initButtons()
    {
        Button btn_agregar_productos_pedido = (Button) findViewById(R.id.btn_agregar_productos_pedido);
        btn_agregar_productos_pedido.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(productos_agregados>0)
                {
                    // Agregar producto
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AgregarProductoPedidoEditar.this);

                    dialog.setTitle(R.string.confirmacion);
                    dialog.setMessage(R.string.confirmacion_agregar_producto_pedido);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new agregarProductoPedido().execute();
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
                    Toast.makeText(contexto, "Debe agregar al menos un producto para continuar!",
                            Toast.LENGTH_LONG).show();
                }
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
        id_pedido = bundle.getString("id_pedido");
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
     * Evita el focus principal al abrir la activity, el cual despliega automaticamente el teclado
     */
    private void noInitialFocus()
    {
        layout = (LinearLayout) findViewById(R.id.LinearLayout_MainActivity);
        layout.requestFocus();
    }

    /**
     * Inicializacion del AutoComplete
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
                // Si presiono la tecla enter.
                if (keyCode == 66)
                {
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
     * Inicializacion de los spinners
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
     * Inicializacion de los Headers de la tabla
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
     * Funcion encargada de gestionar la accion a realizar a presionar sobre una cabecera.
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
     * Coloca el drawable en la cabecera que se presiono.
     * @param cabecera Cabecera a la cual se le aplicara el drawable.
     */
    private void colocarCompoundDrawable(TextView cabecera)
    {
        Log.d(TAG, "Colocando..");
        int drawable = Funciones.getAscDrawable();
        cabecera.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);
        cabecera.setTag("ASC");
    }

    /**
     * Funcion para ordenar la tabla segun una fila y un orden determinado.
     * @param cabecera Fila de la cabecera bajo la cual se aplicara el orden.
     * @param orden_tabla Sentido de orden de la tabla, ASC o DESC
     */
    private void ordenarTabla(String cabecera, String orden_tabla)
    {
        columna_ordenada = cabecera;
        orden = orden_tabla;
        limpiarTabla();
        new reCargarDatos().execute(tipo_filtrado, talla_filtrado, color_filtrado, modelo_filtrado,
                cabecera, orden_tabla);
    }

    /**
     * Invierte el drawable si este ya fue seleccionado previamente, en resumen, si esta en modo
     * ASC, cambia a modo DESC.
     * @param cabecera Cabecera a la cual se le invertira el drawable
     * @param columnaBD Columna de la Base de datos que sera ordenada.
     */
    private void invertirCompoundDrawable(TextView cabecera, String columnaBD)
    {
        Log.d(TAG, "Invirtiendo..");
        Drawable[] d = cabecera.getCompoundDrawables();

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
     * Borra los drawables de las cabeceras de la tabla.
     * @param cabeceras Conjunto de cabeceras de la tabla.
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
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData()
    {
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
     * Funcion encargada de obtener todos los modelos de la base de datos.
     * @return Lista de los modelos existentes en BD.
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
     * Funcion que gestiona el procedimiento del filtrado
     */
    private void gestionarFiltrado()
    {
        cerrarTeclado();
        String tipoFunction, tallaFunction, colorFunction, modeloFunction;
        String defaultValueTipo  =  tipo.getItemAtPosition(0).toString();
        String defaultValueTalla = talla.getItemAtPosition(0).toString();
        String defaultValueColor = color.getItemAtPosition(0).toString();

        // Si el valor seleccionado es el por defecto, no se filtrara por ese campo.
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

        // Actualizando variables globales
        tipo_filtrado = tipoFunction;
        talla_filtrado = tallaFunction;
        color_filtrado = colorFunction;
        modelo_filtrado = modeloFunction;

        filtrarTabla();
    }

    /**
     * Funcion para preparar el filtrado de los datos.
     */
    private void filtrarTabla()
    {
        Log.w(TAG, "Filtrando.. Tipo: " + tipo_filtrado + ", Color: " + color_filtrado + ", Talla: " + talla_filtrado + ", Modelo: " + modelo_filtrado);
        limpiarTabla();
        new reCargarDatos().execute(tipo_filtrado, talla_filtrado, color_filtrado, modelo_filtrado);
    }

    /**
     * Funcion para limpiar la tabla con su contenido
     */
    private void limpiarTabla()
    {
        final TableLayout tabla = (TableLayout) findViewById(R.id.tabla_contenido);
        for (int i = 0; i < filas.size(); i++)
        {
            tabla.removeView(filas.get(i));
        }
    }

    /**
     * Funcion para cerrar el teclado del dispositivo
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

    private class cargarDatos extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(AgregarProductoPedidoEditar.this);
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

    private class reCargarDatos extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(AgregarProductoPedidoEditar.this);
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
     * Funcion principal para la inicializacion de la tabla
     * @param tipo_filtrado Tipo del producto
     * @param talla_filtrado Talla del producto
     * @param color_filtrado Color del producto
     * @param modelo_filtrado Modelo del producto
     * @param filtrando True si esta realizando un filtrado, false de lo contrario (Primera Ejecucion)
     */
    private void inicializarTabla(String tipo_filtrado, String talla_filtrado, String color_filtrado, String modelo_filtrado, boolean filtrando)
    {
        Log.i(TAG, "Inicializando tabla.. Ordenando por: "+columna_ordenada+", orden: "+orden);
        Log.i(TAG, "Filtrando por... Tipo: "+tipo_filtrado+", Talla: "+talla_filtrado+", Color: "+color_filtrado+", Modelo: "+modelo_filtrado);

        final TableLayout tabla = (TableLayout) findViewById(R.id.tabla_contenido);
        filas = new ArrayList<>();

        TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        ArrayList ids_productos_cargados = FuncionesTablas.idsCargados(manager);
        productos_agregados = 0;
        productos = new ArrayList<>();

        // Llenando la tabla de forma iterativa
        //Cursor cursor = manager.cargarProductos(tipo_filtrado, talla_filtrado, color_filtrado, modelo_filtrado);
        Cursor cursor = manager.cargarProductos_Filtrado_Ordenado(tipo_filtrado, talla_filtrado, color_filtrado, modelo_filtrado, columna_ordenada, orden);

        if (cursor.getCount() > 0)
        {
            mostrarTodo(tabla);
            int contador = 0 ;

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                if( !FuncionesTablas.estaAgregado(cursor.getString(3), ids_productos_cargados, TAG) && FuncionesTablas.productoHabilitado(cursor.getString(7), TAG) )
                {
                    Log.i(TAG, "Agregando fila..");
                    final TableRow fila = new TableRow(contexto);

                    //final String id_producto = String.valueOf(cursor.getInt(0));
                    final String talla_producto = FuncionesTablas.obtenerNombreTalla(cursor.getString(1), manager);
                    final String tipo_producto = FuncionesTablas.obtenerNombreTipo(cursor.getString(2), manager);
                    final String modelo_producto = cursor.getString(3);
                    final String id_color = cursor.getString(4);
                    final String color_producto = FuncionesTablas.obtenerNombreColor(id_color, manager);
                    final String precio_producto = cursor.getString(5);
                    final String numeracion_producto = cursor.getString(6);
                    //final String estatus_producto = cursor.getString(7);
                    final String paresxtalla = cursor.getString(8);
                    final String pares_totales = FuncionesTablas.calcularPares(paresxtalla);
                    final String bultos_totales = "1"; // Bultos por defecto a agregar..

                    /* Imagen */
                    ImageView imagen = new ImageView(contexto);

                    final File file;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                    {
                        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/"+modelo_producto + Constantes.EXTENSION_IMG);
                    }
                    else
                    {
                        file = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/"+modelo_producto + Constantes.EXTENSION_IMG);
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

                        /* Talla */
                    TextView talla = new TextView(contexto);
                    talla.setText(talla_producto);
                    talla.setTextColor(Color.DKGRAY);
                    talla.setGravity(Gravity.CENTER);
                    talla.setLayoutParams(params);
                    talla.setTextSize(16f);

                        /* Tipo */
                    TextView tipo = new TextView(contexto);
                    tipo.setText(tipo_producto);
                    tipo.setTextColor(Color.DKGRAY);
                    tipo.setGravity(Gravity.CENTER);
                    tipo.setLayoutParams(params);
                    tipo.setTextSize(16f);

                        /* Modelo */
                    TextView modelo = new TextView(contexto);
                    modelo.setText(modelo_producto);
                    modelo.setTextColor(Color.DKGRAY);
                    modelo.setGravity(Gravity.CENTER);
                    modelo.setLayoutParams(params);
                    modelo.setTextSize(16f);

                        /* Color */
                    TextView color = new TextView(contexto);
                    color.setText(color_producto);
                    color.setTextColor(Color.DKGRAY);
                    color.setGravity(Gravity.CENTER);
                    color.setLayoutParams(params);
                    color.setTextSize(16f);

                        /* Precio */
                    TextView precio = new TextView(contexto);
                    DecimalFormat priceFormat = new DecimalFormat("###,###.##");
                    String output = priceFormat.format(Double.parseDouble(precio_producto));
                    precio.setText(output);
                    precio.setTextColor(Color.DKGRAY);
                    precio.setGravity(Gravity.CENTER);
                    precio.setLayoutParams(params);
                    precio.setTextSize(16f);

                        /* Numeracion */
                    TextView numeracion = new TextView(contexto);
                    numeracion.setText( numeracion_producto .replace("(", "") .replace(")", "") );
                    numeracion.setTextColor(Color.DKGRAY);
                    numeracion.setGravity(Gravity.CENTER);
                    numeracion.setLayoutParams(params);
                    numeracion.setTextSize(16f);

                        /* Opciones */
                    final CheckBox cb_producto = new CheckBox(contexto);
                    cb_producto.setGravity(Gravity.CENTER);

                    TableRow.LayoutParams params2 = new TableRow.LayoutParams( TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT );
                    params2.gravity = Gravity.CENTER;
                    cb_producto.setLayoutParams(params2);

                    cb_producto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (cb_producto.isChecked()) {
                                productos_agregados++;

                                String subtotal = FuncionesTablas.calcularSubtotal(Integer.parseInt(pares_totales), Integer.parseInt(bultos_totales), Double.parseDouble(precio_producto));
                                ArrayList<String> producto = new ArrayList<>();
                                //producto.add(id_producto);
                                producto.add(tipo_producto);
                                producto.add(modelo_producto);
                                producto.add(color_producto);
                                producto.add(talla_producto);
                                producto.add(numeracion_producto.replace(")", ""));
                                producto.add(pares_totales);
                                producto.add(bultos_totales);
                                producto.add(precio_producto);
                                producto.add(subtotal);
                                productos.add(producto);
                                Log.d(TAG, "Producto agregado.. total: " + productos_agregados);
                            } else {
                                productos_agregados--;
                                Log.d(TAG, "Producto eliminado.. total: " + productos_agregados);
                            }
                        }
                    });

                    TableRow.LayoutParams layout = new TableRow.LayoutParams(0,TableRow.LayoutParams.MATCH_PARENT);
                    imagen.setLayoutParams(layout);
                    talla.setLayoutParams(layout);
                    tipo.setLayoutParams(layout);
                    modelo.setLayoutParams(layout);
                    color.setLayoutParams(layout);
                    precio.setLayoutParams(layout);
                    numeracion.setLayoutParams(layout);
                    //cb_producto.setLayoutParams(layout);



                    LinearLayout opciones = new LinearLayout(contexto);
                    opciones.setGravity(Gravity.CENTER);
                    opciones.setLayoutParams(layout);
                    opciones.addView(cb_producto);
                    //opciones.setBackgroundColor(Color.BLUE);

                        /*imagen.setBackgroundColor(Color.RED);
                        talla.setBackgroundColor(Color.BLUE);
                        tipo.setBackgroundColor(Color.RED);
                        modelo.setBackgroundColor(Color.BLUE);
                        color.setBackgroundColor(Color.RED);
                        precio.setBackgroundColor(Color.BLUE);
                        numeracion.setBackgroundColor(Color.RED);
                        cb_producto.setBackgroundColor(Color.RED);*/

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
                    fila.setPadding(0, 2, 0, 0);

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
                else
                {
                    contador++;
                }
            }

            if( contador == cursor.getCount())
            {
                if(!filtrando)
                {
                    Log.w(TAG, "VACIO!!!");
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
                                        ocultarTodo(tabla);

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                                        TextView mensaje = new TextView(contexto);
                                        mensaje.setText(R.string.msj_producto_nodisponible);
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
                                    ocultarTodo(tabla);

                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                                    TextView mensaje = new TextView(contexto);
                                    mensaje.setText(R.string.msj_producto_nodisponible);
                                    //mensaje.setText(R.string.msj_producto_vacio);
                                    mensaje.setGravity(Gravity.CENTER);
                                    mensaje.setTextSize(20f);
                                    mensaje.setLayoutParams(params);

                                    LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor);
                                    contenedor.removeAllViews();
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
     * Muestra todos los componentes de la tabla.
     * @param tabla Tabla a la cual se le haran visibles los componentes
     */
    private void mostrarTodo(TableLayout tabla)
    {
        tabla.setVisibility(View.VISIBLE);
        final TableLayout cabecera = (TableLayout) findViewById(R.id.table_agregar_productos_pedido);
        Button btn_agregar = (Button) findViewById(R.id.btn_agregar_productos_pedido);
        btn_agregar.setVisibility(View.VISIBLE);
        cabecera.setVisibility(View.VISIBLE);

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
     * Oculta todos los componentes de la tabla.
     * @param tabla Tabla a la cual se le haran invisibles los componentes
     */
    private void ocultarTodo(TableLayout tabla)
    {
        final TableLayout cabecera = (TableLayout) findViewById(R.id.table_agregar_productos_pedido);
        tabla.setVisibility(View.INVISIBLE);
        Button btn_agregar = (Button) findViewById(R.id.btn_agregar_productos_pedido);
        btn_agregar.setVisibility(View.INVISIBLE);
        cabecera.setVisibility(View.INVISIBLE);

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

    class agregarProductoPedido extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(AgregarProductoPedidoEditar.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Agregando productos al pedido...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            agregarProductos();
            return null;
        }

        private void agregarProductos() {
            manager.agregarProductoPedidoEditar(productos, id_pedido);
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
            Intent c = new Intent(AgregarProductoPedidoEditar.this, EditarPedido.class);
            c.putExtra("usuario", usuario);
            c.putExtra("id_pedido", id_pedido);
            c.putExtra("borre_producto", true);
            startActivity(c);

            EditarPedido.fa.finish();
            // Prevent the user to go back to this activity
            finish();
        }

    }
}
