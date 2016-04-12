package com.tufano.tufanomovil.gestion.pedidos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.gestion.clientes.AgregarCliente;
import com.tufano.tufanomovil.gestion.clientes.EditarClienteDetalles;
import com.tufano.tufanomovil.global.Funciones;

import java.util.List;

/**
 * Created por Usuario Tufano on 21/01/2016.
 */
public class SeleccionarCliente extends AppCompatActivity
{
    private String usuario;
    private Context contexto;
    private final String TAG = "SeleccionarCliente";
    private DBAdapter manager;
    private Spinner sp_cliente;
    public static Activity fa;
    private List<List<String>> clientes;
    private boolean desdeClientes;
    private String idClienteCreado;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realizar_pedido);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);
        fa = this;

        getExtrasVar();
        createToolBar();
        initSpinners();
        inicializarBotones();
        loadSpinnerData();

        if(desdeClientes)
            seleccionarCliente(idClienteCreado);
    }

    /**
     * Inicializa los spinners
     */
    private void initSpinners()
    {
        sp_cliente = (Spinner) findViewById(R.id.sp_cliente);
        sp_cliente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0)
                    actualizarDatosCliente(position - 1);
                else
                    borrarCamposDatosCliente();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        usuario = bundle.getString("usuario");
        desdeClientes = bundle.getBoolean("desdeClientes");
        idClienteCreado = bundle.getString("idClienteCreado");
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.realizar_pedido_subtitulo);
        setSupportActionBar(toolbar);
    }

    /**
     * Selecciona un cliente del spinner a traves del nombre.
     * @param id ID del cliente a seleccionar.
     */
    private void seleccionarCliente(String id)
    {
        Log.i(TAG, "Seleccionando cliente..");
        int pos = buscarPosicionCliente(id);
        sp_cliente.setSelection(pos);
    }

    /**
     * Busca la posicion del cliente en el array que contiene todos los clientes
     * @param id ID del cliente a buscar
     * @return Posicion del cliente, -1 si no lo encontro.
     */
    private int buscarPosicionCliente(String id)
    {
        for(int i=0; i<clientes.get(0).size(); i++)
        {
            if(clientes.get(0).get(i).equals(id))
                return i+1;
        }
        Log.e(TAG, "Cliente no encontrado...");
        return -1;
    }

    /**
     * Inicializacion de los botones
     */
    private void inicializarBotones()
    {
        Button btn_editar_cliente_pedido = (Button) findViewById(R.id.btn_editar_cliente_pedido);
        btn_editar_cliente_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(sp_cliente.getSelectedItemPosition() >0)
                {
                    String id_cliente = clientes.get(0).get(sp_cliente.getSelectedItemPosition()-1);
                    List<String> datos = manager.cargarDatosClientes(id_cliente);

                    Intent c = new Intent(SeleccionarCliente.this, EditarClienteDetalles.class);
                    c.putExtra("desdePedidos", true);
                    c.putExtra("usuario", usuario);
                    c.putExtra("id_cliente", id_cliente);
                    c.putExtra("rs", datos.get(0));
                    c.putExtra("rif", datos.get(1));
                    c.putExtra("estados", datos.get(2));
                    c.putExtra("tlf", datos.get(3));
                    c.putExtra("mail", datos.get(4));
                    c.putExtra("dir", datos.get(5));
                    c.putExtra("estatus", datos.get(6));
                    startActivity(c);
                }
                else
                {
                    Toast.makeText(contexto, "¡Por favor seleccione algun cliente!", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button btn_agregar_cliente_pedido = (Button) findViewById(R.id.btn_agregar_cliente_pedido);
        btn_agregar_cliente_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent c = new Intent(SeleccionarCliente.this, AgregarCliente.class);
                c.putExtra("usuario", usuario);
                c.putExtra("desdePedidos", true);
                startActivity(c);
            }
        });

        Button btn_realizar_pedido = (Button) findViewById(R.id.btn_realizar_pedido);
        btn_realizar_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(sp_cliente.getSelectedItemPosition() >0)
                {
                    /*EditText et_observaciones = (EditText) findViewById(R.id.et_observaciones);
                    Log.w(TAG, "Realizare un pedido a " + sp_cliente.getItemAtPosition(sp_cliente.getSelectedItemPosition()) );
                    String observacion = et_observaciones.getText().toString().trim();
                    Log.w(TAG, "Observacion: " + observacion );*/

                    String id_cliente = clientes.get(0).get(sp_cliente.getSelectedItemPosition()-1);

                    Intent c = new Intent(SeleccionarCliente.this, ArmarPedido.class);
                    c.putExtra("usuario", usuario);
                    c.putExtra("id_cliente", id_cliente);
                    startActivity(c);
                }
                else
                {
                    Toast.makeText(contexto, "¡Por favor seleccione algun cliente!", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*Button btn_cancelar_pedido = (Button) findViewById(R.id.btn_cancelar_pedido);
        btn_cancelar_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SeleccionarCliente.this);

                dialog.setTitle("Confirmacion");
                dialog.setMessage(R.string.confirmacion_cancelar_pedido);
                dialog.setCancelable(false);
                dialog.setPositiveButton("Estoy seguro", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new async_cancelarPedidoTemporal().execute();
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
        });*/
    }

    /**
     * Borra los datos de los campos del cliente.
     */
    private void borrarCamposDatosCliente()
    {
        EditText et_razon_social = (EditText) findViewById(R.id.et_razon_social);
        EditText et_rif = (EditText) findViewById(R.id.et_rif);
        EditText et_estado = (EditText) findViewById(R.id.et_estado);
        EditText et_telefono = (EditText) findViewById(R.id.et_telefono);
        EditText et_email = (EditText) findViewById(R.id.et_email);
        EditText et_direccion = (EditText) findViewById(R.id.et_direccion);

        et_razon_social.setText("");
        et_rif.setText("");
        et_estado.setText("");
        et_telefono.setText("");
        et_email.setText("");
        et_direccion.setText("");
    }

    /**
     * Funcion que mostrara los datos del cliente seleccionado.
     * @param position Posicion del cliente seleccionado en el spinner.
     */
    private void actualizarDatosCliente(int position)
    {
        Log.d(TAG, "Mostrando datos del cliente " + clientes.get(1).get(position+1));
        String id = clientes.get(0).get(position);

        EditText et_razon_social = (EditText) findViewById(R.id.et_razon_social);
        EditText et_rif = (EditText) findViewById(R.id.et_rif);
        EditText et_estado = (EditText) findViewById(R.id.et_estado);
        EditText et_telefono = (EditText) findViewById(R.id.et_telefono);
        EditText et_email = (EditText) findViewById(R.id.et_email);
        EditText et_direccion = (EditText) findViewById(R.id.et_direccion);

        List<String> datos_clientes = obtenerDatosCliente(id);

        et_razon_social.setText(datos_clientes.get(0));
        et_rif.setText( datos_clientes.get(1) );
        et_estado.setText( datos_clientes.get(2) );
        et_telefono.setText( Funciones.formatoTelefono(datos_clientes.get(3)) );
        et_email.setText( datos_clientes.get(4) );
        et_direccion.setText( datos_clientes.get(5) );
    }

    /**
     * Funcion que obtiene los datos del cliente indicado a traves de la BD
     * @param id ID del cliente a consultar.
     * @return Lista con los datos del cliente.
     */
    private List<String> obtenerDatosCliente(String id)
    {
        return manager.cargarDatosClientes(id);
    }

    /**
     * Funcion que carga los datos del spinner de clientes
     */
    private void loadSpinnerData()
    {
        Log.i(TAG, "loadSpinnerData");
        clientes = manager.cargarListaClientes();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, clientes.get(1));
        dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp_cliente.setAdapter(dataAdapter);
    }

    /*
    class async_cancelarPedidoTemporal extends AsyncTask< String, String, String >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(SeleccionarCliente.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cancelando el pedido...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                //enviamos y recibimos y analizamos los datos en segundo plano.
                if (cancelarPedidoTemporal())
                {
                    return "ok"; //login valido
                }
                else
                {
                    Log.d(TAG, "err");
                    return "err"; //login invalido
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
                Toast.makeText(contexto, "Pedido cancelado exitosamente!!", Toast.LENGTH_LONG).show();

                // Redirige a la pantalla de Home
                Intent c = new Intent(SeleccionarCliente.this, ArmarPedido.class);
                c.putExtra("usuario",usuario);
                startActivity(c);

                //Menu.fa.finish();
                ArmarPedido.fa.finish();

                // Prevent the user to go back to this activity
                finish();
            }
            else
            {
                Toast.makeText(contexto, "Hubo un error cancelando el pedido..", Toast.LENGTH_LONG).show();
            }
        }
    }

    */

    /*protected boolean cancelarPedidoTemporal()
    {
        return manager.borrarPedidoTemporal() > 0;
    }*/
}
