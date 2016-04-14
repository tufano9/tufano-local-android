package com.tufano.tufanomovil.global;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tufano.tufanomovil.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class productosAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private final  String         TAG      = "productosAdapter";
    public  ImageLoader imageLoader;
    private Context     contexto;
    private List<List<String>> productos = new ArrayList<>();
    private Activity ac;

    public productosAdapter(Activity a, List<List<String>> datos)
    {
        for (int i = 0; i < datos.size(); i++) {
            List<String> p = new ArrayList<>();
            p.add(datos.get(i).get(0)); // imagesData
            p.add(datos.get(i).get(1)); // tallasData
            p.add(datos.get(i).get(2)); // tiposData
            p.add(datos.get(i).get(3)); // modelosData
            p.add(datos.get(i).get(4)); // coloresData
            p.add(datos.get(i).get(5)); // precioData
            p.add(datos.get(i).get(6)); // numeracionData
            p.add(datos.get(i).get(7)); // editarData
            p.add(datos.get(i).get(8)); // compartirData

            productos.add(p);
        }

        ac = a;
        contexto = a.getApplicationContext();
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(contexto);
    }

    public void add(List<String> producto) {
        productos.add(producto);
        Log.i(TAG, "Producto agregado al adapter.");
    }

    public int getCount() {
        return productos.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(final int position, View convertView, ViewGroup parent) {
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
        ImageView editar     = (ImageView) vi.findViewById(R.id.editar); // Colocarle el listener
        ImageView compartir  = (ImageView) vi.findViewById(R.id.compartir); // Colocarle el listener

        imageLoader.DisplayImage(productos.get(position).get(0), imagen);
        talla.setText(productos.get(position).get(1));
        tipo.setText(productos.get(position).get(2));
        modelo.setText(productos.get(position).get(3));
        color.setText(productos.get(position).get(4));
        precio.setText(productos.get(position).get(5));
        numeracion.setText(productos.get(position).get(6));

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(contexto, "Imagen presionada (Ampliar)", Toast.LENGTH_SHORT).show();
                // productos.get(position).get(0)
            }
        });

        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(contexto, "Editar presionada", Toast.LENGTH_SHORT).show();
                //productos.get(position).get(7)
            }
        });

        compartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(contexto, "Compartir presionada", Toast.LENGTH_SHORT).show();

                new compartirViaWhatsApp().execute(productos.get(position).get(0),
                        productos.get(position).get(4), productos.get(position).get(6));

                /*ConsultarProductos.shareButtonPressed(productos.get(position).get(0),
                        productos.get(position).get(4), productos.get(position).get(6));*/
            }
        });
        return vi;
    }

    /**
     * Clase para compartir Via WhatsApp en 2do plano
     */
    public class compartirViaWhatsApp extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(ac);
            pDialog.setTitle("Por favor espere...");
            pDialog.setMessage("Cargando imagen para compartirla...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(final String... params) {
            final Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            //share directly to WhatsApp and bypass the system picker
            sendIntent.setPackage("com.whatsapp");

            // modelo_nombre, color_producto, numeracion_producto

            final File archivo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                archivo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/" + params[0] + Constantes.EXTENSION_IMG);
            }
            else {
                archivo = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/" + params[0] + Constantes.EXTENSION_IMG);
            }

            try {
                if (archivo.exists()) {
                    Log.d(TAG, "Compartiendo imagen " + archivo.getAbsolutePath());
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                    Bitmap bitmapImage = Funciones.decodeSampledBitmapFromResource(archivo, 2160, 1620);
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                    String path = MediaStore.Images.Media.insertImage(contexto.getContentResolver(),
                            bitmapImage, "Descripcion", null);

                    final Uri imageUri2 = Uri.parse(path);

                    final Thread hilo = new Thread() {
                        @Override
                        public void run() {
                            synchronized (this) {
                                ac.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final String[]          opciones       = contexto.getResources().getStringArray(R.array.extrasViaWhatsApp);
                                        final ArrayList<String> mSelectedItems = new ArrayList<>();  // Where we track the selected items
                                        final ArrayList<String> datos          = new ArrayList<>();  // Where we track the selected items

                                        AlertDialog.Builder builder = new AlertDialog.Builder(ac);
                                        builder.setTitle(R.string.dialog_compartir_whatsapp_extra)
                                                // Specify the list array, the items to be selected by default (null for none),
                                                // and the listener through which to receive callbacks when items are selected
                                                .setMultiChoiceItems(R.array.extrasViaWhatsApp, null, new DialogInterface.OnMultiChoiceClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                        if (isChecked) {
                                                            // If the user checked the item, add it to the selected items
                                                            mSelectedItems.add(opciones[which]);

                                                            switch (opciones[which]) {
                                                                case "Modelo": {
                                                                    datos.add(params[0]);
                                                                    Log.d(TAG, "Modelo agregado " + params[0]);
                                                                }
                                                                break;
                                                                case "Color": {
                                                                    datos.add(params[1]);
                                                                    Log.d(TAG, "Color agregado " + params[1]);
                                                                }
                                                                break;
                                                                case "Numeracion": {
                                                                    datos.add(params[2]);
                                                                    Log.d(TAG, "Numeracion agregada " + params[2]);
                                                                }
                                                                break;
                                                            }

                                                            Log.d(TAG, "Seleccionaste " + opciones[which]);
                                                        }
                                                        else if (mSelectedItems.contains(opciones[which])) {
                                                            int pos = mSelectedItems.indexOf(opciones[which]);
                                                            Log.d(TAG, "Eliminaste " + opciones[which] + ": " + datos.get(pos));
                                                            mSelectedItems.remove(opciones[which]);
                                                            datos.remove(pos);
                                                        }
                                                    }
                                                })
                                                .setPositiveButton("Compartir", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        if (!mSelectedItems.isEmpty()) {
                                                            String txt_whatsapp = generarTextoWhatsAppCompartir(datos, mSelectedItems);
                                                            sendIntent.putExtra(Intent.EXTRA_TEXT, txt_whatsapp);
                                                            sendIntent.setType("text/plain");
                                                        }

                                                        Log.d(TAG, "Enviando al whatsApp");
                                                        dialog.cancel();

                                                        sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri2);
                                                        sendIntent.setType("image/*");
                                                        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                                        Log.d(TAG, "Starting activity ");
                                                        contexto.startActivity(sendIntent);
                                                    }
                                                })
                                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        Log.d(TAG, "Operacion cancelada");
                                                        dialog.cancel();
                                                    }
                                                });

                                        builder.show();
                                    }
                                });
                            }
                        }
                    };
                    hilo.start();
                }
                else {
                    Toast.makeText(contexto, "Ha ocurrido un error compartiendo la imagen.", Toast.LENGTH_LONG).show();
                }
            }
            catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(contexto, "Whatsapp no esta instalado.", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
        }

        /**
         * Genera el texto que se compartira con la imagen dentro del whatsapp.
         *
         * @param datos              Lista con los datos a compartir.
         * @param itemsSeleccionados Lista con los elementos que se van a compartir.
         * @return Cadena de texto que se va a compartir via WhatsApp
         */
        private String generarTextoWhatsAppCompartir(ArrayList<String> datos, ArrayList<String> itemsSeleccionados) {
            String texto = "";

            final String[] opciones   = contexto.getResources().getStringArray(R.array.extrasViaWhatsApp);
            List<String>   stringList = new ArrayList<>(Arrays.asList(opciones));

            if (itemsSeleccionados.contains("Numeracion")) {
                int pos   = itemsSeleccionados.indexOf("Numeracion");
                int start = Funciones.buscarCaracter(datos.get(pos), '(');
                int end   = Funciones.buscarCaracter(datos.get(pos), ')');
                datos.set(pos, datos.get(pos).substring(start + 1, end));
            }

            for (int i = 0; i < itemsSeleccionados.size(); i++) {
                if (stringList.contains(itemsSeleccionados.get(i))) {
                    if (texto.equals(""))
                        texto = itemsSeleccionados.get(i) + ": " + datos.get(i);
                    else
                        texto += ", " + itemsSeleccionados.get(i) + ": " + datos.get(i);
                }
            }

            return texto;
        }
    }
}