package com.tufano.tufanomovil.gestion.perfil;

import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.Home;
import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.Funciones;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Usuario Tufano onn 02/02/2016.
 */
public class EditarPerfil extends AppCompatActivity
{
    private String id_usuario;
    private Context contexto;
    private DBAdapter manager;
    private final String TAG = "EditarPerfil";
    public static Activity fa;
    private EditText nombre_usuario, apellido_usuario, email_usuario, telefono_usuario;
    private EditText cedula_usuario, password, repassword;
    private Spinner estado_usuario;
    private Button editar_perfil;
    private ProgressDialog pDialog;
    private CheckBox cb_cambiarPass;
    private EditText newpassword_usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);

        createToolBar();
        getExtrasVar();
        initComponents();
        initButtonAction();
        initCheckBoxAction();
        loadSpinnerData();
        loadData();
    }

    /**
     * Obtiene las variables que fueron pasadas como parametro desde otro activity
     */
    private void getExtrasVar()
    {
        Bundle bundle = getIntent().getExtras();
        id_usuario = bundle.getString("usuario");
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.editar_perfil);
        setSupportActionBar(toolbar);
    }

    /**
     * Inicializacion primaria de los componentes utilizados en esta clase.
     */
    private void initComponents()
    {
        nombre_usuario = (EditText) findViewById(R.id.nombre_usuario);
        apellido_usuario = (EditText) findViewById(R.id.apellido_usuario);
        email_usuario = (EditText) findViewById(R.id.email_usuario);
        telefono_usuario = (EditText) findViewById(R.id.telefono_usuario);
        cedula_usuario = (EditText) findViewById(R.id.cedula_usuario);
        password = (EditText) findViewById(R.id.password_usuario);
        repassword = (EditText) findViewById(R.id.repassword_usuario);
        newpassword_usuario = (EditText) findViewById(R.id.newpassword_usuario);

        estado_usuario = (Spinner) findViewById(R.id.estado_usuario);
        editar_perfil = (Button) findViewById(R.id.btn_editar_perfil);
        cb_cambiarPass = (CheckBox) findViewById(R.id.cb_cambiarPassword);

        LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayout_MainActivity);
        layout.requestFocus();
    }

    /**
     * Inicializa el boton para editar el perfil.
     */
    private void initButtonAction()
    {
        editar_perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camposValidados()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(EditarPerfil.this);

                    dialog.setTitle(R.string.confirmacion);
                    dialog.setMessage(R.string.confirmacion_editar_perfil);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editarPerfil();
                        }
                    });

                    dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                } else {
                    Toast.makeText(contexto, "¡Por favor ingrese todos los datos!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Inicializa el checkbox encargado de habilitar y deshabilitar el cambio de password del
     * usuario.
     */
    private void initCheckBoxAction()
    {
        cb_cambiarPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    repassword.setEnabled(true);
                    newpassword_usuario.setEnabled(true);
                    repassword.setText("");
                    newpassword_usuario.setText("");
                    repassword.setError(null);
                    newpassword_usuario.setError(null);
                    repassword.setVisibility(View.VISIBLE);
                    newpassword_usuario.setVisibility(View.VISIBLE);
                }
                else
                {
                    repassword.setEnabled(false);
                    newpassword_usuario.setEnabled(false);
                    repassword.setText("");
                    newpassword_usuario.setText("");
                    repassword.setError(null);
                    newpassword_usuario.setError(null);
                    repassword.setVisibility(View.GONE);
                    newpassword_usuario.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Carga con informacion el spinner del estado.
     */
    private void loadSpinnerData()
    {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                contexto, R.array.estados_lista, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        estado_usuario.setAdapter(adapter);
    }

    /**
     * Carga la data desde la BD hasta los campos.
     */
    private void loadData()
    {
        List<String> datosUsuario = manager.buscarUsuario_ID(id_usuario);
        nombre_usuario.setText( datosUsuario.get(1) );
        apellido_usuario.setText( datosUsuario.get(2) );
        cedula_usuario.setText( datosUsuario.get(3) );
        telefono_usuario.setText( datosUsuario.get(4) );
        email_usuario.setText( datosUsuario.get(5) );
        int pos = Funciones.buscarPosicionElemento(datosUsuario.get(6), estado_usuario);
        estado_usuario.setSelection(pos);
    }

    /**
     * Funcion que prepara la edicion del perfil.
     */
    private void editarPerfil()
    {
        ArrayList<String> datos = obtenerDatos();
        //noinspection unchecked
        new editarPerfil().execute(datos);
    }

    /**
     * Obtiene los datos del usuario que estan en los campos.
     * @return Lista con todos los datos.
     */
    private ArrayList<String> obtenerDatos()
    {
        ArrayList<String> datos = new ArrayList<>();
        datos.add(nombre_usuario.getText().toString().trim());
        datos.add(apellido_usuario.getText().toString().trim());
        datos.add(email_usuario.getText().toString().trim());
        datos.add(telefono_usuario.getText().toString().trim());
        datos.add(cedula_usuario.getText().toString().trim());
        datos.add(password.getText().toString().trim());
        datos.add(estado_usuario.getSelectedItem().toString().trim());
        datos.add(repassword.getText().toString().trim());
        return datos;
    }

    /**
     * Valida los campos para editar el perfil
     * @return True si los campos fueron validados correctamente, false en caso contrario.
     */
    private boolean camposValidados()
    {
        if(nombre_usuario.getText().toString().trim().isEmpty())
        {
            nombre_usuario.setError("Campo obligatorio");
            return false;
        }
        else if(apellido_usuario.getText().toString().trim().isEmpty())
        {
            apellido_usuario.setError("Campo obligatorio");
            return false;
        }
        else if (Funciones.isValidEmail(email_usuario.getText().toString().trim()))
        {
            email_usuario.setError("Por favor, ingrese un email valido!!");
            return false;
        }
        else if(telefono_usuario.getText().toString().trim().isEmpty())
        {
            telefono_usuario.setError("Campo obligatorio");
            return false;
        }
        else if(cedula_usuario.getText().toString().trim().isEmpty())
        {
            cedula_usuario.setError("Campo obligatorio");
            return false;
        }
        else if(password.getText().toString().isEmpty())
        {
            password.setError("Campo obligatorio");
            return false;
        }
        else if(estado_usuario.getSelectedItemPosition()==0)
        {
            TextView errorText = (TextView) estado_usuario.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);//just to highlight that this is an error
            errorText.setText(R.string.error_estado);//changes the selected item text to this
            return false;
        }
        else if(newpassword_usuario.isEnabled())
        {
            // Debo validar dichos campos
            if(newpassword_usuario.getText().toString().trim().isEmpty())
            {
                newpassword_usuario.setError("Campo obligatorio");
                return false;
            }
            else if(repassword.getText().toString().trim().isEmpty())
            {
                repassword.setError("Campo obligatorio");
                return false;
            }
            else if( !newpassword_usuario.getText().toString().equals(repassword.getText().toString()) )
            {
                newpassword_usuario.setError("¡Las contraseñas no coinciden!");
                repassword.setError("¡Las contraseñas no coinciden!");
                return false;
            }
            else
                return true;
        }
        else
            return true;
    }

    /**
     * Verifica si ambas contraseñas coinciden (La del usuario y la de la BD)
     * @return True si coinciden, False en caso contrario.
     */
    private boolean passwordsCoinciden()
    {
        try
        {
            String key = manager.getUserKey(id_usuario);
            String passwordEditText = password.getText().toString().trim();
            String encryptedPassword = new String(Funciones.encrypt(key, passwordEditText), "UTF-8");
            String passwordBD = manager.obtenerPassword(id_usuario);
            return encryptedPassword.equals(passwordBD);
        }
        catch (UnsupportedEncodingException | GeneralSecurityException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Clase para editar en 2do plano el perfil del usuario actual.
     */
    class editarPerfil extends AsyncTask< ArrayList<String>, Void, ArrayList<String> >
    {
        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(EditarPerfil.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Editando el perfil...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @SafeVarargs
        @Override
        protected final ArrayList<String> doInBackground(ArrayList<String>... params)
        {
            ArrayList<String> resultado = new ArrayList<>();
            try
            {
                long res = editarPerfilUsuario(params);

                if (res == -1)
                {
                    Log.d(TAG, "err");
                    resultado.add("error");
                    return resultado;
                }
                else if (res == -2)
                {
                    Log.d(TAG, "Cedula ya existente");
                    resultado.add("existente");
                    return resultado;
                }
                else if (res == -3)
                {
                    Log.d(TAG, "Contraseñas no coinciden");
                    resultado.add("badPassword");
                    return resultado;
                }
                else
                {
                    resultado.add("ok");
                    return resultado;
                }
            }
            catch (RuntimeException e)
            {
                Log.d(TAG, "Error: " + e);
                resultado.add("error2");
                return resultado;
            }
        }

        protected void onPostExecute(ArrayList<String> result)
        {
            pDialog.dismiss();

            switch (result.get(0))
            {
                case "ok":
                    // Muestra al id_usuario un mensaje de operacion exitosa
                    Toast.makeText(contexto, "Perfil editado exitosamente!!", Toast.LENGTH_LONG).show();

                    /*if(desdePedidos)
                    {
                        // Redirige a la pantalla de Pedidos
                        Intent c = new Intent(EditarPerfil.this, SeleccionarCliente.class);
                        c.putExtra("id_usuario", id_usuario);
                        c.putExtra("desdeClientes", true);
                        c.putExtra("idClienteCreado", id_cliente);
                        startActivity(c);
                        SeleccionarCliente.fa.finish();
                    }
                    else
                    {
                        // Redirige a la pantalla de Home
                        Intent c = new Intent(EditarPerfil.this, EditarCliente.class);
                        c.putExtra("id_usuario", id_usuario);
                        startActivity(c);
                        EditarCliente.fa.finish();
                    }*/

                    // Redirige a la pantalla de Pedidos
                    Intent c = new Intent(EditarPerfil.this, Home.class);
                    c.putExtra("usuario", id_usuario);
                    startActivity(c);
                    Home.fa.finish();

                    // Prevent the user to go back to this activity
                    finish();
                    break;
                case "existente":
                    Toast.makeText(contexto, "Ya existe un usuario con esa misma cedula..", Toast.LENGTH_LONG).show();
                    break;
                case "badPassword":
                    Toast.makeText(contexto, "Ha ingresado una contraseña erronea", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(contexto, "Hubo un error editando el usuario..", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        private long editarPerfilUsuario(ArrayList<String>[] datos)
        {
            if(!existeCedula(datos[0].get(4), id_usuario) )
            {
                if(passwordsCoinciden())
                {
                    if(cb_cambiarPass.isChecked())
                    {
                        //Debo cambiar el password..
                        // El usuario no existe, puedo continuar..
                        Log.i(TAG, "Editare la contraseña");
                        return manager.editarUsuario(id_usuario, datos[0], true);
                    }
                    else
                    {
                        //Actualizo menos el password..
                        // El cliente no existe, puedo continuar..
                        Log.i(TAG, "No editare la contraseña");
                        return manager.editarUsuario(id_usuario, datos[0], false);
                    }
                }
                else
                {
                    return -3;
                }
            }
            else
                return -2;
            /*else
            {
                String rifs = sp_rif.getSelectedItem().toString() + rif1.getText().toString().trim() + "-" +
                        rif2.getText().toString().trim();
                // El cliente existe, valido si cambio el campo o se mantienen igual
                if ( !razon_social.getText().toString().trim().equals(rs) || !rif.equals(rifs) )
                {
                    Log.d(TAG, "El campo ingresado por el id_usuario cambio y por lo tanto ya tengo ese cliente");
                    //Log.d(TAG, "1-->"+razon_social.getText().toString().trim().equals(rs)+", 2-->"+rif.equals(rifs));
                    // El campo ingresado por el id_usuario cambio y por lo tanto ya tengo ese cliente
                    return -2;
                }
                else
                {
                    // Los campos se mantienen, asi que el cliente que ya existe, es el mismo que
                    // ando editando.. prosigo como si nada
                    Log.d(TAG, "Los campos se mantienen, asi que el cliente que ya existe, es el mismo que ando editando.. prosigo como si nada");
                    return manager.editarCliente(id_cliente, datos);
                }
            }*/
        }

        private boolean existeCedula(String cedula, String id_cliente)
        {
            Cursor cursor = manager.buscarUsuarioCedula_ID(cedula, id_cliente);
            Log.d(TAG, "Existen "+cursor.getCount()+" usuarios con esa cedula..");
            return cursor.getCount() > 0;
        }
    }
}