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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.gestion.colores.AgregarColor;
import com.tufano.tufanomovil.gestion.colores.EditarColor;
import com.tufano.tufanomovil.gestion.colores.GestionColores;
import com.tufano.tufanomovil.gestion.talla.AgregarTalla;
import com.tufano.tufanomovil.gestion.talla.EditarTalla;
import com.tufano.tufanomovil.gestion.talla.GestionTallas;
import com.tufano.tufanomovil.gestion.tipo.AgregarTipo;
import com.tufano.tufanomovil.gestion.tipo.EditarTipo;
import com.tufano.tufanomovil.gestion.tipo.GestionTipos;
import com.tufano.tufanomovil.global.Constantes;
import com.tufano.tufanomovil.global.Funciones;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created por Usuario Tufano on 12/01/2016.
 */
public class AgregarProducto extends AppCompatActivity
{
    private String usuario;
    private Context contexto;
    private final String TAG = "AgregarProducto";
    private final int PICK_IMAGE_REQUEST = 1;
    private Bitmap imagen_cargada = null;
    private ProgressDialog pDialog;
    private DBAdapter manager;
    private Spinner sp_color;
    private List<List<String>> contenedor_colores, contenedor_tipos, contenedor_tallas;
    private Spinner talla, tipo;
    private ArrayList<Integer> ids_tabla;
    private Button btn_agregar_producto, btn_agregar_tipo, btn_agregar_talla, btn_agregar_color,
    btn_editar_tipo, btn_editar_talla, btn_editar_color;
    public static Activity fa;

