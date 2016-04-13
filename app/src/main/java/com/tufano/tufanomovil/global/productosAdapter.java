package com.tufano.tufanomovil.global;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tufano.tufanomovil.R;

import java.util.List;

public class productosAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    public  ImageLoader  imageLoader;
    private List<String> imagesData;
    private List<String> tallasData;
    private List<String> tiposData;
    private List<String> modelosData;
    private List<String> coloresData;
    private List<String> precioData;
    private List<String> numeracionData;
    private List<String> editarData;
    private List<String> compartirData;

    /*public productosAdapter(Activity a, String[] imagenes, String[] tallas, String[] tipos,
                            String[] modelos, String[] colores, String[] precio,
                            String[] numeracion, String[] editar_parametros,
                            String[] compartir_parametros)
    {
        imagesData = imagenes;
        tallasData = tallas;
        tiposData = tipos;
        modelosData = modelos;
        coloresData = colores;
        precioData = precio;
        numeracionData = numeracion;
        editarData = editar_parametros;
        compartirData = compartir_parametros;

        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(a.getApplicationContext());
    }*/

    public productosAdapter(Activity a, List<List<String>> datos) {
        imagesData = datos.get(0);
        tallasData = datos.get(1);
        tiposData = datos.get(2);
        modelosData = datos.get(3);
        coloresData = datos.get(4);
        precioData = datos.get(5);
        numeracionData = datos.get(6);
        editarData = datos.get(7);
        compartirData = datos.get(8);

        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(a.getApplicationContext());
    }

    public int getCount() {
        return imagesData.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.consultar_producto_items, null);

        ImageView imagen     = (ImageView) vi.findViewById(R.id.imagen);
        TextView  talla      = (TextView) vi.findViewById(R.id.talla);
        TextView  tipo       = (TextView) vi.findViewById(R.id.tipo);
        TextView  modelo     = (TextView) vi.findViewById(R.id.modelo);
        TextView  color      = (TextView) vi.findViewById(R.id.color);
        TextView  precio     = (TextView) vi.findViewById(R.id.precio);
        TextView  numeracion = (TextView) vi.findViewById(R.id.numeracion);
        //ImageView editar = (ImageView) vi.findViewById(R.id.editar); // Colocarle el listener
        //ImageView compartir = (ImageView) vi.findViewById(R.id.compartir); // Colocarle el listener

        imageLoader.DisplayImage(imagesData.get(position), imagen);
        talla.setText(tallasData.get(position));
        tipo.setText(tiposData.get(position));
        modelo.setText(modelosData.get(position));
        color.setText(coloresData.get(position));
        precio.setText(precioData.get(position));
        numeracion.setText(numeracionData.get(position));
        return vi;
    }
}