package com.tufano.tufanomovil.gestion.productos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.tufano.tufanomovil.R;

/**
 * Creado por Gerson el 12/01/2016.
 */
public class GestionProductos extends AppCompatActivity
{
    private String usuario;
    public static Activity fa;
    private ImageView agregar, editar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_productos);

        fa = this;

        getExtrasVar();
        createToolBar();
        initComponents();
        initListeners();
    }

    /**
     * Inicializa los componentes primarios de la activity
     */
    private void initComponents()
    {
        //Creando los imagesView que haran funcion de menu al ser clickeados
        agregar = (ImageView) findViewById(R.id.img_agregar);
        editar = (ImageView) findViewById(R.id.img_editar);
    }

    /**
     * Inicializa los listeners
     */
    private void initListeners()
    {
        agregar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Al ser clickeado, se abre una nueva activity
                Intent c = new Intent(GestionProductos.this, AgregarProducto.class);
                c.putExtra("usuario", usuario);
                startActivity(c);
            }
        });

        editar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Al ser clickeado, se abre una nueva activity
                Intent c = new Intent(GestionProductos.this, EditarProducto.class);
                c.putExtra("usuario", usuario);
                startActivity(c);
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
    }

    /**
     * Crea la barra superior con un subtitulo.
     */
    private void createToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.gestion_producto_subtitulo);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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

}