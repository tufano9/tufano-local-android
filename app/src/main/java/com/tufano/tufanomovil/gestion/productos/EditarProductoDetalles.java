package com.tufano.tufanomovil.gestion.productos;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
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
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.gestion.colores.GestionColores;
import com.tufano.tufanomovil.gestion.talla.GestionTallas;
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
 * Created por Usuario Tufano on 14/01/2016.
 */
public class EditarProductoDetalles extends AppCompatActivity
{
    private String usuario;
    private Context contexto;
    private final int PICK_IMAGE_REQUEST = 1;
    private final String TAG = "EditarProductoDetalles";
    private ProgressDialog pDialog;
    private DBAdapter manager;
    private Bitmap imagen_cargada = null;
    private String id_producto, talla_producto, tipo_producto, color_producto, modelo_producto;
    private String precio_producto, numeracion_producto, estatus_producto, paresxtalla;
    private EditText modelo_seleccionado, precio_seleccionado;
    private Switch estatus_seleccionado;
    private Spinner sp_color, sp_tipo, sp_talla;
    private List<List<String>> contenedor_colores, contenedor_tipos, contenedor_tallas;
    private ArrayList<Integer> ids_tabla;
    private boolean primeraEjecucion = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_productos);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        getExtrasVar();
        createToolBar();
        noInitialFocus();
        initComponents();
        initImagesViews();
        initButtons();
        initListeners();
        cargarValoresPrevios();
    }

    /**
     * Inicializa los listeners
     */
    private void initListeners()
    {
        sp_talla.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (primeraEjecucion)
                {
                    Log.w(TAG, "Primera Ejecucion..");
                    primeraEjecucion = false;

                    String numeracion = sp_talla.getSelectedItem().toString().trim();

                    int start = Funciones.buscarCaracter(numeracion, '(');
                    int end = Funciones.buscarCaracter(numeracion, ')');

                    numeracion = numeracion.substring(start + 1, end);
                    //numeracion = numeracion.replace("(", "").replace(")", ""); // 18-25

                    int guionPos = Funciones.buscarCaracter(numeracion, '-');
                    int minimo = Integer.parseInt(numeracion.substring(0, guionPos));
                    int maximo = Integer.parseInt(numeracion.substring(guionPos + 1));
                    int diferencia = maximo - minimo;
                    Log.d(TAG, "Has seleccionado la numeracion: " + numeracion);
                    Log.d(TAG, "minimo: " + minimo + ", maximo: " + maximo + ", diferencia: " + diferencia);

                    crearMiniTablaNumeracion(minimo, diferencia, true);
                }
                else if (position > 0)
                {
                    Log.w(TAG, "Ya no es primera Ejecucion y seleccione alguna opcion valida..");
                    String numeracion = sp_talla.getSelectedItem().toString().trim().substring(1);
                    numeracion = numeracion.replace("(", "").replace(")", "");
                    int guionPos = Funciones.buscarCaracter(numeracion, '-');
                    int minimo = Integer.parseInt(numeracion.substring(0, guionPos));
                    int maximo = Integer.parseInt(numeracion.substring(guionPos + 1));
                    int diferencia = maximo - minimo;
                    Log.d(TAG, "Has seleccionado la numeracion: " + numeracion);
                    Log.d(TAG, "minimo: " + minimo + ", maximo: " + maximo + ", diferencia: " + diferencia);

                    crearMiniTablaNumeracion(minimo, diferencia, false);
                }
                else
                {
                    Log.w(TAG, "Ya no es primera Ejecucion y seleccione la opcion por defecto..");
                    final LinearLayout bulto_numeracion = (LinearLayout) findViewById(R.id.bulto_numeracion);
                    final LinearLayout cabecera = (LinearLayout) findViewById(R.id.cabecera_bulto_numeracion);
                    bulto_numeracion.removeAllViews();
                    cabecera.removeAllViews();
                    ids_tabla = new ArrayList<>();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected");
            }
        });
    }

    /**
     * Inicializa los botones
     */
    private void initButtons()
    {
        Button btn_editar_producto = (Button) findViewById(R.id.btn_editar_producto);
        btn_editar_producto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camposValidados()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(EditarProductoDetalles.this);

                    dialog.setMessage(R.string.confirmacion_editar_producto);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String talla2 = sp_talla.getSelectedItem().toString().substring(2);

                            String paresxtalla = obtenerParesxTallas();

                            int color_id = obtenerIDColor();
                            int idTipo = obtenerIDTipo();
                            int idTalla = obtenerIDTalla();
                            new async_editarProductoBD().execute(id_producto, String.valueOf(idTalla), String.valueOf(idTipo), modelo_seleccionado.getText().toString().trim(), String.valueOf(color_id), precio_seleccionado.getText().toString().trim(), talla2, estatus_seleccionado.isChecked() ? "1" : "0", paresxtalla);

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
    }

    /**
     * Inicializa los imageViews
     */
    private void initImagesViews()
    {
        ImageView imagen = (ImageView) findViewById(R.id.seleccion_img_producto_editar);
        imagen.setOnClickListener(new View.OnClickListener() {
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
     * Inicializa los componentes primarios de la activity
     */
    private void initComponents()
    {
        modelo_seleccionado = (EditText) findViewById(R.id.modelo_producto);
        precio_seleccionado = (EditText) findViewById(R.id.precio_producto);
        estatus_seleccionado = (Switch) findViewById(R.id.estatus_switch);

        /* Spinner Tipo */
        sp_tipo = (Spinner) findViewById(R.id.tipo_producto_editar);
        /* Spinner Talla */
        sp_talla = (Spinner) findViewById(R.id.talla_producto_editar);
        /* Spinner Color */
        sp_color = (Spinner) findViewById(R.id.color_producto);
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        usuario = bundle.getString("usuario");
        id_producto = bundle.getString("id_producto");
        talla_producto = bundle.getString("talla_producto");
        tipo_producto = bundle.getString("tipo_producto");
        modelo_producto = bundle.getString("modelo_producto");
        color_producto = bundle.getString("color_producto");
        precio_producto = bundle.getString("precio_producto");
        numeracion_producto = bundle.getString("numeracion_producto");
        estatus_producto = bundle.getString("estatus_producto");
        paresxtalla = bundle.getString("paresxtalla");
        //id_color = bundle.getString("id_color");

        int start = Funciones.buscarCaracter(numeracion_producto, '(');
        int end = Funciones.buscarCaracter(numeracion_producto, ')');

        numeracion_producto = numeracion_producto.substring(start + 1, end);
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
        LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayout_MainActivity);
        layout.requestFocus();
    }

    /**
     * Obtiene el ID del color actualmente seleccionado.
     * @return ID del color seleccionado en el spinner.
     */
    private int obtenerIDColor()
    {
        int actual_position = sp_color.getSelectedItemPosition();
        return Integer.parseInt(contenedor_colores.get(0).get(actual_position - 1));
    }

    /**
     * Obtener el ID del tipo actualmente seleccionado.
     * @return ID del tipo seleccionado en el spinner.
     */
    private int obtenerIDTipo()
    {
        int actual_position = sp_tipo.getSelectedItemPosition();
        return Integer.parseInt(contenedor_tipos.get(0).get(actual_position - 1));
    }

    /**
     * Obtiene el ID de la talla actualmente seleccionada en el Spinner
     * @return ID de la talal seleccionada.
     */
    private int obtenerIDTalla()
    {
        int actual_position = sp_talla.getSelectedItemPosition();
        return Integer.parseInt(contenedor_tallas.get(0).get(actual_position - 1));
    }

    /**
     * Obtiene los pares por tallas bajo un formato separado por comas.
     * @return Retorna los pares por tallas.
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
     * Carga los valores previamente descritos dentro del producto, como por ejemplo su imagen.
     */
    private void cargarValoresPrevios()
    {
        /* Imagen */
        ImageView imagen = (ImageView) findViewById(R.id.seleccion_img_producto_editar);

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
            imagen.setImageBitmap(Funciones.decodeSampledBitmapFromResource(file, 432, 324));
            //imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        else
        {
            Log.e(TAG, "La imagen no pudo ser localizada..");
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.img_notfound);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 432, 324, true));
            imagen.setImageDrawable(d);
            //imagen.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        loadSpinnerData();
        selectSpinnerData();

        EditText modelo = (EditText) findViewById(R.id.modelo_producto);
        modelo.setText(modelo_producto);
        EditText precio = (EditText) findViewById(R.id.precio_producto);
        precio.setText(precio_producto);

        Switch estatus = (Switch) findViewById(R.id.estatus_switch);
        estatus.setTextOn("Habilitado");
        estatus.setTextOff("Des-Habilitado");

        /*String numeracion = sp_talla.getSelectedItem().toString().trim().substring(1);
        numeracion = numeracion.replace("(", "").replace(")", "");
        int guionPos = Funciones.buscarCaracter(numeracion, '-');
        int minimo = Integer.parseInt(numeracion.substring(0, guionPos));
        int maximo = Integer.parseInt(numeracion.substring(guionPos + 1));
        int diferencia = maximo - minimo;*/

        //final LinearLayout bulto_numeracion = (LinearLayout) findViewById(R.id.bulto_numeracion);
        //final LinearLayout cabecera = (LinearLayout) findViewById(R.id.cabecera_bulto_numeracion);
        /*bulto_numeracion.removeAllViews();
        cabecera.removeAllViews();
        crearMiniTablaNumeracion(minimo, diferencia, true);*/

        if(estatus_producto.equals("1"))
            estatus.setChecked(true);
        else
            estatus.setChecked(false);
    }

    /**
     * Crea una tabla que contiene la numeracion del producto.
     * @param minimo Talla minima de la numeracion.
     * @param diferencia Diferencia entre el minimo y el maximo de las tallas de la numeracion.
     *                   Por ejemplo (34-26) = 8
     * @param lleno Booleano indicado si se deben llenar o no los valores dentro de la tabla. Se
     *              llena en la primera ejecucion con los datos de la BD, si estoy editando y cambie
     *              algun valor de la talla, le paso falso para que no me llene nada y el usuario
     *              pueda llenarlo a su gusto.
     */
    private void crearMiniTablaNumeracion(int minimo, int diferencia, boolean lleno)
    {
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        final LinearLayout bulto_numeracion = (LinearLayout) findViewById(R.id.bulto_numeracion);
        final LinearLayout cabecera = (LinearLayout) findViewById(R.id.cabecera_bulto_numeracion);
        String currentValue;

        bulto_numeracion.removeAllViews();
        cabecera.removeAllViews();
        ids_tabla = new ArrayList<>();
        int nextId = 0;

        String[] pxt = paresxtalla.split(",");

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

            if(lleno)
                componente.setText(pxt[i]);

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
     * Selecciona los valores que tendran seleccionados los spinners de color, tipo y talla.
     */
    private void selectSpinnerData()
    {
        int position = Funciones.buscarPosicionElemento(color_producto, sp_color);
        sp_color.setSelection(position);
        position = Funciones.buscarPosicionElemento(tipo_producto, sp_tipo);
        sp_tipo.setSelection(position);
        position = Funciones.buscarPosicionElemento(talla_producto + "(" + numeracion_producto + ")", sp_talla);
        sp_talla.setSelection(position);
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
        sp_tipo.setAdapter(dataAdapter);

        contenedor_tallas = manager.cargarListaTallas();
        dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, contenedor_tallas.get(1));
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp_talla.setAdapter(dataAdapter);
    }

    private boolean camposValidados()
    {
        Log.i(TAG, "Validando campos");

        if( modelo_seleccionado.getText().toString().trim().equals("") )
        {
            modelo_seleccionado.setError("Inserte un modelo!");
            return false;
        }
        else if ( sp_color.getSelectedItemPosition()==0 )
        {
            TextView errorText = (TextView) sp_color.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_color);//changes the selected item text to this
            return false;
        }
        else if ( precio_seleccionado.getText().toString().trim().equals("") )
        {
            precio_seleccionado.setError("Inserte un precio!");
            return false;
        }
        else if ( sp_tipo.getSelectedItemPosition()==0 )
        {
            TextView errorText = (TextView) sp_tipo.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_tipo);//changes the selected item text to this
            return false;
        }
        else if( sp_talla.getSelectedItemPosition()==0 )
        {
            TextView errorText = (TextView) sp_talla.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_talla);//changes the selected item text to this
            return false;
        }
        else if(!miniTablaEstaLlena())
        {
            Toast.makeText(contexto, "Indique la cantidad de pares por talla", Toast.LENGTH_LONG).show();
            return false;
        }
        else
        {
            modelo_seleccionado.setError(null);
            precio_seleccionado.setError(null);
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

    /**
     * Clase para la edicion en segundo plano de un producto en la BD
     */
    class async_editarProductoBD extends AsyncTask< String, String, String >
    {
        String id, talla, tipo, modelo, color, precio, numeracion, estatus, paresxtalla;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarProductoDetalles.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Editando el producto...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            id = params[0];
            talla = params[1];
            tipo = params[2];
            modelo = params[3];
            color = params[4];
            precio = params[5];
            numeracion = params[6];
            estatus = params[7];
            paresxtalla = params[8];

            try
            {
                //enviamos y recibimos y analizamos los datos en segundo plano.
                if (editarProducto(id, talla, tipo, modelo, color, precio, numeracion, estatus, paresxtalla))
                {
                    return "ok";
                }
                else
                {
                    Log.e(TAG, "err");
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
                Toast.makeText(contexto, "Producto editado exitosamente!!", Toast.LENGTH_LONG).show();

                // Redirige a la pantalla de Home
                Intent c = new Intent(EditarProductoDetalles.this, EditarProducto.class);
                c.putExtra("usuario", usuario);
                startActivity(c);

                // Prevent the user to go back to this activity
                EditarProducto.fa.finish();
                finish();
            }
            else
            {
                Toast.makeText(contexto, "Hubo un error editando el producto..", Toast.LENGTH_LONG).show();
            }
        }

        private boolean editarProducto(String id, String talla, String tipo, String modelo, String color, String precio, String numeracion, String estatus, String paresxtalla)
        {
            long id_producto = manager.editarProducto(id, talla, tipo, modelo, color, precio, numeracion, estatus, paresxtalla);

            // Si edite con exito el producto en BD
            if(id_producto!=-1)
            {
                // Si debo editar la imagen
                if(imagen_cargada != null)
                {
                    // Proceso a editar la imagen..
                    //noinspection RedundantIfStatement
                    if(eliminarImagen(modelo_producto) && guardarImagen(modelo.trim()))
                        return true;
                    else
                        return false;
                }
                // Debo editar el nombre de la imagen solamente
                else if ( !modelo_producto.equals( modelo_seleccionado.getText().toString().trim() ) )
                {
                    //noinspection RedundantIfStatement
                    if(modificarNombreImagen(modelo_producto, modelo_seleccionado.getText().toString().trim()))
                        return true;
                    else
                        return false;
                }
                // Si no debo editar la imagen
                else
                {
                    // Acabe con la edicion del producto
                    return true;
                }
            }
            return false;
        }

        private boolean modificarNombreImagen(String oldName, String newName)
        {
            Log.i(TAG, "Se modificare el nombre de la imagen "+oldName+" con el nombre de: " + newName);
            File imagesFolder;

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
                if(imagesFolder.exists())
                {
                    File from = new File(imagesFolder, oldName + Constantes.EXTENSION_IMG);
                    File to = new File(imagesFolder, newName + Constantes.EXTENSION_IMG);

                    if (from.renameTo(to))
                    {
                        Log.i(TAG, "File successfully renamed");
                    }
                    else
                    {
                        Log.e(TAG, "File could not be renamed");
                    }
                }

                return true;
            }

            return false;
        }

        private boolean guardarImagen(String nombre)
        {
            nombre = nombre.trim();
            Log.i(TAG, "Se guardara una imagen con el nombre de: "+nombre);
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

    /**
     * Funcion utilizada para la recepcion de informacion al momento de cambiar la imagen.
     * @param requestCode Codigo del request.
     * @param resultCode Codigo resultante del request.
     * @param data Data contenida.
     */
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
                ImageView imageView = (ImageView) findViewById(R.id.seleccion_img_producto_editar);
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

    /**
     * Funcion encargada de mostrar el activity de gestion de tipos.
     */
    private void mostrarGestionTipos()
    {
        Intent c = new Intent(EditarProductoDetalles.this, GestionTipos.class);
        c.putExtra("usuario",usuario);
        startActivity(c);
    }

    /**
     * Funcion encargada de mostrar el activity de gestion de tallas.
     */
    private void mostrarGestionTallas()
    {
        Intent c = new Intent(EditarProductoDetalles.this, GestionTallas.class);
        c.putExtra("usuario",usuario);
        startActivity(c);
    }

    /**
     * Funcion encargada de mostrar el activity de gestion de colores.
     */
    private void mostrarGestionColores()
    {
        Intent c = new Intent(EditarProductoDetalles.this, GestionColores.class);
        c.putExtra("usuario",usuario);
        startActivity(c);
    }
}