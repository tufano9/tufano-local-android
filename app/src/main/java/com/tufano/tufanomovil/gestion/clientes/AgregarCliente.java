package com.tufano.tufanomovil.gestion.clientes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.gestion.pedidos.SeleccionarCliente;
import com.tufano.tufanomovil.global.Funciones;

/**
 * Created por Usuario Tufano on 19/01/2016.
 */
public class AgregarCliente extends AppCompatActivity
{
    private final String TAG = "AgregarProductoPedido";
    private String usuario;
    private boolean desdePedidos;
    private Context contexto;
    private ProgressDialog pDialog;
    private DBAdapter manager;
    private Spinner sp_rif, sp_estado;
    private EditText razon_social, rif1, rif2, telefono, email, direccion;
    private String idCreado;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_clientes);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        getExtrasVar();
        createToolBar();
        noInitialFocus();
        initComponents();
        initButtons();
        initListeners();
        loadSpinnerData();
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
        // activity de pedidos.
        desdePedidos = bundle.getBoolean("desdePedidos");
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
     * Inicializa los listeners.
     */
    private void initListeners()
    {
        rif1.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                Log.d(TAG, "LENGTH: " + rif1.getText().toString().length());
                if (rif1.getText().toString().length() == 8)
                    rif2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
    }

    /**
     * Inicializa los botones.
     */
    private void initButtons()
    {
        Button btn_agregar = (Button) findViewById(R.id.btn_agregar_cliente);
        btn_agregar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (camposValidados())
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AgregarCliente.this);

                    dialog.setMessage(R.string.confirmacion_agregar_cliente);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Si", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            agregarCliente();
                        }
                    });

                    dialog.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                }
            }
        });
    }

    /**
     * Inicializa los componentes primarios de la activity
     */
    private void initComponents()
    {
        sp_rif = (Spinner) findViewById(R.id.sp_rif);
        sp_estado = (Spinner) findViewById(R.id.sp_estados);
        razon_social = (EditText) findViewById(R.id.et_razon_social);
        rif1 = (EditText) findViewById(R.id.et_rif1);
        rif2 = (EditText) findViewById(R.id.et_rif2);
        telefono = (EditText) findViewById(R.id.et_telefono);
        email = (EditText) findViewById(R.id.et_email);
        direccion = (EditText) findViewById(R.id.et_direccion);
    }

    /**
     * Agrega un cliente en la BD. Los datos seran obtenidos automaticamente desde los campos.
     */
    private void agregarCliente()
    {
        String rs = Funciones.capitalizeWords(razon_social.getText().toString().trim());
        String rif = sp_rif.getSelectedItem().toString() + rif1.getText().toString().trim() + "-" +
                rif2.getText().toString().trim();
        String estado = Funciones.capitalizeWords(sp_estado.getSelectedItem().toString().trim());
        String tlf = telefono.getText().toString().trim();
        String mail = email.getText().toString().trim();
        String dir = Funciones.capitalizeWords(direccion.getText().toString().trim());
        new async_crearClienteBD().execute(rs, rif, estado, tlf, mail, dir);
    }

    /**
     * Valida los campos antes de agregar el cliente.
     *
     * @return True si los campos son correctos, false en caso contrario.
     */
    private boolean camposValidados()
    {
        Log.i(TAG, "Validando campos");

        if (razon_social.getText().toString().trim().isEmpty())
        {
            razon_social.setError("Introduzca una razon social!!");
            return false;
        }
        else if (sp_rif.getSelectedItemPosition() == 0)
        {
            TextView errorText = (TextView) sp_rif.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_rif);//changes the selected item text to this
            return false;
        }
        else if (rif1.getText().toString().trim().isEmpty())
        {
            rif1.setError("Introduzca un rif!!");
            return false;
        }
        else if (rif2.getText().toString().trim().isEmpty())
        {
            rif2.setError("Introduzca un rif!!");
            return false;
        }
        else if (sp_estado.getSelectedItemPosition() == 0)
        {
            TextView errorText = (TextView) sp_estado.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_estado);//changes the selected item text to this
            return false;
        }
        else if (telefono.getText().toString().trim().isEmpty())
        {
            telefono.setError("Introduzca un telefono!!");
            return false;
        }
        else if (email.getText().toString().trim().isEmpty())
        {
            email.setError("Introduzca un email!!");
            return false;
        }
        else if (Funciones.isValidEmail(email.getText().toString()))
        {
            email.setError("Por favor, ingrese un e-mail valido!!");
            return false;
        }
        else if (direccion.getText().toString().trim().isEmpty())
        {
            direccion.setError("Introduzca una direccion!!");
            return false;
        }
        else
        {
            razon_social.setError(null);
            rif1.setError(null);
            rif2.setError(null);
            telefono.setError(null);
            email.setError(null);
            direccion.setError(null);
            return true;
        }
    }

    /**
     * Carga la data por defecto en los spinners (Tipo de Rif y Estados).
     */
    private void loadSpinnerData()
    {
        Log.i(TAG, "loadSpinnerData");
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(contexto, R.array.rif_lista, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp_rif.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                contexto, R.array.estados_lista, R.layout.spinner_item);
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp_estado.setAdapter(adapter2);
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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

        return super.onOptionsItemSelected(item);
    }

    /**
     * Clase para agregar en segundo plano un cliente en la Base de Datos
     */
    class async_crearClienteBD extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                // Crea el cliente en BD
                long res = crearCliente(params);

                if (res == -1)
                {
                    Log.d(TAG, "err");
                    return "err";
                }
                else if (res == -2)
                {
                    Log.d(TAG, "Cliente ya existente");
                    return "existente";
                }
                else
                {
                    idCreado = String.valueOf(res);
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
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(AgregarCliente.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Agregando el cliente...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();

            switch (result)
            {
                case "ok":
                    // Muestra al usuario un mensaje de operacion exitosa
                    Toast.makeText(contexto, "Cliente agregado exitosamente!!", Toast.LENGTH_LONG).show();

                    if (desdePedidos)
                    {
                        // Redirige a la pantalla de Pedidos
                        Intent c = new Intent(AgregarCliente.this, SeleccionarCliente.class);
                        c.putExtra("usuario", usuario);
                        c.putExtra("desdeClientes", true);
                        c.putExtra("idClienteCreado", idCreado);
                        startActivity(c);
                        // Destruye el activity previo del pedido para crear uno nuevo, esto para
                        // evitar que se dupliquen..
                        SeleccionarCliente.fa.finish();
                    }
                    else
                    {
                        // Redirige a la pantalla de Home
                        Intent c = new Intent(AgregarCliente.this, GestionClientes.class);
                        c.putExtra("usuario", usuario);
                        startActivity(c);
                        // Destruye el activity previo de clientes para crear uno nuevo, esto para
                        // evitar que se dupliquen..
                        GestionClientes.fa.finish();
                    }

                    // Prevent the user to go back to this activity
                    finish();
                    break;
                case "existente":
                    Toast.makeText(contexto, "Ya existe un cliente con la misma razon social o rif..", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(contexto, "Hubo un error agregando el cliente..", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        /**
         * Funcion encargada de crear un cliente.
         *
         * @param datos String array con los datos del cliente.
         *              Los datos deben estar ordenados de la siguiente forma:
         *              Razon social, rif, estado, telefono, e-mail, direccion.
         * @return Valor indicativo del resultado de la operacion.
         * -1 ---> Error.
         * -2 ---> Cliente existente.
         * Valor positivo ---> ID del cliente creado.
         */
        private long crearCliente(String[] datos)
        {
            if (!existeCliente(datos[0], datos[2]))
            {
                long id_cliente = manager.agregarCliente(datos);
                Log.d(TAG, "ID_cliente: " + id_cliente);
                return id_cliente;
            }
            else
            {
                Log.e(TAG, "Ya existe dicho cliente!!");
                return -2;
            }
        }

        /**
         * Verifica si existe un cliente con la razon social o el rif ingresado.
         *
         * @param rs  Razon Social
         * @param rif Rif
         * @return True si el cliente ya existe, False en caso contrario.
         */
        private boolean existeCliente(String rs, String rif)
        {
            Cursor cursor = manager.cargarClientesNombre(rs, rif);
            return cursor.getCount() > 0;
        }
    }
}