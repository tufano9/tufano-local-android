package com.tufano.tufanomovil.gestion.tipo;

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
public class AgregarTipo extends AppCompatActivity
{
    private Context contexto;
    private final String TAG = "AgregarTipo";
    private DBAdapter manager;
    private ProgressDialog pDialog;
    private EditText tipo;
    private boolean desdeProductos;
    private String usuario;
    private String idCreado;
    private String idTallaCreado;
    private String idColorCreado;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_tipo);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        createToolBar();
        getExtrasVar();
        initComponents();
        noInitialFocus();
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        usuario = bundle.getString("usuario");
        desdeProductos = bundle.getBoolean("desdeProductos");
        idTallaCreado = bundle.getString("idTallaCreado");
        idColorCreado = bundle.getString("idColorCreado");
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.agregar_cliente_subtitulo);
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
     * Inicializa los componentes primarios de la activity como el EditText y el boton.
     */
    private void initComponents()
    {
        tipo = (EditText) findViewById(R.id.nombre_tipo_agregar);
        //tipo.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        tipo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        Button agregar = (Button) findViewById(R.id.btn_agregar_tipo);
        agregar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (camposValidados())
                {
                    tipo.setError(null);
                    final String newTipo = Funciones.capitalizeWords(tipo.getText().toString().trim());
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AgregarTipo.this);

                    dialog.setTitle(R.string.confirmacion_agregar_tipo);
                    dialog.setMessage("Se agregara el siguiente tipo: " + newTipo);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            new async_agregarTipoBD().execute(newTipo);
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
                    Toast.makeText(contexto, "Por favor ingrese el nombre del tipo!!", Toast.LENGTH_LONG).show();
                    tipo.setError("¡Este campo es obligatorio!");
                }
            }
        });
    }

    /**
     * Verifica que los campos introducidos por el usuario esten correctos.
     * @return True si los campos estan correctos, False en caso contrario.
     */
    private boolean camposValidados()
    {
        return !tipo.getText().toString().trim().isEmpty();
    }

    /**
     * Clase para agregar en segundo plano un tipo de producto a la base de datos.
     */
    class async_agregarTipoBD extends AsyncTask< String, String, String >
    {
        String nombre;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(AgregarTipo.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Agregando el tipo...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            nombre = params[0];

            try
            {
                long result = agregarTipo(nombre);

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
                    // Guardo el id recien creado (Tipo)
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
                    // Si viene desde el activity de productos, quiere decir que estaba agregando un
                    // producto y necesite agregar un tipo, presione el boton y me redirigio a esta
                    // activity, la cual al acabar el proceso me devolvera a mi activity de productos
                    // con el nuevo tipo agregado y seleccionado.
                    if(desdeProductos)
                    {
                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Tipo agregado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige al activity de productos con los datos..
                        Intent c = new Intent(AgregarTipo.this, AgregarProducto.class);
                        c.putExtra("usuario", usuario);
                        c.putExtra("idTipoCreado", idCreado);
                        c.putExtra("desdeTipo", true);
                        c.putExtra("idTallaCreado", idTallaCreado);
                        c.putExtra("idColorCreado", idColorCreado);
                        startActivity(c);
                        AgregarProducto.fa.finish();
                    }
                    // Entre a esta activity por medio del menu emergente superior.
                    else
                    {
                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Tipo agregado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige al menu de tipos
                        Intent c = new Intent(AgregarTipo.this, GestionTipos.class);
                        startActivity(c);
                        GestionTipos.fa.finish();
                    }

                    // Prevent the user to go back to this activity
                    finish();
                    break;
                case "existente":
                    Toast.makeText(contexto, "Tipo existente, por favor indique otro nombre..", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(contexto, "Hubo un error agregando el tipo..", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        /**
         * Funcion encargada de agregar un tipo de productos a la BD
         * @param nombre Nombre del tipo de producto. Por ej Torera
         * @return El ID del tipo de producto creado, -1 si ocurrio un error, -2 si ya existe el tipo.
         */
        private long agregarTipo(String nombre)
        {
            if( !existeTipo(nombre))
            {
                long id_talla = manager.agregarTipos(nombre);
                Log.d(TAG, "id_talla: " + id_talla);
                return id_talla;
            }
            else
            {
                Log.e(TAG, "Ya existe dicho tipo!!");
                return -2;
            }
        }

        /**
         * Verifica si el tipo de producto ya existe en la BD.
         * @param nombre Talla que se quiere saber si existe
         * @return Devuelve true si la tipo ya existe con el nombre ingresado.
         */
        private boolean existeTipo(String nombre)
        {
            Cursor cursor = manager.cargarTipos_nombre(nombre);
            return cursor.getCount() > 0;
        }
    }
}