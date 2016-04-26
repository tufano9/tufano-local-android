package com.tufano.tufanomovil.global;

/**
 * Desarrollado por Gerson el 5/4/2016.
 * <p/>
 * Clase donde guardo todos los metodos que puede que necesite en un futuro.
 */
public class RecicledBin
{
    /**
     * Cierra el teclado del dispositivo
     */
    /*private void cerrarTeclado(Context contexto)
    {
        InputMethodManager inputManager =
                (InputMethodManager) contexto.
                        getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null)
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        else
            Log.w(TAG, "No se pudo ocultar el Teclado");
    }
     */

    /**
     * Inicializador del autocompleteTextView
     */
    /*private void initAutoComplete()
    {
        Log.i(TAG, "initAutoComplete");
        /*ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, obtenerModelos());

        modelo_autoComplete = (AutoCompleteTextView) findViewById(R.id.autoC_modelo);
        modelo_autoComplete.setAdapter(adapter);
        modelo_autoComplete.setThreshold(1);
        modelo_autoComplete.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == 66)
                {
                    layout.requestFocus();
                    gestionarFiltrado();
                    return true;
                }
                return false;
            }
        });

        modelo_autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                gestionarFiltrado();
            }
        });
    }*/

    /*public long eliminarCliente(String id)
    {
        return db.delete(TABLA_CLIENTES, CN_ID_CLIENTE + "=?", new String[]{id});
    }*/

    /*public Cursor buscarClienteID(String id_cliente)
    {
        String[] columnas = new String[]{CN_RAZON_SOCIAL_CLIENTE};
        String[] args = { id_cliente };
        return db.query(TABLA_CLIENTES, columnas, CN_ID_CLIENTE + "=?", args, null, null, null);
    }*/

    /*public long eliminarColor(String id)
    {
        return db.delete(TABLA_COLORES, CN_ID_COLOR + "=?", new String[]{id});
    }*/


    /*public long eliminarTipo(String id)
    {
        return db.delete(TABLA_TIPOS, CN_ID_TIPO + "=?", new String[]{id});
    }*/



    /*public long eliminarTalla(String id)
    {
        return db.delete(TABLA_TALLAS, CN_ID_TALLA + "=?", new String[]{id});
    }*/


    /*public Cursor buscarUsuarioID(String id_usuario)
    {
        String[] columnas = new String[]{CN_ID_USUARIO, CN_NOMBRE_USUARIO, CN_NOMBRE, CN_APELLIDO, CN_CEDULA, CN_TELEFONO, CN_EMAIL, CN_PASSWORD, CN_PASSWORD, CN_ESTADO, CN_KEY};
        String[] args = { id_usuario };
        return db.query(TABLA_USUARIO, columnas, CN_ID_USUARIO + "=?", args, null, null, null);
    }*/



        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
}
