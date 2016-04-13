package com.tufano.tufanomovil.gestion.productos;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBAdapter;
import com.tufano.tufanomovil.global.EndlessScrollListener;
import com.tufano.tufanomovil.global.productosAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Creado por Gerson el 13/04/2016..
 */
public class ConsultarProductos extends AppCompatActivity {
    public static Activity fa;
    private final int CANT_DATOS_MOSTRAR = 10;
    private Context            contexto;
    private DBAdapter          manager;
    private ListView           list;
    private List<List<String>> datos;
    private productosAdapter   adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consultar_productos);

        contexto = getApplicationContext();
        manager = new DBAdapter(contexto);
        fa = this;

        loadDefaultData();
    }

    private void loadDefaultData() {
        list = (ListView) findViewById(R.id.list);
        datos = new ArrayList<>();

        List<String> imagenes = new ArrayList<>();
        imagenes.add("1215-07");
        imagenes.add("1214-99");

        List<String> tallas = new ArrayList<>();
        tallas.add("P");
        tallas.add("P");

        List<String> tipos = new ArrayList<>();
        tipos.add("Torera");
        tipos.add("Sandalia");

        List<String> modelos = new ArrayList<>();
        modelos.add("1215-07P");
        modelos.add("1214-99P");

        List<String> colores = new ArrayList<>();
        colores.add("Rojo");
        colores.add("Azul Marino");

        List<String> precio = new ArrayList<>();
        precio.add("5.500");
        precio.add("3.850");

        List<String> numeracion = new ArrayList<>();
        numeracion.add("18-25");
        numeracion.add("18-25");

        List<String> editar_parametros = new ArrayList<>();
        editar_parametros.add("1");
        editar_parametros.add("1");

        List<String> compartir_parametros = new ArrayList<>();
        compartir_parametros.add("1");
        compartir_parametros.add("1");

        /*
        String[] imagenes = {"1215-07", "1214-99", "1101-01", "1207-12", "2302-03"};
        String[] tallas = {"P", "P", "M", "G", "P"};
        String[] tipos = {"Torera", "Sandalia", "Torera", "Sandalia", "Colegial"};
        String[] modelos = {"1215-07P", "1214-99P", "1101-01M", "1207-12G", "2302-03P"};
        String[] colores = {"Rojo", "Azul Marino", "Blanco", "Rosado", "Negro"};
        String[] precio = {"5.500", "3.850", "7.500", "2.000", "2.500"};
        String[] numeracion = {"18-25", "18-25", "26-34", "35-40", "18-25"};
        String[] editar_parametros = {"1", "1", "1", "1", "1"};
        String[] compartir_parametros = {"1", "1", "1", "1", "1"};
        */

        datos.add(imagenes);
        datos.add(tallas);
        datos.add(tipos);
        datos.add(modelos);
        datos.add(colores);
        datos.add(precio);
        datos.add(numeracion);
        datos.add(editar_parametros);
        datos.add(compartir_parametros);


        adapter = new productosAdapter(this, datos);
        //adapter = new productosAdapter(this, imagenes, tallas, tipos, modelos, colores, precio, numeracion, editar_parametros, compartir_parametros);

        list.setAdapter(adapter);
        list.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                //String msj = "onLoadMore : page = "+page+", totalItemsCount = "+totalItemsCount;
                //Toast.makeText(getApplicationContext(), msj, Toast.LENGTH_LONG).show();
                Log.i("TAG", "onLoadMore : page = " + page + ", totalItemsCount = " + totalItemsCount);
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                customLoadMoreDataFromApi(page, totalItemsCount);
                // or customLoadMoreDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });

    }

    // Append more data into the adapter
    public void customLoadMoreDataFromApi(int page, int totalItemsCount) {
        // This method probably sends out a network request and appends new data items to your adapter.
        // Use the page value and add it as a parameter to your API request to retrieve paginated data.
        // Deserialize API response and then construct new objects to append to the adapter
        int limit = 5; // Numero total de elementos

        if (totalItemsCount == limit) {
            Toast.makeText(getApplicationContext(), "YA ACABE!", Toast.LENGTH_LONG).show();
        }
        else {
            int min = totalItemsCount + 1;
            int max = min + CANT_DATOS_MOSTRAR;

            //String msj = "min: "+min+", max: "+max;
            //Toast.makeText(getApplicationContext(), msj, Toast.LENGTH_LONG).show();

            Log.i("TAG", "min: " + min + ", max: " + max);

            List<String> imagenes = new ArrayList<>();
            imagenes.add("1101-01");
            imagenes.add("1207-12");

            List<String> tallas = new ArrayList<>();
            tallas.add("G");
            tallas.add("M");

            List<String> tipos = new ArrayList<>();
            tipos.add("Torera");
            tipos.add("Sandalia");

            List<String> modelos = new ArrayList<>();
            modelos.add("1215-07P");
            modelos.add("1214-99P");

            List<String> colores = new ArrayList<>();
            colores.add("Rojo");
            colores.add("Azul Marino");

            List<String> precio = new ArrayList<>();
            precio.add("5.500");
            precio.add("3.850");

            List<String> numeracion = new ArrayList<>();
            numeracion.add("18-25");
            numeracion.add("18-25");

            List<String> editar_parametros = new ArrayList<>();
            editar_parametros.add("1");
            editar_parametros.add("1");

            List<String> compartir_parametros = new ArrayList<>();
            compartir_parametros.add("1");
            compartir_parametros.add("1");

            datos = new ArrayList<>();
            datos.add(imagenes);
            datos.add(tallas);
            datos.add(tipos);
            datos.add(modelos);
            datos.add(colores);
            datos.add(precio);
            datos.add(numeracion);
            datos.add(editar_parametros);
            datos.add(compartir_parametros);

            adapter = new productosAdapter(this, datos);

            ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
        }
    }
}
