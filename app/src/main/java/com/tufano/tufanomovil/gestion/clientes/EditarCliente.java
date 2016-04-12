package com.tufano.tufanomovil.gestion.clientes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.Funciones;

import java.util.ArrayList;

/**
 * Created por Usuario Tufano on 19/01/2016.
 */
public class EditarCliente extends AppCompatActivity
{
    private String usuario;
    private Context contexto;
    private final String TAG = "EditarCliente";
    private ProgressDialog pDialog;
    private DBAdapter manager;
    public static Activity fa;
    private TextView cabecera_1, cabecera_2, cabecera_3, cabecera_4, cabecera_5;
    private AutoCompleteTextView razon_social;
    private Spinner estado;
    private boolean primerInicio = true;
    private ArrayList<View> filas;
    private LinearLayout layout;
    private String columna_ordenada, orden;
    private String razon_social_filtrado, estado_filtrado;
    private boolean primerCargaTabla = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_clientes);
        fa = this;

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        createToolBar();
        getExtrasVar();
        noInitialFocus();
        initSpinners();
        initAutoCompleteTextView();
        initTextViewHeader();

        columna_ordenada = "razon_social";
        orden = "ASC";

        new cargarDatos().execute();
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
        toolbar.setSubtitle(R.string.editar_cliente_subtitulo);
        setSupportActionBar(toolbar);
    }

    /**
     * Inicializa las cabeceras de la tabla.
     */
    private void initTextViewHeader()
    {
        cabecera_1 = (TextView) findViewById(R.id.cabecera_1);
        cabecera_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_1, "razon_social");
            }
        });

        cabecera_2 = (TextView) findViewById(R.id.cabecera_2);
        cabecera_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_2, "rif");
            }
        });

        cabecera_3 = (TextView) findViewById(R.id.cabecera_3);
        cabecera_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_3, "estado");
            }
        });

        cabecera_4 = (TextView) findViewById(R.id.cabecera_4);
        cabecera_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_4, "telefono");
            }
        });

        cabecera_5 = (TextView) findViewById(R.id.cabecera_5);
        cabecera_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cabeceraPresionada(cabecera_5, "email");
            }
        });
    }

    /**
     * Metodo encargado de gestionar la cabecera de la tabla presionada, de colocar la imagen
     * correspondiente y de ordenar la tabla segun el campo presionado por la cabecera.
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
            limpiarHeaders();
            colocarCompoundDrawable(cabecera);
            ordenarTabla(columnaBD, "ASC");
        }
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
     * Ordenar la tabla bajo los parametros indicados.
     * @param cabecera Cabecera bajo la cual se ordenara.
     * @param orden Orden a utilizar (ASC o DESC)
     */
    private void ordenarTabla(String cabecera, String orden)
    {
        columna_ordenada = cabecera;
        this.orden = orden;
        limpiarTabla();
        //estado razonsocial
        new reCargarDatos().execute(estado_filtrado, razon_social_filtrado);
    }

    /**
     * Elimina todos los compounds drawables de las cabeceras.
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
     * Inicializador del autocompleteTextView
     */
    private void initAutoCompleteTextView()
    {
        razon_social = (AutoCompleteTextView) findViewById(R.id.autoC_razonSocial);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, obtenerRazonesSociales());

        razon_social.setAdapter(adapter);
        razon_social.setThreshold(1);
        razon_social.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                // Si presiono la tecla ENTER
                if (keyCode == 66)
                {
                    layout.requestFocus();
                    cerrarTeclado();
                    int pos = estado.getSelectedItemPosition();
                    if(pos>0)
                    {
                        // Filtrando por estado y razon social.
                        estado_filtrado = estado.getSelectedItem().toString();
                        razon_social_filtrado = razon_social.getText().toString().trim();
                        filtrarTabla();
                    }
                    else
                    {
                        // Filtrando solo por razon social..
                        estado_filtrado = "";
                        razon_social_filtrado = razon_social.getText().toString().trim();
                        filtrarTabla();
                    }
                    return true;
                }
                return false;
            }
        });

        razon_social.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!razon_social.getText().toString().trim().equals(""))
                {
                    int pos = estado.getSelectedItemPosition();
                    if (pos > 0)
                    {
                        cerrarTeclado();
                        // Filtrando por estado y razon social
                        estado_filtrado = estado.getSelectedItem().toString();
                        razon_social_filtrado = razon_social.getText().toString().trim();
                        filtrarTabla();
                    }
                    else
                    {
                        cerrarTeclado();
                        // Filtrando por razon social
                        estado_filtrado = "";
                        razon_social_filtrado = razon_social.getText().toString().trim();
                        filtrarTabla();
                    }
                }
                else
                {
                    int pos = estado.getSelectedItemPosition();
                    if (pos > 0)
                    {
                        cerrarTeclado();
                        // Filtrando por estado
                        estado_filtrado = estado.getSelectedItem().toString();
                        razon_social_filtrado = "";
                        filtrarTabla();
                    }
                    else
                    {
                        cerrarTeclado();
                        // Sin filtros
                        estado_filtrado = "";
                        razon_social_filtrado = "";
                        filtrarTabla();
                    }
                }
            }
        });
    }

    /**
     * Inicializar Spinners de Estados.
     */
    private void initSpinners()
    {
        estado = (Spinner) findViewById(R.id.spEstado_editar_cliente);
        estado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(!primerInicio)
                {
                    //Log.w(TAG, "Has seleccionado " + estado.getItemAtPosition(position));
                    if(position>0)
                    {
                        if (!razon_social.getText().toString().trim().equals(""))
                        {
                            estado_filtrado = estado.getItemAtPosition(position).toString();
                            razon_social_filtrado = razon_social.getText().toString().trim();
                            filtrarTabla();
                        }
                        else
                        {
                            estado_filtrado = estado.getItemAtPosition(position).toString();
                            razon_social_filtrado = "";
                            filtrarTabla();
                        }
                    }
                    else
                    {
                        if (!razon_social.getText().toString().trim().equals(""))
                        {
                            estado_filtrado = "";
                            razon_social_filtrado = razon_social.getText().toString().trim();
                            filtrarTabla();
                        }
                        else
                        {
                            estado_filtrado = "";
                            razon_social_filtrado = "";
                            filtrarTabla();
                        }
                    }
                }
                else
                    primerInicio = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    /**
     * Funcion para cerrar el teclado.
     */
    private void cerrarTeclado()
    {
        InputMethodManager inputManager =
                (InputMethodManager) contexto.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null)
            inputManager.hideSoftInputFromWindow( getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        else
            Log.w(TAG, "No se pudo ocultar el Teclado");
    }

    /**
     * Ejecucion del filtro de datos de la tabla.
     */
    private void filtrarTabla()
    {
        String defaultValueEstado = this.estado.getItemAtPosition(0).toString();
        limpiarTabla();
        if(!estado_filtrado.equals(defaultValueEstado) && !razon_social_filtrado.equals(""))
        {
            //Log.w(TAG, "Filtrando tabla por: Estado = " + estado_filtrado + " y Razon Social = " + razon_social_filtrado);
            new reCargarDatos().execute(estado_filtrado, razon_social_filtrado);
        }
        else if(estado_filtrado.equals(defaultValueEstado) && razon_social_filtrado.equals(""))
        {
            //Log.w(TAG, "Filtrando tabla por: NADA");
            new reCargarDatos().execute(null, null);
        }
        else if(!estado_filtrado.equals(defaultValueEstado))
        {
            //Log.w(TAG, "Filtrando tabla por: Estado = "+estado_filtrado);
            new reCargarDatos().execute(estado_filtrado, null);
        }
        else if(!razon_social_filtrado.equals(""))
        {
            //Log.w(TAG, "Filtrando tabla por: Razon Social = " + razon_social_filtrado);
            new reCargarDatos().execute(null, razon_social_filtrado);
        }
    }

    /**
     * Limpia toda la informacion existente en la tabla.
     */
    private void limpiarTabla()
    {
        final TableLayout tabla = (TableLayout) findViewById(R.id.table_editar_clientes);
        for (int i = 0; i < filas.size(); i++)
        {
            tabla.removeView(filas.get(i));
        }
    }

    /**
     * Obtiene todas las razones sociales de los clientes existentes en la BD.
     * @return Una lista de todas las razones sociales de la BD.
     */
    private ArrayList<String> obtenerRazonesSociales()
    {
        Cursor cursor = manager.cargarClientes();

        ArrayList<String> rs = new ArrayList<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            rs.add(cursor.getString(1));
        }
        return rs;
    }

    private class cargarDatos extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarCliente.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            if(primerCargaTabla)
            {
                //initSpinners();
                loadSpinnerData();
                primerCargaTabla = false;
            }
            inicializarTabla(false);
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
        }

    }

    /**
     * Carga la data del spinner de estados con data predefinida.
     */
    private void loadSpinnerData()
    {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                contexto, R.array.estados_lista, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        estado.setAdapter(adapter);
    }

    /**
     * Clase encargada de volver a cargar la informacion de la tabla en segundo plano, incluyendo
     * parametros de filtrado como el estado y/o la razon social.
     */
    private class reCargarDatos extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarCliente.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando informacion...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            estado_filtrado = params[0];
            razon_social_filtrado = params[1];
            inicializarTabla(true);
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();
        }
    }

    /**
     * Metodo base encargado de inicializar la tabla segun los patrones de filtrado y orden.
     * @param filtrado Indica si se esta realizando un filtrado, unicamente sera false en la
     * primera ejecucion.
     */
    private void inicializarTabla(boolean filtrado)
    {
        Log.i(TAG, "Inicializando tabla.. Ordenando por: " + columna_ordenada + ", orden: " + orden);
        Log.i(TAG, "Filtrando por... Estado: "+this.estado_filtrado+", RazonSocial: "+this.razon_social_filtrado);

        final TableLayout tabla = (TableLayout) findViewById(R.id.table_editar_clientes);
        filas = new ArrayList<>();

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        //razon_social_filtrado = razon_social.getText().toString().trim();
        //estado_filtrado = estado.getItemAtPosition(position).toString();

        Cursor cursor = manager.cargarClientesFiltrado(this.estado_filtrado, this.razon_social_filtrado,
                columna_ordenada, orden);

        if (cursor.getCount() > 0)
        {
            mostrarTodo(tabla);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                Log.i(TAG, "Agregando fila..");
                final TableRow fila = new TableRow(contexto);

                // Extrayendo datos de la base de datos
                final String id_cliente = String.valueOf(cursor.getInt(0));
                final String rs = cursor.getString(1);
                final String rifs = cursor.getString(2);
                final String estados = cursor.getString(3);
                final String tlf = cursor.getString(4);
                final String mail = cursor.getString(5);
                final String dir = cursor.getString(6);
                final String estatus = cursor.getString(7);

                /* Razon Social */
                TextView razon_social = new TextView(contexto);
                razon_social.setText(rs);
                razon_social.setTextColor(Color.DKGRAY);
                razon_social.setGravity(Gravity.CENTER);
                razon_social.setLayoutParams(params);
                razon_social.setTextSize(16f);

                /* Rif */
                TextView rif = new TextView(contexto);
                rif.setText(Funciones.formatoRif(rifs));
                rif.setTextColor(Color.DKGRAY);
                rif.setGravity(Gravity.CENTER);
                rif.setLayoutParams(params);
                rif.setTextSize(16f);

                /* Estado */
                TextView estado = new TextView(contexto);
                estado.setText(estados);
                estado.setTextColor(Color.DKGRAY);
                estado.setGravity(Gravity.CENTER);
                estado.setLayoutParams(params);
                estado.setTextSize(16f);

                /* Telefono */
                TextView telefono = new TextView(contexto);
                telefono.setText(Funciones.formatoTelefono(tlf));
                telefono.setTextColor(Color.DKGRAY);
                telefono.setGravity(Gravity.CENTER);
                telefono.setLayoutParams(params);
                telefono.setTextSize(16f);

                /* E-Mail */
                TextView email = new TextView(contexto);
                email.setText(mail);
                email.setTextColor(Color.DKGRAY);
                email.setGravity(Gravity.CENTER);
                email.setLayoutParams(params);
                email.setTextSize(16f);

                /* Opciones */
                Button editar = new Button(contexto);
                editar.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent c = new Intent(EditarCliente.this, EditarClienteDetalles.class);
                        c.putExtra("usuario", usuario);
                        c.putExtra("id_cliente", id_cliente);
                        c.putExtra("rs", rs);
                        c.putExtra("rif", rifs);
                        c.putExtra("estados", estados);
                        c.putExtra("tlf", tlf);
                        c.putExtra("mail", mail);
                        c.putExtra("dir", dir);
                        c.putExtra("estatus", estatus);
                        startActivity(c);
                    }
                });

                editar.setBackgroundResource(R.drawable.icn_edit);
                int edit_image_width = 70;
                int edit_image_height = 70;
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(edit_image_width, edit_image_height);
                editar.setLayoutParams(parms);
                editar.setPadding(2, 10, 2, 10);

                LinearLayout opciones = new LinearLayout(contexto);
                opciones.setGravity(Gravity.CENTER);
                opciones.setLayoutParams(params);
                opciones.addView(editar);

                // Llenando la fila con data
                fila.setBackgroundColor(Color.WHITE);
                fila.addView(razon_social);
                fila.addView(rif);
                fila.addView(estado);
                fila.addView(telefono);
                fila.addView(email);
                fila.addView(opciones);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    fila.setBackground(Funciones.intToDrawable(contexto, R.drawable.table_border));
                else
                    //noinspection deprecation
                    fila.setBackgroundDrawable(Funciones.intToDrawable(contexto,
                            R.drawable.table_border));

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
            if(!filtrado)
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

                                    TextView mensaje = new TextView(contexto);
                                    mensaje.setText(R.string.msj_cliente_vacio);
                                    mensaje.setGravity(Gravity.CENTER);
                                    mensaje.setTextSize(20f);

                                    LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor);
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
                TableRow.LayoutParams parametros = new TableRow.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f);

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
                    fila.setBackgroundDrawable(Funciones.intToDrawable(contexto,
                            R.drawable.table_border));

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
     * @param tabla Tabla a la cual se le haran visibles los componentes (Razon social, Estado, Tabla)
     */
    private void mostrarTodo(TableLayout tabla)
    {
        tabla.setVisibility(View.VISIBLE);
        AutoCompleteTextView rs = (AutoCompleteTextView) findViewById(R.id.autoC_razonSocial);
        LinearLayout estado = (LinearLayout) findViewById(R.id.contenedor_spinners);
        rs.setVisibility(View.VISIBLE);
        estado.setVisibility(View.VISIBLE);
    }

    /**
     * Oculta todos los componentes de la tabla.
     * @param tabla Tabla a la cual se le haran invisibles los componentes (Razon social, Estado, Tabla)
     */
    private void ocultarTodo(TableLayout tabla)
    {
        tabla.setVisibility(View.INVISIBLE);
        AutoCompleteTextView rs = (AutoCompleteTextView) findViewById(R.id.autoC_razonSocial);
        LinearLayout estado = (LinearLayout) findViewById(R.id.contenedor_spinners);
        rs.setVisibility(View.INVISIBLE);
        estado.setVisibility(View.INVISIBLE);
    }
}