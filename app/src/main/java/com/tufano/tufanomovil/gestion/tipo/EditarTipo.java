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
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.gestion.productos.AgregarProducto;
import com.tufano.tufanomovil.global.Funciones;

/**
 * Created por Usuario Tufano on 15/01/2016.
 */
public class EditarTipo extends AppCompatActivity
{
    private Context contexto;
    private final String TAG = "EditarTipo";
    private DBAdapter manager;
    private ProgressDialog pDialog;
    private String id_tipo, tipos_producto;
    private EditText nombre_tipo_editar;
    private boolean desdeProductos;
    private String usuario;
    private String idTallaCreado;
    private String idColorCreado;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tipos);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        createToolBar();
        getExtrasVar();
        obtenerTiposProductos();
        initButtons();
        initTextViews();
        cargarValoresPrevios();
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
     * Obtiene los tipos de productos a partir del id del tipo.
     */
    private void obtenerTiposProductos()
    {
        Cursor cursor = manager.buscarTipo_ID(id_tipo);
        if (cursor.moveToFirst()) tipos_producto = cursor.getString(0);
        cursor.close();
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        id_tipo = bundle.getString("id_tipo");
        usuario = bundle.getString("usuario");
        desdeProductos = bundle.getBoolean("desdeProductos");
        idTallaCreado = bundle.getString("idTallaCreado");
        idColorCreado = bundle.getString("idColorCreado");
    }

    /**
     * Inicializa los TextViews
     */
    private void initTextViews()
    {
        nombre_tipo_editar = (EditText) findViewById(R.id.nombre_tipo_editar);

        // Solo Letras y Palabras con Capitalize
        nombre_tipo_editar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        // Para que todos los caracteres sean mayusculas.
        //nombre_tipo_editar.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
    }

    /**
     * Inicializa los botones
     */
    private void initButtons()
    {
        Button btn_editar_tipo = (Button) findViewById(R.id.btn_editar_tipo);
        btn_editar_tipo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (camposValidados())
                {
                    nombre_tipo_editar.setError(null);
                    final String newTipo = Funciones.capitalizeWords(nombre_tipo_editar.getText().toString().trim());
                    AlertDialog.Builder dialog = new AlertDialog.Builder(EditarTipo.this);

                    dialog.setTitle(R.string.confirmacion_editar_tipo);
                    dialog.setMessage("Se editara el tipo: \"" + tipos_producto + "\" a: \"" + newTipo + "\"");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            new async_editarTipoBD().execute(id_tipo, newTipo);
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
                    nombre_tipo_editar.setError("¡Este campo es obligatorio!");
                }
            }
        });
    }

    /**
     * Carga los valores actuales del tipo de producto (Antes de su posible edicion)
     */
    private void cargarValoresPrevios()
    {
        nombre_tipo_editar.setText(tipos_producto);
    }

    /**
     * Verifica que los campos introducidos por el usuario esten validados
     * @return True si los datos fueron validados exitosamente, False en caso contrario.
     */
    private boolean camposValidados()
    {
        return !nombre_tipo_editar.getText().toString().trim().isEmpty();
    }

    /**
     * Clase para editar en segundo plano un tipo de productos
     */
    class async_editarTipoBD extends AsyncTask< String, String, String >
    {
        String id, nombre;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarTipo.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Editando el tipo...");
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
                final long result = editarTipo(id, nombre);

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
                    // Si viene desde el activity de productos, quiere decir que estaba agregando un
                    // producto y necesite editar un tipo, presione el boton y me redirigio a esta
                    // activity, la cual al acabar el proceso me devolvera a mi activity de productos
                    // con el nuevo tipo editado y seleccionado.
                    if(desdeProductos)
                    {
                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Tipo editado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige al activity de agregar productos
                        Intent c = new Intent(EditarTipo.this, AgregarProducto.class);
                        c.putExtra("usuario", usuario);
                        c.putExtra("idTipoCreado", id_tipo);
                        c.putExtra("desdeTipo", true);
                        c.putExtra("idTallaCreado", idTallaCreado);
                        c.putExtra("idColorCreado", idColorCreado);
                        startActivity(c);
                        AgregarProducto.fa.finish();
                    }
                    // Entre al activity por medio del menu emergente superior de la app
                    else
                    {
                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Tipo editado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige al menu de gestion de tipos
                        Intent c = new Intent(EditarTipo.this, GestionTipos.class);
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
                    Toast.makeText(contexto, "Hubo un error editadando el tipo..", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        /**
         * Edita el tipo indicado bajo los parametros.
         * @param id ID del tipo de producto a editar.
         * @param nombre Nombre con el cual se quiere editar el tipo de producto.
         * @return -1 si ocurrio un error, -2 si ya existe el tipo, valor positivo en caso exitoso.
         */
        private long editarTipo(String id, String nombre)
        {
            if( !existeTipo(nombre, id))
            {
                long id_talla = manager.editarTipos(id, nombre);
                Log.d(TAG, "Columnas modificadas: " + id_talla);
                return id_talla;
            }
            else
            {
                Log.e(TAG, "Ya existe dicho tipo!!");
                return -2;
            }
        }

        /*private String obtenerTipoIngresado()
        {
            return nombre_tipo_editar.getText().toString();
        }*/

        /**
         * Verifica si el tipo de producto ya existe con el nombre indicado y con un id distinto.
         * @param nombre Talla que se quiere saber si existe
         * @return Devuelve true si la talla ya existe con el nombre ingresado y un id distinto al
         * señalado.
         */
        private boolean existeTipo(String nombre, String id)
        {
            Cursor cursor = manager.cargarTipos_nombreID(nombre, id);
            return cursor.getCount() > 0;
        }
    }
}