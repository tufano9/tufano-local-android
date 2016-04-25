package com.tufano.tufanomovil.gestion.tipo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.Funciones;

/**
 * Created por Usuario Tufano on 15/01/2016.
 */
public class ConsultarTipos extends AppCompatActivity
{
    public static Activity  fa;
    private final int id_mensaje = Funciones.generateViewId();
    private       Context   contexto;
    private       DBAdapter manager;
    private       String    usuario;
    private String TAG = "ConsultarTipos";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_tipos);

        fa = this;
        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        getExtrasVar();
        createToolBar();
        initComponents();
        inicializarTabla();
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
     * Crea una barra de herramientas superior con un subtitulo definido.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.gestion_tipo_subtitulo);
        setSupportActionBar(toolbar);
    }

    /**
     * Inicializa los componentes primarios de la activity. En este caso el boton flotante para
     * agregar un tipo de producto.
     */
    private void initComponents()
    {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent c = new Intent(ConsultarTipos.this, AgregarTipo.class);
                c.putExtra("usuario", usuario);
                startActivity(c);
            }
        });
    }

    /**
     * Funcion encargada de inicializar y llenar de data la tabla generada dinamicamente.
     */
    private void inicializarTabla()
    {
        String TAG = "ConsultarTipos";
        Log.i(TAG, "Inicializando tabla..");
        final TableLayout tabla = (TableLayout) findViewById(R.id.table_gestion_tipos);

        TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        // Llenando la tabla de forma iterativa
        Cursor cursor = manager.cargarTipos();
        if (cursor.getCount() > 0)
        {
            mostrarTodo();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                Log.i(TAG, "Agregando fila..");
                TableRow fila = new TableRow(contexto);

                final String id_tipo        = String.valueOf(cursor.getInt(0));
                final String tipos_producto = cursor.getString(1);

                /* Tipos */
                TextView tipos = generarTextViewTipo(contexto, tipos_producto, params);

                /* Opciones */
                Button editar = generarButtonOpciones(contexto, id_tipo, tipos_producto);

                LinearLayout opciones = new LinearLayout(contexto);
                opciones.setGravity(Gravity.CENTER);
                opciones.setLayoutParams(params);
                opciones.addView(editar);
                //opciones.addView(eliminar);

                // Llenando la fila con data
                fila.setBackgroundColor(Color.WHITE);
                fila.addView(tipos);
                fila.addView(opciones);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    fila.setBackground(Funciones.intToDrawable(contexto, R.drawable.table_border));
                else
                    //noinspection deprecation
                    fila.setBackgroundDrawable(Funciones.intToDrawable(contexto,
                            R.drawable.table_border));

                tabla.addView(fila);
            }
        }
        else
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
                                ocultarTodo();
                                agregarMensaje(R.string.msj_tipo_vacio);

                                /*TextView mensaje = new TextView(contexto);
                                mensaje.setText(R.string.msj_tipo_vacio);
                                mensaje.setGravity(Gravity.CENTER);
                                mensaje.setTextSize(20f);

                                LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor);
                                contenedor.addView(mensaje);*/
                            }
                        });
                    }
                }
            };
            hilo.start();
        }

        cursor.close();
    }

    /**
     * Genera el boton de opciones que se incluira en la tabla.
     *
     * @param contexto       Contexto de la aplicacion.
     * @param id_tipo        ID del tipo actual.
     * @param tipos_producto Tipo de producto.
     * @return Boton con la accion correspondiente.
     */
    private Button generarButtonOpciones(Context contexto, final String id_tipo, final String tipos_producto)
    {
        Button editar = new Button(contexto);
        editar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent c = new Intent(ConsultarTipos.this, EditarTipo.class);
                c.putExtra("id_tipo", id_tipo);
                c.putExtra("tipos_producto", tipos_producto);
                startActivity(c);
            }
        });
        editar.setBackgroundResource(R.drawable.icn_edit);
        int                       edit_image_width  = 70;
        int                       edit_image_height = 70;
        LinearLayout.LayoutParams parms             = new LinearLayout.LayoutParams(edit_image_width, edit_image_height);
        editar.setLayoutParams(parms);
        editar.setPadding(2, 10, 2, 10);
        return editar;
    }

    /**
     * Genera un textView con el tipo de producto actual.
     *
     * @param contexto       Contexto de la aplicacion.
     * @param tipos_producto Tipo de producto.
     * @param params         Parametros de la tabla.
     * @return TextView armado con el tipo de producto actual.
     */
    private TextView generarTextViewTipo(Context contexto, String tipos_producto, TableRow.LayoutParams params)
    {
        TextView tipos = new TextView(contexto);
        tipos.setText(tipos_producto);
        tipos.setTextColor(Color.DKGRAY);
        tipos.setGravity(Gravity.CENTER);
        tipos.setLayoutParams(params);
        tipos.setTextSize(16f);
        return tipos;
    }

    private void agregarMensaje(final int msj)
    {
        final Thread hilo1 = new Thread()
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
                            Log.i(TAG, "Agregando mensaje");
                            TextView mensaje = new TextView(contexto);
                            mensaje.setText(msj);
                            mensaje.setGravity(Gravity.CENTER);
                            mensaje.setTextSize(20f);
                            mensaje.setId(id_mensaje);
                            mensaje.setLayoutParams(
                                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT));

                            LinearLayout contenedor = (LinearLayout) findViewById(R.id.contenedor_base);
                            contenedor.addView(mensaje);
                        }
                    });
                }
            }
        };
        hilo1.start();
    }

    /**
     * Funcion encargada de ocultar los elementos de la tabla.
     */
    private void ocultarTodo()
    {
        findViewById(R.id.contenedor).setVisibility(View.GONE);
    }

    /**
     * Funcion encargada de mostrar los elementos de la tabla.
     */
    private void mostrarTodo()
    {
        findViewById(R.id.contenedor).setVisibility(View.VISIBLE);
    }

    /*
    class async_eliminarTipoBD extends AsyncTask< String, String, String >
    {
        String id;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(ConsultarTipos.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Eliminando el tipo...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            id = params[0];

            try
            {
                //enviamos y recibimos y analizamos los datos en segundo plano.
                if (eliminarTipo(id))
                {
                    return "ok";
                }
                else
                {
                    Log.d(TAG, "err");
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
                Toast.makeText(contexto, "Tipo eliminado exitosamente!!", Toast.LENGTH_LONG).show();

                // Redirige
                Intent c = new Intent(ConsultarTipos.this, ConsultarTipos.class);
                startActivity(c);

                // Prevent the user to go back to this activity
                finish();
            }
            else
            {
                Toast.makeText(contexto, "Hubo un error eliminando el tipo..", Toast.LENGTH_LONG).show();
            }
        }

        private boolean eliminarTipo(String id)
        {
            Log.d(TAG, "Eliminar tipo con id: " + id);
            long filas_afectadas = manager.eliminarTipo(id);
            return filas_afectadas != 0;
        }
    }

    */
}