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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.gestion.productos.AgregarProducto;
import com.tufano.tufanomovil.global.Funciones;

/**
 * Created por Usuario Tufano on 15/01/2016.
 */
public class AgregarColor extends AppCompatActivity
{
    private Context contexto;
    private final String TAG = "AgregarColor";
    private DBAdapter manager;
    private ProgressDialog pDialog;
    private EditText color;
    private String usuario;
    private boolean desdeProductos;
    private String idCreado;
    private String idTipoCreado;
    private String idTallaCreado;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_color);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        getExtrasVar();
        createToolBar();
        noInitialFocus();
        initEditTexts();
        initButtons();
    }

    /**
     * Inicializa los editTexts
     */
    private void initEditTexts()
    {
        color = (EditText) findViewById(R.id.nombre_color_agregar);
        //color.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        color.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
    }

    /**
     * Inicializa los botones.
     */
    private void initButtons()
    {
        Button agregar = (Button) findViewById(R.id.btn_agregar_color);
        agregar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (camposValidados())
                {
                    color.setError(null);
                    final String newColor = Funciones.capitalizeWords(color.getText().toString().trim());
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AgregarColor.this);

                    dialog.setTitle(R.string.confirmacion_agregar_color);
                    dialog.setMessage("Se agregara el siguiente color: " + newColor);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            new async_agregarColorBD().execute(newColor);
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
                    color.setError("¡Este campo es obligatorio!");
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

        // Obtiene el usuario actualmente en el sistema
        usuario = bundle.getString("usuario");

        // Obtiene un valor booleano que indicara si este activity fue instanciado a traves del
        // activity de productos.
        desdeProductos = bundle.getBoolean("desdeProductos");

        idTipoCreado = bundle.getString("idTipoCreado");
        idTallaCreado = bundle.getString("idTallaCreado");
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.agregar_color);
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
     * Valida los campos antes de agregar el color.
     * @return True si los campos son correctos, false en caso contrario.
     */
    private boolean camposValidados()
    {
        return !color.getText().toString().trim().isEmpty();
    }

    /**
     * Clase para agregar en segundo plano un color a la BD.
     */
    class async_agregarColorBD extends AsyncTask< String, String, String >
    {
        String color;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(AgregarColor.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Agregando el color...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            color = params[0];

            try
            {
                long result = agregarColor(color);

                if ( result == -1 )
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
                    idCreado = String.valueOf(result);
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
                        Toast.makeText(contexto, "Tipo agregado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige
                        Intent c = new Intent(AgregarColor.this, AgregarProducto.class);
                        c.putExtra("usuario", usuario);
                        c.putExtra("idColorCreado", idCreado);
                        c.putExtra("desdeColor", true);
                        c.putExtra("idTipoCreado", idTipoCreado);
                        c.putExtra("idTallaCreado", idTallaCreado);
                        startActivity(c);
                        AgregarProducto.fa.finish();
                    }
                    else
                    {
                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Color agregado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige
                        Intent c = new Intent(AgregarColor.this, GestionColores.class);
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
                    Toast.makeText(contexto, "Hubo un error agregando el color..", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        /**
         * Metodo para agregar un color en la BD.
         * @param nombre Nombre del color a agregar, por ej "Blanco Perlado".
         * @return -1 en caso de error, -2 en caso de que el color ya exista, un valor positivo (ID)
         * si la operacion fue exitosa.
         */
        private long agregarColor(String nombre)
        {
            if( !existeColor(nombre))
            {
                // Agrega el color a la BD.
                long id_talla = manager.agregarColores(nombre);
                Log.d(TAG, "id_color: " + id_talla);
                return id_talla;
            }
            else
            {
                Log.e(TAG, "Ya existe dicho color!!");
                return -2;
            }
        }

        /**
         * Metodo encargado de verificar si existe un color dado en la BD, por medio de su nombre.
         * @param color Color que se quiere saber si existe
         * @return Devuelve true si el color ya existe con el nombre ingresado
         */
        private boolean existeColor(String color)
        {
            Cursor cursor = manager.cargarColores_nombre(color);
            return cursor.getCount() > 0;
        }
    }
}