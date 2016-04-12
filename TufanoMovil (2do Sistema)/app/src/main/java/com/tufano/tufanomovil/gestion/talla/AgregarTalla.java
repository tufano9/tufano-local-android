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
public class AgregarTalla extends AppCompatActivity
{
    private Context contexto;
    private final String TAG = "AgregarTalla";
    private DBAdapter manager;
    private ProgressDialog pDialog;
    private EditText talla;
    private NumberPicker lim_inf, lim_sup;
    private String usuario;
    private boolean desdeProductos;
    private String idCreado;
    private String idTipoCreado;
    private String idColorCreado;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_tallas);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        getExtrasVar();
        createToolBar();
        noInitialFocus();
        initComponents();
        initButtons();
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
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
     * Inicializa el boton para agregar la talla
     */
    private void initButtons()
    {
        Button agregar = (Button) findViewById(R.id.btn_agregar_talla);
        agregar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (camposValidados())
                {
                    talla.setError(null);
                    final String newTalla = Funciones.capitalizeWords(talla.getText().toString().trim());
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AgregarTalla.this);

                    dialog.setMessage(R.string.confirmacion_agregar_talla);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String numeracion = "(" + lim_inf.getValue() + "-" + lim_sup.getValue() + ")";

                            new async_agregarTallaBD().execute(newTalla, numeracion);
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
     * Metodo encargada de inicializar los componentes.
     */
    private void initComponents()
    {
        talla = (EditText) findViewById(R.id.nombre_talla_agregar);
        //talla.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        talla.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        lim_inf = (NumberPicker) findViewById(R.id.sp_numeracion_inf);
        lim_sup = (NumberPicker) findViewById(R.id.sp_numeracion_sup);

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

        // Coloca un filtro que impide valores que no sean numericos, no permite simbolos.
        talla.setFilters(new InputFilter[] { filter });

        lim_inf.setMinValue(1);
        lim_inf.setMaxValue(49);
        lim_sup.setMinValue(2);
        lim_sup.setMaxValue(50);

        lim_inf.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                lim_sup.setMinValue(newVal+1);
            }
        });
    }

    /**
     * Funcion encargada de validar los datos introducidos por el usuario en el campo.
     * @return True si los campos estan correctos, False en caso contrario.
     */
    private boolean camposValidados()
    {
        return !talla.getText().toString().trim().isEmpty();
    }

    /**
     * Clase encargada de agregar en segundo plano la talla a la BD.
     */
    class async_agregarTallaBD extends AsyncTask< String, String, String >
    {
        String nombre, numeracion;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(AgregarTalla.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Agregando la talla...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            nombre = params[0];
            numeracion = params[1];

            try
            {
                long result = agregarTalla(nombre, numeracion);

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
                    // Si viene desde el activity de productos, quiere decir que estaba agregando un
                    // producto y necesite agregar una talla, presione el boton y me redirigio a esta
                    // activity, la cual al acabar el proceso me devolvera a mi activity de productos
                    // con la nueva talla agregada y seleccionada.
                    if(desdeProductos)
                    {
                        // Muestra al usuario un mensaje de operacion exitosa
                        Toast.makeText(contexto, "Tipo agregado exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige
                        Intent c = new Intent(AgregarTalla.this, AgregarProducto.class);
                        c.putExtra("usuario", usuario);
                        c.putExtra("idTallaCreado", idCreado);
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
                        Toast.makeText(contexto, "Talla agregada exitosamente!!", Toast.LENGTH_LONG).show();

                        // Redirige
                        Intent c = new Intent(AgregarTalla.this, GestionTallas.class);
                        //c.putExtra("usuario",usuario);
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
                    Toast.makeText(contexto, "Hubo un error agregando la talla..", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        private long agregarTalla(String nombre, String numeracion)
        {
            if( !existeTalla(nombre))
            {
                long id_talla = manager.agregarTallas(nombre, numeracion);
                Log.d(TAG, "id_talla: " + id_talla);
                return id_talla;
            }
            else
            {
                Log.e(TAG, "Ya existe dicha talla!!");
                return -2;
            }
        }

        /**
         *
         * @param nombre Talla que se quiere saber si existe
         * @return Devuelve true si la talla ya existe con el nombre ingresado
         */
        private boolean existeTalla(String nombre)
        {
            Cursor cursor = manager.cargarTallas_nombre(nombre);
            return cursor.getCount() > 0;
        }
    }
}