    private String current_talla, current_tipo, current_color;
    private String idTipoSeleccionado;
    private String idTallaSeleccionada;
    private String idColorSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_productos);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);
        fa = this;

        getExtrasVar();
        createToolBar();
        noInitialFocus();
        initComponents();
        initImageView();
        loadSpinnerData();
        buttonsActions();
        spinnersActions();

        //if(desdeTipo)
            seleccionarTipo(idTipoSeleccionado);

        //if(desdeTalla)
            seleccionarTalla(idTallaSeleccionada);

        //if(desdeColor)
            seleccionarColor(idColorSeleccionado);
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        usuario = bundle.getString("usuario");

        //boolean desdeTipo = bundle.getBoolean("desdeTipo");
        idTipoSeleccionado = bundle.getString("idTipoCreado");

        //boolean desdeTalla = bundle.getBoolean("desdeTalla");
        idTallaSeleccionada = bundle.getString("idTallaCreado");

        //boolean desdeColor = bundle.getBoolean("desdeColor");
        idColorSeleccionado = bundle.getString("idColorCreado");
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.agregar_producto_subtitulo);
        setSupportActionBar(toolbar);
    }

    /**
     * Evita el focus principal al abrir la activity, el cual despliega automaticamente el teclado
     */
    private void noInitialFocus()
    {
        LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayout_MainActivity);
        layout.requestFocus();
    }

    /**
     * Selecciona el tipo de producto por defecto.
     * @param id ID del tipo de producto.
     */
    private void seleccionarTipo(String id)
    {
        if(id!=null)
        {
            Log.i(TAG, "Seleccionando tipo..");
            int pos = buscarPosicion(id, contenedor_tipos);
            tipo.setSelection(pos);
        }
    }

    /**
     * Selecciona la talla del producto por defecto.
     * @param id ID de la talla del producto.
     */
    private void seleccionarTalla(String id)
    {
        if(id!=null)
        {
            Log.i(TAG, "Seleccionando talla..");
            int pos = buscarPosicion(id, contenedor_tallas);
            talla.setSelection(pos);
        }
    }

    /**
     * Selecciona el color de producto por defecto.
     * @param id ID del color de producto.
     */
    private void seleccionarColor(String id)
    {
        if(id!=null)
        {
            Log.i(TAG, "Seleccionando color..");
            int pos = buscarPosicion(id, contenedor_colores);
            sp_color.setSelection(pos);
        }
    }

    /**
     * Busca la posicion de un elemento dentro de una Array de listas (String)
     * @param id Elemento a buscar
     * @param contenedor Array de Array en el cual se buscara el elemento.
     * @return Retorna la posicion del elemento encontrado.
     */
    private int buscarPosicion(String id, List<List<String>> contenedor)
    {
        for(int i=0; i< contenedor.get(0).size(); i++)
        {
            if(contenedor.get(0).get(i).equals(id))
                return i+1;
        }
        return -1;
    }

    /**
     * Inicializa los botones de agregar producto, agregar/editar tipo, agregar/editar talla,
     * agregar/editar color
     */
    private void buttonsActions()
    {
        btn_agregar_producto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camposValidados())
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AgregarProducto.this);

                    dialog.setMessage(R.string.confirmacion_agregar_producto);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            agregarProducto();
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
            }
        });

        btn_agregar_tipo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent c = new Intent(AgregarProducto.this, AgregarTipo.class);
                c.putExtra("usuario", usuario);
                c.putExtra("desdeProductos", true);
                c.putExtra("idTallaCreado", current_talla);
                c.putExtra("idColorCreado", current_color);

                startActivity(c);
            }
        });

        btn_agregar_talla.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent c = new Intent(AgregarProducto.this, AgregarTalla.class);
                c.putExtra("usuario", usuario);
                c.putExtra("desdeProductos", true);
                c.putExtra("idTipoCreado", current_tipo);
                c.putExtra("idColorCreado", current_color);

                startActivity(c);
            }
        });

        btn_agregar_color.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent c = new Intent(AgregarProducto.this, AgregarColor.class);
                c.putExtra("usuario", usuario);
                c.putExtra("desdeProductos", true);
                c.putExtra("idTipoCreado", current_tipo);
                c.putExtra("idTallaCreado", current_talla);
                startActivity(c);
            }
        });

        btn_editar_tipo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(tipo.getSelectedItemPosition() >0)
                {
                    String id_tipo = contenedor_tipos.get(0).get(tipo.getSelectedItemPosition() - 1);

                    Intent c = new Intent(AgregarProducto.this, EditarTipo.class);
                    c.putExtra("desdeProductos", true);
                    c.putExtra("usuario", usuario);
                    c.putExtra("id_tipo", id_tipo);
                    c.putExtra("idTallaCreado", current_talla);
                    c.putExtra("idColorCreado", current_color);
                    startActivity(c);
                }
                else
                {
                    Toast.makeText(contexto, "¡Por favor seleccione algun tipo!", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_editar_talla.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(talla.getSelectedItemPosition() >0)
                {
                    String id_talla = contenedor_tallas.get(0).get(talla.getSelectedItemPosition() - 1);

                    Intent c = new Intent(AgregarProducto.this, EditarTalla.class);
                    c.putExtra("desdeProductos", true);
                    c.putExtra("usuario", usuario);
                    c.putExtra("id_talla", id_talla);
                    c.putExtra("idTipoCreado", current_tipo);
                    c.putExtra("idColorCreado", current_color);
                    startActivity(c);
                }
                else
                {
                    Toast.makeText(contexto, "¡Por favor seleccione alguna talla!", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_editar_color.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(sp_color.getSelectedItemPosition() >0)
                {
                    String id_color = contenedor_colores.get(0).get(sp_color.getSelectedItemPosition() - 1);

                    Intent c = new Intent(AgregarProducto.this, EditarColor.class);
                    c.putExtra("desdeProductos", true);
                    c.putExtra("usuario", usuario);
                    c.putExtra("id_color", id_color);
                    c.putExtra("idTipoCreado", current_tipo);
                    c.putExtra("idTallaCreado", current_talla);
                    startActivity(c);
                }
                else
                {
                    Toast.makeText(contexto, "¡Por favor seleccione algun color!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    /**
     * Inicializa el imageView para la seleccion del producto.
     */
    private void initImageView()
    {
        ImageView seleccion_img_producto = (ImageView) findViewById(R.id.seleccion_img_producto);
        seleccion_img_producto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Buscar imagen en.."), PICK_IMAGE_REQUEST);
            }
        });
    }

    /**
     * Define las acciones de los spinners.
     */
    private void spinnersActions()
    {
        talla.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(position>0)
                {
                    String numeracion = talla.getSelectedItem().toString().trim();
                    int parentesisPos = Funciones.buscarCaracter(numeracion, '(');
                    numeracion = numeracion.substring(parentesisPos);
                    numeracion = numeracion.replace("(", "").replace(")", "");
                    int guionPos = Funciones.buscarCaracter(numeracion, '-');
                    int minimo = Integer.parseInt(numeracion.substring(0, guionPos));
                    int maximo = Integer.parseInt(numeracion.substring(guionPos+1));
                    int diferencia = maximo - minimo;
                    current_talla = String.valueOf(obtenerIDTalla());
                    Log.d(TAG, "Has seleccionado la numeracion: " + numeracion);
                    Log.d(TAG, "minimo: " + minimo + ", maximo: " + maximo + ", diferencia: " + diferencia);

                    crearMiniTablaNumeracion(minimo, diferencia);
                }
                else
                {
                    final LinearLayout bulto_numeracion = (LinearLayout) findViewById(R.id.bulto_numeracion);
                    final LinearLayout cabecera = (LinearLayout) findViewById(R.id.cabecera_bulto_numeracion);
                    bulto_numeracion.removeAllViews();
                    cabecera.removeAllViews();
                    ids_tabla = new ArrayList<>();
                    current_talla = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected");
            }
        });

        tipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(position>0)
                    current_tipo = String.valueOf(obtenerIDTipo());
                else
                    current_tipo = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sp_color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(position>0)
                    current_color = String.valueOf(obtenerIDColor());
                else
                    current_color = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Inicializa los componentes principales de la clase.
     */
    private void initComponents()
    {
        talla = (Spinner) findViewById(R.id.talla_producto);
        tipo = (Spinner) findViewById(R.id.tipo_producto);
        sp_color = (Spinner) findViewById(R.id.color_producto);
        btn_agregar_producto = (Button) findViewById(R.id.btn_agregar_producto);
        btn_agregar_tipo = (Button) findViewById(R.id.btn_agregar_tipo);
        btn_agregar_talla = (Button) findViewById(R.id.btn_agregar_talla);
        btn_agregar_color = (Button) findViewById(R.id.btn_agregar_color);
        btn_editar_tipo = (Button) findViewById(R.id.btn_editar_tipo);
        btn_editar_talla = (Button) findViewById(R.id.btn_editar_talla);
        btn_editar_color = (Button) findViewById(R.id.btn_editar_color);
    }

    /**
     * Crea la tabla de numeracion de pares por talla.
     * @param minimo Valor minimo de la talla. Por ej 26.
     * @param diferencia Diferencia entre el valor minimo y el valor maximo.
     * Por ej (34-26) = 8 (Diferencia) ---> Numero de tallas
     */
    private void crearMiniTablaNumeracion(int minimo, int diferencia)
    {
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        final LinearLayout bulto_numeracion = (LinearLayout) findViewById(R.id.bulto_numeracion);
        final LinearLayout cabecera = (LinearLayout) findViewById(R.id.cabecera_bulto_numeracion);
        String currentValue;

        bulto_numeracion.removeAllViews();
        cabecera.removeAllViews();
        ids_tabla = new ArrayList<>();
        int nextId = 0;

        for(int i=0; i<=diferencia; i++)
        {
            currentValue = String.valueOf(minimo + i);

            final EditText componente = new EditText(contexto);
            final TextView titulo = new TextView(contexto);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                if(nextId!=0)
                {
                    componente.setId(nextId);
                    ids_tabla.add(nextId);
                }
                else
                {
                    int id = View.generateViewId();
                    componente.setId(id);
                    ids_tabla.add(id);
                }

                if(i+1 <= diferencia)
                {
                    nextId = View.generateViewId();
                }
            }
            else
            {
                if(nextId!=0)
                {
                    componente.setId(nextId);
                    ids_tabla.add(nextId);
                }
                else
                {
                    int id = Funciones.generateViewId();
                    componente.setId(id);
                    ids_tabla.add(id);
                }

                if(i+1 <= diferencia)
                {
                    nextId = Funciones.generateViewId();
                }
            }

            //componente.setText(currentValue);
            componente.setBackgroundResource(android.R.drawable.edit_text);
            componente.setInputType(InputType.TYPE_CLASS_NUMBER);
            componente.setLayoutParams(params);
            if(i+1 <= diferencia)
                componente.setNextFocusDownId(nextId);
            titulo.setLayoutParams(params);
            titulo.setGravity(Gravity.CENTER);
            titulo.setTextSize(18f);
            titulo.setTypeface(null, Typeface.BOLD);
            titulo.setText(currentValue);

            final Thread hilo = new Thread() {
                @Override
                public void run() {
                    synchronized (this) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                cabecera.addView(titulo);
                                bulto_numeracion.addView(componente);
                            }
                        });
                    }
                }
            };
            hilo.start();

            //bulto_numeracion.addView(componente);
        }

    }

    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData()
    {
        contenedor_colores = manager.cargarListaColores();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, contenedor_colores.get(1));
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp_color.setAdapter(dataAdapter);

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
     * Obtiene el ID del color a partir de contenedor con todos los colores.
     * @return ID del color.
     */
    private int obtenerIDColor()
    {
        int actual_position = sp_color.getSelectedItemPosition();
        return Integer.parseInt(contenedor_colores.get(0).get(actual_position - 1));
    }

    /**
     * Obtiene el ID del tipo de producto a partir de contenedor con todos los tipos.
     * @return ID del tipo de producto.
     */
    private int obtenerIDTipo()
    {
        int actual_position = tipo.getSelectedItemPosition();
        return Integer.parseInt(contenedor_tipos.get(0).get(actual_position - 1));
    }

    /**
     * Obtiene el ID de la talla a partir de contenedor con todas las tallas.
     * @return ID de la talla.
     */
    private int obtenerIDTalla()
    {
        int actual_position = talla.getSelectedItemPosition();
        return Integer.parseInt(contenedor_tallas.get(0).get(actual_position - 1));
    }

    /**
     * Preparando los datos para agregar el producto
     */
    private void agregarProducto()
    {
        Log.i(TAG, "Preparando datos para agregar el producto..");

        // Recopilar todos los datos para agregar el producto..
        EditText et_modelo = (EditText) findViewById(R.id.modelo_producto);
        Spinner et_color = (Spinner) findViewById(R.id.color_producto);
        EditText et_precio = (EditText) findViewById(R.id.precio_producto);
        Spinner sp_tipo = (Spinner) findViewById(R.id.tipo_producto);
        Spinner sp_talla = (Spinner) findViewById(R.id.talla_producto);

        String tipo = sp_tipo.getSelectedItem().toString().trim(); // Obtengo por ej. "Torera"
        String talla = sp_talla.getSelectedItem().toString().substring(0, 1).trim(); // Obtengo por ej. "P (18-25)"
        String color = et_color.getSelectedItem().toString().trim();
        String precio = et_precio.getText().toString().trim();
        String modelo = et_modelo.getText().toString().trim();
        String numeracion = sp_talla.getSelectedItem().toString().substring(2).trim();
        int idColor = obtenerIDColor();
        int idTipo = obtenerIDTipo();
        int idTalla = obtenerIDTalla();

        String paresxtalla = obtenerParesxTallas();

        Log.i(TAG, "Los datos del producto a agregar son los siguientes: -Talla: " + talla + " -Tipo: " + tipo + " -Modelo: " + modelo + " -Precio: " + precio + " -Color: " + color + " -id_color: " + idColor + " -Numeracion: " + numeracion);
        new async_crearProductoBD().execute(String.valueOf(idTalla), String.valueOf(idTipo), precio, String.valueOf(idColor), modelo, numeracion, paresxtalla);
    }

    /**
     * Obtiene los pares por tallas de la mini tabla.
     * @return Pares por talla en un formato separado por comas.
     */
    private String obtenerParesxTallas()
    {
        String res = null;
        for (int i = 0; i < ids_tabla.size(); i++)
        {
            EditText et = (EditText) findViewById(ids_tabla.get(i));

            if(i==0)
                res = et.getText().toString();
            else
                res += ","+et.getText().toString();
        }
        return res;
    }

    /**
     * Validacion previa de campos para agregar un producto.
     * @return True si los campos estan correctos, False en caso contrario.
     */
    private boolean camposValidados()
    {
        Log.i(TAG, "Validando campos");
        EditText modelo = (EditText) findViewById(R.id.modelo_producto);
        Spinner color = (Spinner) findViewById(R.id.color_producto);
        EditText precio = (EditText) findViewById(R.id.precio_producto);
        Spinner tipo = (Spinner) findViewById(R.id.tipo_producto);
        Spinner talla = (Spinner) findViewById(R.id.talla_producto);

        if( modelo.getText().toString().trim().equals("") )
        {
            modelo.setError("Inserte un modelo!");
            return false;
        }
        else if ( color.getSelectedItemPosition()==0 )
        {
            TextView errorText = (TextView) color.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_color);//changes the selected item text to this
            return false;
        }
        else if ( precio.getText().toString().trim().equals("") )
        {
            precio.setError("Inserte un precio!");
            return false;
        }
        else if ( tipo.getSelectedItemPosition()==0 )
        {
            TextView errorText = (TextView) tipo.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_tipo);//changes the selected item text to this
            return false;
        }
        else if( talla.getSelectedItemPosition()==0 )
        {
            TextView errorText = (TextView) talla.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_talla);//changes the selected item text to this
            return false;
        }
        else if (imagen_cargada==null)
        {
            Toast.makeText(contexto, "Indique una imagen para el producto", Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!miniTablaEstaLlena())
        {
            Toast.makeText(contexto, "Indique la cantidad de pares por talla", Toast.LENGTH_LONG).show();
            return false;
        }
        else
        {
            modelo.setError(null);
            precio.setError(null);
            return true;
        }
    }

    /**
     * Determina si la mini-tabla de los pares por talla esta llena o no.
     * @return True si la tabla de cantidad x pares esta llena
     */
    private boolean miniTablaEstaLlena()
    {
        for (int i = 0; i < ids_tabla.size(); i++)
        {
            EditText et = (EditText) findViewById(ids_tabla.get(i));
            if(et.getText().toString().isEmpty())
                return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Recibiendo imagen");

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            Uri selectedImageUri = data.getData();

            try
            {
                // Cargamos en memoria la imagen seleccionada por el usuario.
                imagen_cargada = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

                // Buscamos la ruta de la imagen en cuestion.
                //String selectedImagePath = getPath(selectedImageUri);

                // Creamos una version minificada de la imagen.
                //Bitmap preview = getPreview(selectedImagePath);

                // Asignamos la imagen preview para que el usuario la visualice.
                ImageView imageView = (ImageView) findViewById(R.id.seleccion_img_producto);
                imageView.setImageBitmap(imagen_cargada);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                {
                    imageView.setBackground(null);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Clase para agregar un producto a la base de datos en segundo plano.
     */
    class async_crearProductoBD extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(AgregarProducto.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Agregando el producto...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            /*
            talla=params[0];
            tipo=params[1];
            precio=params[2];
            color=params[3];
            modelo=params[4];
            numeracion=params[5];
            */

            try
            {
                //enviamos y recibimos y analizamos los datos en segundo plano.
                if (crearProducto(params))
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
                Toast.makeText(contexto, "Producto agregado exitosamente!!", Toast.LENGTH_LONG).show();

                // Redirige a la pantalla de Home
                Intent c = new Intent(AgregarProducto.this, GestionProductos.class);
                c.putExtra("usuario", usuario);
                startActivity(c);
                GestionProductos.fa.finish();

                // Prevent the user to go back to this activity
                finish();
            }
            else
            {
                Toast.makeText(contexto, "Hubo un error agregando el producto..", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Crea el producto en la Base de datos
     * @param datos Datos con los cuales se creara el producto.
     * @return True si la operacion fue exitosa, false en caso contrario.
     */
    private boolean crearProducto(String[] datos)
    {
        if( !existeProducto(datos[4]) )
        {
            long id_producto = manager.agregarProducto(datos);
            Log.d(TAG, "ID_producto: " + id_producto);
            // Si el producto fue agregado exitosamente en BD, proceso a copiar la imagen..
            return id_producto != -1 && guardarImagen(datos[4]);
        }
        else
        {
            Log.e(TAG, "Ya existe dicho producto!!");
            return false;
        }
    }

    /**
     *
     * @param modelo Modelo que se quiere saber si existe
     * @return Devuelve true si el producto ya existe con el modelo ingresado
     */
    private boolean existeProducto(String modelo)
    {
        Cursor cursor = manager.cargarProductosModelo(modelo);
        return cursor.getCount() > 0;
    }

    /**
     * Funcion para guardar una imagen del producto en el dispositivo movil.
     * @param nombre Nombre de la imagen.
     * @return True si la imagen fue guardada con exito, false en caso contrario.
     */
    private boolean guardarImagen(String nombre)
    {
        Log.i(TAG, "Se guardara una imagen con el nombre de: " + nombre);
        File imagesFolder;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        if(imagen_cargada != null)
        {
            imagen_cargada.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        }
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
            {
                imagesFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles");
            }
            else
            {
                imagesFolder = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles");
            }
            if (!imagesFolder.exists())
            {
                if (imagesFolder.mkdirs())
                    Log.i(TAG, "Carpeta creada exitosamente");
                else
                {
                    Log.e(TAG, "La carpeta no pudo ser creada..");
                    return false;
                }
            }
            try
            {
                File myfile = File.createTempFile(nombre, Constantes.EXTENSION_IMG, imagesFolder);
                FileOutputStream out = new FileOutputStream(myfile);
                String temp_name = myfile.getName();

                Log.i(TAG, "Temporal name: " + temp_name);

                out.write(bytes.toByteArray());
                out.flush();
                out.close();

                File from = new File(imagesFolder, temp_name);
                File to = new File(imagesFolder, nombre + Constantes.EXTENSION_IMG);

                if (from.renameTo(to))
                {
                    Log.i(TAG, "File successfully renamed");
                }
                else
                {
                    Log.e(TAG, "File could not be renamed");
                }
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
                return false;
            }

            /*Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
            Uri contentUri = Uri.fromFile(myfile);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);*/

            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_agregar_productos, menu);
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
        else if (id == R.id.tallas_settings)
        {
            mostrarGestionTallas();
            return true;
        }
        else if (id == R.id.tipos_settings)
        {
            mostrarGestionTipos();
            return true;
        }
        else if (id == R.id.colores_settings)
        {
            mostrarGestionColores();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        Log.d(TAG, "onSaveInstanceState");
        savedInstanceState.putParcelable("image", imagen_cargada);
        String[] cantidadxpar = new String[ids_tabla.size()];

        for (int i = 0; i < cantidadxpar.length; i++)
        {
            final EditText campo = (EditText) findViewById(ids_tabla.get(i));
            cantidadxpar[i] = campo.getText().toString();
        }

        savedInstanceState.putStringArray("cantidadxpar", cantidadxpar);
        savedInstanceState.putIntegerArrayList("ids", ids_tabla);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState");
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        imagen_cargada = savedInstanceState.getParcelable("image");
        //String[] cantidadxpar = savedInstanceState.getStringArray("cantidadxpar");
        //ArrayList<Integer> ids = savedInstanceState.getIntegerArrayList("ids");

        /*if(cantidadxpar != null && cantidadxpar.length>0)
        {
            for (int i = 0; i < cantidadxpar.length; i++)
            {
                if(ids!=null)
                {
                    final EditText campo = (EditText) findViewById(ids.get(i));
                    Log.i(TAG, "Valor obtenido: " + cantidadxpar[i] + ", ID: " + ids.get(i) );
                    campo.setText( String.valueOf(cantidadxpar[i]) );
                }
            }
        }*/

        ImageView imageView = (ImageView) findViewById(R.id.seleccion_img_producto);
        imageView.setImageBitmap(imagen_cargada);
    }

    /**
     * Inicia el activity para la gestion de tipos
     */
    private void mostrarGestionTipos()
    {
        Intent c = new Intent(AgregarProducto.this, GestionTipos.class);
        c.putExtra("usuario",usuario);
        startActivity(c);
    }

    /**
     * Inicia el activity para la gestion de tallas
     */
    private void mostrarGestionTallas()
    {
        Intent c = new Intent(AgregarProducto.this, GestionTallas.class);
        c.putExtra("usuario",usuario);
        startActivity(c);
    }

    /**
     * Inicia el activity para la gestion de colores
     */
    private void mostrarGestionColores()
    {
        Intent c = new Intent(AgregarProducto.this, GestionColores.class);
        c.putExtra("usuario",usuario);
        startActivity(c);
    }
}