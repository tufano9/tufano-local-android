package com.tufano.tufanomovil.gestion.colores;

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
public class GestionColores extends AppCompatActivity
{
    private Context contexto;
    private final String TAG = "GestionColores";
    private DBAdapter manager;
    //private ProgressDialog pDialog;
    public static Activity fa;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_colores);

        fa = this;
        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        createToolBar();
        initButtons();
        inicializarTabla();
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.gestion_color_subtitulo);
        setSupportActionBar(toolbar);
    }

    /**
     * Inicializa los botones
     */
    private void initButtons()
    {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent c = new Intent(GestionColores.this, AgregarColor.class);
                startActivity(c);
            }
        });
    }

    /**
     * Metodo principal para gestionar la tabla de colores.
     */
    private void inicializarTabla()
    {
        Log.i(TAG, "Inicializando tabla..");
        final TableLayout tabla = (TableLayout) findViewById(R.id.table_gestion_colores);

        TableRow.LayoutParams params = new TableRow.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        // Llenando la tabla de forma iterativa
        Cursor cursor = manager.cargarColores();
        if(cursor.getCount()>0)
        {
            mostrarTodo(tabla);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
            {
                Log.i(TAG, "Agregando fila..");
                TableRow fila = new TableRow(contexto);

                final String id_color = String.valueOf(cursor.getInt(0));
                final String nombre_color = cursor.getString(1);

                /* Tipos */
                TextView color = new TextView(contexto);
                color.setText(nombre_color);
                color.setTextColor(Color.DKGRAY);
                color.setGravity(Gravity.CENTER);
                color.setLayoutParams(params);
                color.setTextSize(16f);

                /* Opciones */
                Button editar = new Button(contexto);
                editar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent c = new Intent(GestionColores.this, EditarColor.class);
                        c.putExtra("id_color", id_color);
                        c.putExtra("nombre_color", nombre_color);
                        startActivity(c);
                    }
                });
                editar.setBackgroundResource(R.drawable.icn_edit);
                int edit_image_width = 70;
                int edit_image_height = 70;
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(edit_image_width, edit_image_height);
                editar.setLayoutParams(parms);
                editar.setPadding(2, 10, 2, 10);
                /*final Button eliminar = new Button(contexto);

                eliminar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(GestionColores.this);

                        dialog.setTitle(R.string.confirmacion_eliminar_color);
                        dialog.setMessage("Se eliminará el siguiente color: " + nombre_color);
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new async_eliminarColorBD().execute(id_color);
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
                fila.addView(color);
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
                                mensaje.setText(R.string.msj_color_vacio);
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
     * Muestra todos los componentes de la tabla.
     * @param tabla Tabla a la cual se le haran visibles los componentes
     */
    private void mostrarTodo(TableLayout tabla)
    {
        tabla.setVisibility(View.VISIBLE);
    }

    /**
     * Oculta todos los componentes de la tabla.
     * @param tabla Tabla a la cual se le haran invisibles los componentes
     */
    private void ocultarTodo(TableLayout tabla)
    {
        tabla.setVisibility(View.INVISIBLE);
    }

    /*

    class async_eliminarColorBD extends AsyncTask< String, String, String >
    {
        String id;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(GestionColores.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Eliminando el color...");
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
                if (eliminarColor(id))
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
                Toast.makeText(contexto, "Color eliminado exitosamente!!", Toast.LENGTH_LONG).show();

                // Redirige
                Intent c = new Intent(GestionColores.this, GestionColores.class);
                startActivity(c);

                // Prevent the user to go back to this activity
                finish();
            }
            else
            {
                Toast.makeText(contexto, "Hubo un error eliminando el color..", Toast.LENGTH_LONG).show();
            }
        }

        private boolean eliminarColor(String id)
        {
            Log.d(TAG, "Eliminar color con id: " + id);
            long filas_afectadas = manager.eliminarColor(id);
            return filas_afectadas != 0;
        }
    }

    */
}
