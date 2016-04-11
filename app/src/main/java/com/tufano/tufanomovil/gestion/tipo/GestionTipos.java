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
public class GestionTipos extends AppCompatActivity
{
    private Context contexto;
    private DBAdapter manager;
    public static Activity fa;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_tipos);

        fa = this;
        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        createToolBar();
        initComponents();
        inicializarTabla();
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
                Intent c = new Intent(GestionTipos.this, AgregarTipo.class);
                startActivity(c);
            }
        });
    }

    /**
     * Funcion encargada de inicializar y llenar de data la tabla generada dinamicamente.
     */
    private void inicializarTabla()
    {
        String TAG = "GestionTipos";
        Log.i(TAG, "Inicializando tabla..");
        final TableLayout tabla = (TableLayout) findViewById(R.id.table_gestion_tipos);

        TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        // Llenando la tabla de forma iterativa
        Cursor cursor = manager.cargarTipos();
        if(cursor.getCount()>0)
        {
            mostrarTodo(tabla);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                Log.i(TAG, "Agregando fila..");
                TableRow fila = new TableRow(contexto);

                final String id_tipo = String.valueOf(cursor.getInt(0));
                final String tipos_producto = cursor.getString(1);

                /* Tipos */
                TextView tipos = generarTextViewTipo(contexto, tipos_producto, params);

                /* Opciones */
                Button editar = generarButtonOpciones(contexto, id_tipo, tipos_producto);

                /*final Button eliminar = new Button(contexto);

                eliminar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Log.i(TAG, "Eliminar presionado");
                        AlertDialog.Builder dialog = new AlertDialog.Builder(GestionTipos.this);

                        dialog.setTitle(R.string.confirmacion_eliminar_tipo);
                        dialog.setMessage("Se eliminará el siguiente tipo: " + tipos_producto);
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new async_eliminarTipoBD().execute(id_tipo);
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
                });
                eliminar.setBackgroundResource(android.R.drawable.ic_menu_delete);
                eliminar.setLayoutParams(new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                */

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
                                ocultarTodo(tabla);

                                TextView mensaje = new TextView(contexto);
                                mensaje.setText(R.string.msj_tipo_vacio);
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

        cursor.close();
    }

    /**
     * Genera el boton de opciones que se incluira en la tabla.
     * @param contexto Contexto de la aplicacion.
     * @param id_tipo ID del tipo actual.
     * @param tipos_producto Tipo de producto.
     * @return Boton con la accion correspondiente.
     */
    private Button generarButtonOpciones(Context contexto, final String id_tipo, final String tipos_producto)
    {
        Button editar = new Button(contexto);
        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent c = new Intent(GestionTipos.this, EditarTipo.class);
                c.putExtra("id_tipo", id_tipo);
                c.putExtra("tipos_producto", tipos_producto);
                startActivity(c);
            }
        });
        editar.setBackgroundResource(R.drawable.icn_edit);
        int edit_image_width = 70;
        int edit_image_height = 70;
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(edit_image_width, edit_image_height);
        editar.setLayoutParams(parms);
        editar.setPadding(2, 10, 2, 10);
        return editar;
    }

    /**
     * Genera un textView con el tipo de producto actual.
     * @param contexto Contexto de la aplicacion.
     * @param tipos_producto Tipo de producto.
     * @param params Parametros de la tabla.
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

    /**
     * Muestra la tabla
     * @param tabla Tabla a mostrar.
     */
    private void mostrarTodo(TableLayout tabla)
    {
        tabla.setVisibility(View.VISIBLE);
    }

    /**
     * Oculta la tabla para mostrar algun mensaje.
     * @param tabla Tabla a ocultar.
     */
    private void ocultarTodo(TableLayout tabla)
    {
        tabla.setVisibility(View.INVISIBLE);
    }

    /*
    class async_eliminarTipoBD extends AsyncTask< String, String, String >
    {
        String id;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(GestionTipos.this);
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
                Intent c = new Intent(GestionTipos.this, GestionTipos.class);
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