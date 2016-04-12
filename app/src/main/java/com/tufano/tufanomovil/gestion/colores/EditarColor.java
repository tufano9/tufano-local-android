package com.tufano.tufanomovil.gestion.colores;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.gestion.productos.AgregarProducto;
import com.tufano.tufanomovil.global.Funciones;

/**
 * Created por Usuario Tufano on 15/01/2016.
 */
public class EditarColor extends AppCompatActivity
{
    private Context contexto;
    private final String TAG = "EditarColor";
    private DBAdapter manager;
    private ProgressDialog pDialog;
    private EditText nombre_color_editar;
    private String id_color, colores_producto;
    private String usuario;
    private boolean desdeProductos;
    private String idTipoCreado;
    private String idTallaCreado;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_colores);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        getExtrasVar();
        createToolBar();
        obtenerNombreColor();
        initButtons();
        initEditText();
        cargarValoresPrevios();
    }

    /**
     * Obtiene el nombre del color por medio de su ID
     */
    private void obtenerNombreColor()
    {
        Cursor cursor = manager.buscarColor_ID(id_color);

        if (cursor.moveToFirst())
        {
            colores_producto = cursor.getString(0);
        }
        cursor.close();
    }

    /**
     * Inicializa los editTexts
     */
    private void initEditText()
    {
        nombre_color_editar = (EditText) findViewById(R.id.nombre_color_editar);
        // La siguiente linea es para devolver todas las letras a mayusculas.
        //nombre_color_editar.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        nombre_color_editar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
    }

    /**
     * Inicializa los botones
     */
    private void initButtons()
    {
        Button btn_editar_color = (Button) findViewById(R.id.btn_editar_color);
        btn_editar_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (camposValidados())
                {
                    nombre_color_editar.setError(null);
                    final String newColor = Funciones.capitalizeWords(nombre_color_editar.getText().toString().trim());
                    AlertDialog.Builder dialog = new AlertDialog.Builder(EditarColor.this);

                    dialog.setTitle(R.string.confirmacion_editar_color);
                    dialog.setMessage("Se editara el color: \"" + colores_producto+"\" a: \"" + newColor+"\"");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            new async_editarColorBD().execute(id_color, newColor);
                        }
                    });

                    dialog.setNegativeButton("NO", new DialogInterface.OnClickListener()
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
                    Toast.makeText(contexto, "Por favor ingrese el nombre del color!!", Toast.LENGTH_LONG).show();
                    nombre_color_editar.setError("¡Este campo es obligatorio!");
                }
            }
        });
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.editar_color);
        setSupportActionBar(toolbar);
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        id_color = bundle.getString("id_color");
        usuario = bundle.getString("usuario");
        desdeProductos = bundle.getBoolean("desdeProductos");
        idTipoCreado = bundle.getString("idTipoCreado");
        idTallaCreado = bundle.getString("idTallaCreado");
    }

    /**
     * Carga la data real actualmente utilizada por el color.
     */
    private void cargarValoresPrevios()
    {
        nombre_color_editar.setText(colores_producto);
    }

    /**
     * Valida los campos antes de editar el color.
     * @return True si los campos son correctos, false en caso contrario.
     */
    private boolean camposValidados()
    {
        return !nombre_color_editar.getText().toString().trim().isEmpty();
    }

    /**
     * Clase para editar en segundo plano un color en la BD.
     */
    class async_editarColorBD extends AsyncTask< String, String, String >
    {
        String id, nombre;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarColor.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Editando el color...");
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
                final long result = editarColor(id, nombre);

                //boolean campo_cambio = !talla_producto.equals(obtenerTipoIngresado());

                if ( result == 0 ) // No hubieron modificaciones
                {
                    Log.d(TAG, "err");
                    return "err";
                }
                else if ( result == -2 )
                {
                    Log.d(TAG, "existente");
                    return "existente";
                }
                else
                {
                    return "ok";
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

            switch (result)
            {
                case "ok":
                    if(desdeProductos)
                    {
                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Color editado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige
                        Intent c = new Intent(EditarColor.this, AgregarProducto.class);
                        c.putExtra("usuario", usuario);
                        c.putExtra("idColorCreado", id_color);
                        c.putExtra("desdeColor", true);
                        c.putExtra("idTipoCreado", idTipoCreado);
                        c.putExtra("idTallaCreado", idTallaCreado);
                        startActivity(c);
                        AgregarProducto.fa.finish();
                    }
                    else
                    {
                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Color editado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige
                        Intent c = new Intent(EditarColor.this, GestionColores.class);
                        startActivity(c);
                        GestionColores.fa.finish();
                    }

                    // Prevent the user to go back to this activity
                    finish();
                    break;
                case "existente":
                    Toast.makeText(contexto, "Color existente, por favor indique otro nombre..", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(contexto, "Hubo un error editadando el color..", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        /**
         * Edita un color en la BD.
         * @param id ID del color a editar.
         * @param nombre Nombre del color que reemplazara el antiguo.
         * @return -1 si hubo un error, -2 si el color ya existe, un valor positivo (ID) en caso
         * de una operacion exitosa.
         */
        private long editarColor(String id, String nombre)
        {
            if( !existeColor(nombre, id))
            {
                long id_color = manager.editarColores(id, nombre);
                Log.d(TAG, "Columnas modificadas: " + id_color);
                return id_color;
            }
            else
            {
                Log.e(TAG, "Ya existe dicho color!!");
                return -2;
            }
        }

        /*private String obtenerTipoIngresado()
        {
            return nombre_tipo_editar.getText().toString();
        }*/

        /**
         * Verifica si existe el color en la base de datos, tomando en cuenta el nombre insertado.
         * @param nombre Color que se quiere saber si existe
         * @return Devuelve true si el color ya existe con el nombre ingresado
         */
        private boolean existeColor(String nombre, String id)
        {
            Cursor cursor = manager.cargarColores_nombreID(nombre, id);
            return cursor.getCount() > 0;
        }
    }
}