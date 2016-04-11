package com.tufano.tufanomovil.gestion.talla;

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
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.gestion.productos.AgregarProducto;
import com.tufano.tufanomovil.global.Funciones;

/**
 * Created por Usuario Tufano on 15/01/2016.
 */
public class EditarTalla extends AppCompatActivity
{
    private Context contexto;
    private final String TAG = "EditarTalla";
    private DBAdapter manager;
    private ProgressDialog pDialog;
    private String id_talla;
    private String numeraciones, talla_producto;
    private EditText talla;
    private NumberPicker lim_inf, lim_sup;
    private String usuario;
    private boolean desdeProductos;
    private String idTipoCreado;
    private String idColorCreado;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tallas);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        getExtrasVar();
        createToolBar();
        noInitialFocus();
        obtenerDatosTalla();
        initEditText();
        initSpinner();
        initButtons();
        cargarValoresPrevios();
    }

    /**
     * Inicializa los spinners
     */
    private void initSpinner()
    {
        lim_inf = (NumberPicker) findViewById(R.id.sp_numeracion_inf_editar);
        lim_sup = (NumberPicker) findViewById(R.id.sp_numeracion_sup_editar);

        lim_inf.setMinValue(1);
        lim_inf.setMaxValue(49);
        lim_sup.setMinValue(2);
        lim_sup.setMaxValue(50);

        lim_inf.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                lim_sup.setMinValue(newVal + 1);
            }
        });
    }

    /**
     * Inicializa los editText
     */
    private void initEditText()
    {
        talla = (EditText) findViewById(R.id.nombre_talla_editar);
        //talla.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        talla.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        InputFilter filter = new InputFilter()
        {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend)
            {
                if (source instanceof SpannableStringBuilder)
                {
                    SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
                    for (int i = end - 1; i >= start; i--)
                    {
                        char currentChar = source.charAt(i);
                        if (!Character.isLetterOrDigit(currentChar) && !Character.isSpaceChar(currentChar))
                        {
                            sourceAsSpannableBuilder.delete(i, i+1);
                        }
                    }
                    return source;
                }
                else
                {
                    StringBuilder filteredStringBuilder = new StringBuilder();
                    for (int i = start; i < end; i++)
                    {
                        char currentChar = source.charAt(i);
                        if (Character.isLetterOrDigit(currentChar) || Character.isSpaceChar(currentChar))
                        {
                            filteredStringBuilder.append(currentChar);
                        }
                    }
                    return filteredStringBuilder.toString();
                }
            }
        };

        talla.setFilters(new InputFilter[]{filter});
    }

    /**
     * Inicializa los botones
     */
    private void initButtons()
    {
        Button agregar = (Button) findViewById(R.id.btn_editar_talla);
        agregar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (camposValidados())
                {
                    talla.setError(null);
                    final String newTalla = Funciones.capitalizeWords(talla.getText().toString().trim());
                    AlertDialog.Builder dialog = new AlertDialog.Builder(EditarTalla.this);

                    dialog.setMessage(R.string.confirmacion_editar_talla);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String numeracion = "(" + lim_inf.getValue() + "-" + lim_sup.getValue() + ")";

                            new async_editarTallaBD().execute(id_talla, newTalla, numeracion);
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
                    Toast.makeText(contexto, "Por favor ingrese el nombre de la talla!!", Toast.LENGTH_LONG).show();
                    talla.setError("¡Este campo es obligatorio!");
                }
            }
        });
    }

    /**
     * Obtiene los datos de la talla a partir de su ID
     */
    private void obtenerDatosTalla()
    {
        Cursor cursor = manager.buscarTalla_ID(id_talla);

        if (cursor.moveToFirst())
        {
            talla_producto = cursor.getString(0);
            numeraciones = cursor.getString(1);
        }
        cursor.close();

        //talla_producto = bundle.getString("talla_producto");
        //numeraciones = bundle.getString("numeraciones");

        if (numeraciones != null)
        {
            numeraciones = numeraciones .replace("(", "") .replace(")", "");
        }
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        id_talla = bundle.getString("id_talla");
        usuario = bundle.getString("usuario");
        desdeProductos = bundle.getBoolean("desdeProductos");
        idTipoCreado = bundle.getString("idTipoCreado");
        idColorCreado = bundle.getString("idColorCreado");
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.gestion_talla_subtitulo);
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
     * Carga los valores que tiene actualmente la talla.
     */
    private void cargarValoresPrevios()
    {
        int posicion_guion = Funciones.buscarCaracter(numeraciones, '-');
        int min = Integer.parseInt(numeraciones.substring(0, posicion_guion));
        int max = Integer.parseInt(numeraciones.substring(posicion_guion+1));
        talla.setText( talla_producto );
        lim_inf.setValue(min);
        lim_sup.setValue(max);
    }

    /**
     * Valida los campos introducidos por el usuario antes de la edicion.
     * @return True si los campos estan correctos, False en caso contrario.
     */
    private boolean camposValidados()
    {
        return !talla.getText().toString().trim().isEmpty();
    }

    /**
     * Clase para editar una talla en BD bajo segundo plano.
     */
    class async_editarTallaBD extends AsyncTask< String, String, String >
    {
        String id, nombre, numeracion;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarTalla.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Editando la talla...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            id = params[0];
            nombre = params[1];
            numeracion = params[2];

            try
            {
                final long result = editarTalla(id, nombre, numeracion);

                // Si el campo de la talla no cambio
                boolean campo_cambio = !talla_producto.equals(obtenerTallaIngresada());

                if ( result == 0 ) // No hubieron modificaciones
                {
                    Log.d(TAG, "err");
                    return "err";
                }
                // Ya existe y el campo cambio, puede darse el caso de que exista pero el campo no
                // cambio, es decir, cuando solo modifique la numeracion pero deje intacto el nombre
                // de la talla..
                else if ( result == -2 && campo_cambio)
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
                    // producto y necesite editar una talla, presione el boton y me redirigio a esta
                    // activity, la cual al acabar el proceso me devolvera a mi activity de productos
                    // con la nueva talla editada y seleccionada.
                    if(desdeProductos)
                    {
                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Talla editada exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige
                        Intent c = new Intent(EditarTalla.this, AgregarProducto.class);
                        c.putExtra("usuario", usuario);
                        c.putExtra("idTallaCreado", id_talla);
                        c.putExtra("desdeTalla", true);
                        c.putExtra("idTipoCreado", idTipoCreado);
                        c.putExtra("idColorCreado", idColorCreado);
                        startActivity(c);
                        AgregarProducto.fa.finish();
                    }
                    // Entre al activity por medio del menu emergente superior de la app
                    else
                    {
                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Talla editada exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige
                        Intent c = new Intent(EditarTalla.this, GestionTallas.class);
                        startActivity(c);
                        GestionTallas.fa.finish();
                    }

                    // Prevent the user to go back to this activity
                    finish();
                    break;
                case "existente":
                    Toast.makeText(contexto, "Talla existente, por favor indique otro nombre..", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(contexto, "Hubo un error editadando la talla..", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        private String obtenerTallaIngresada()
        {
            return talla.getText().toString();
        }

        /**
         * Funcion encargada de editar la talla en BD
         * @param id ID de la talla a editar
         * @param nombre Nombre de la talla con el cual se editara.
         * @param numeracion Numeracion que sustituira a la antigua.
         * @return -2 si la talla ya existe, -1 si ocurrio un error, o un valor positivo si la
         * operacion fue exitosa.
         */
        private long editarTalla(String id, String nombre, String numeracion)
        {
            if( !existeTalla(nombre, id))
            {
                long id_talla = manager.editarTallas(id, nombre, numeracion);
                Log.d(TAG, "Columnas modificadas: " + id_talla);
                return id_talla;
            }
            else
            {
                Log.e(TAG, "Ya existe dicha talla!!");
                return -2;
            }
        }

        /**
         * Comprueba que la talla no exista en la BD.
         * @param nombre Talla que se quiere saber si existe
         * @return Devuelve true si la talla ya existe con el nombre ingresado y un ID distinto.
         * Es decir, no toma en cuenta la talla editada actualmente.
         */
        private boolean existeTalla(String nombre, String id)
        {
            Cursor cursor = manager.cargarTallas_nombreID(nombre, id);
            return cursor.getCount() > 0;
        }
    }
}