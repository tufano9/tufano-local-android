package com.tufano.tufanomovil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.Funciones;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    private Context contexto;
    private final String TAG = "MainActivity";
    private DBAdapter manager;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contexto = getApplicationContext();

        // Limpiar la BD
        //contexto.deleteDatabase(DBHelper.DB_NAME);

        // Imprimir contenido de la BD
        //Funciones.imprimirBaseDatos(DBAdapter.db);

        // Creando objeto para el manejo de la BD
        manager = new DBAdapter(contexto);

        createToolBar();
        noInitialFocus();
        initButtons();
        setLogin();
    }

    /**
     * Coloca los valores por defecto en los campos de login, unicamente para efectos de DEBUG
     */
    private void setLogin()
    {
        // Colocar de forma predeterminada las credencials de inicio de sesion..
        Funciones.setLoginCredentials("ftufano", "1234", (TextView) findViewById(R.id.usuario_login), (TextView) findViewById(R.id.pass_login));
    }

    /**
     * Inicializa los botones
     */
    private void initButtons()
    {
        Button btn_login = (Button) findViewById(R.id.btn_login);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                validarLogin();
            }
        });
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
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle("Bienvenido");
        setSupportActionBar(toolbar);
    }

    /**
     * Funcion encargada de procesar toda la informacion del login de la app
     */
    private void validarLogin()
    {
        TextView usuario = (TextView) findViewById(R.id.usuario_login);
        TextView pass = (TextView) findViewById(R.id.pass_login);

        if(usuario.getText().toString().isEmpty())
        {
            usuario.setError("Este campo es obligatorio");
            Toast.makeText(contexto, "Por favor ingrese el usuario!", Toast.LENGTH_LONG).show();
        }
        else if(pass.getText().toString().isEmpty())
        {
            usuario.setError(null);
            pass.setError("Este campo es obligatorio");
            Toast.makeText(contexto, "Por favor ingrese la contraseña!", Toast.LENGTH_LONG).show();
        }
        else
        {
            pass.setError(null);

            // Los campos estan llenos, procedo a verificar la informacion..
            new login_local().execute(usuario.getText().toString(), pass.getText().toString());
        }
    }

    /**
     * Hace un login en segundo plano usando la base de datos.
     */
    class login_local extends AsyncTask< String, String, String >
    {
        String password, usuario;

        @Override
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Verificando datos de forma local...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            usuario=params[0];
            password=params[1];

            try
            {
                //enviamos y recibimos y analizamos los datos en segundo plano.
                if (doLogin(usuario, password))
                {
                    //login valido
                    return "ok";
                }
                else
                {
                    Log.d("Loginstatuslocal","err");
                    //login invalido
                    return "err";
                }
            }
            catch (RuntimeException e)
            {
                Log.d("Loginstatuslocal", "err2: " + e);
                return "err2";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            pDialog.dismiss();

            if (result.equals("ok"))
            {
                // El login fue correcto, busco el ID del usuario en la BD
                String id_usuario = buscarIDUsuario(usuario);
                // Inicio sesion
                iniciarSesion(id_usuario);
            }
            else
            {
                Toast.makeText(contexto, "La combinacion de usuario y contraseña es erronea!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Metodo encargado de verificar en la BD los datos proporcionados por el usuario
     * @param nombre_usuario Nombre del usuario que intenta acceder al sistema
     * @param local_password Contraseña del usuario anteriormente descrito
     * @return True si el login fue realizado de forma correcta, False en caso contrario.
     */
    private boolean doLogin(String nombre_usuario, String local_password)
    {
        Log.d(TAG, "doLogin");
        boolean bandera = false;

        Cursor cursor = manager.buscarUsuario(nombre_usuario);

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            // Busco el usuario en la Base de datos..
            if(cursor.getString(1).equals(nombre_usuario))
            {
                // Al encontrarlo, extraigo la contraseña y el key (con lo que la encripto)
                try
                {
                    String db_password = cursor.getString(7);
                    String key = cursor.getString(10);

                    // Encripto la password ingresada por el usuario utilizando el key de la BD
                    String local_encrypt_password = new String(Funciones.encrypt(key, local_password), "UTF-8");

                    // Si la contraseña local encriptada con el mismo key de la base de datos, es
                    // igual a mi contraseña encriptada de la BD, entonces la contraseña esta bien.
                    if( db_password.equals(local_encrypt_password) )
                    {
                        Log.d(TAG, "Usuario y contraseña correctos!");
                        bandera = true;
                    }
                    else
                    {
                        Log.d(TAG,"Usuario y contraseña INCORRECTOS!");
                    }
                }
                catch (GeneralSecurityException | UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }

                break;
            }
        }

        // Cierro el cursor luego de utilizarlo..
        cursor.close();

        return bandera;
    }

    /**
     * Metodo encargado de buscar el ID del username ingresado como parametro.
     * @param usuario Username al que se le buscara el ID.
     * @return El ID del usuario en cuestion, null en caso de no existir dicho usuario.
     */
    private String buscarIDUsuario(String usuario)
    {
        Cursor cursor = manager.buscarUsuario(usuario);

        if(cursor.moveToFirst())
        {
            Log.d(TAG, "ID_USUARIO = "+cursor.getString(0));
            return cursor.getString(0);
        }
        cursor.close();
        return null;
    }

    /**
     * Metodo para ingresar a la aplicacion luego de haber sido autentificado exitosamente.
     * @param usuario Usuario que inicio la sesion en la aplicacion.
     */
    private void iniciarSesion(String usuario)
    {
        Log.d(TAG, "Successful Login");

        // Creo un activity nuevo con el Home de la app, pasando como parametro el id del usuario
        // que se acaba de logear.
        Intent c = new Intent(MainActivity.this, Home.class);
        c.putExtra("usuario",usuario);
        startActivity(c);
    }

    /**
     * Con este metodo se crea el menu emergente que aparece al presionar sobre la tecla Menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Metodo encargado de gestionar la accion a realizar tras seleccionar algun elemento del menu
     * emergente.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}