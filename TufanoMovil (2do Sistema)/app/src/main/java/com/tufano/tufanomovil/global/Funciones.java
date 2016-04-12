package com.tufano.tufanomovil.global;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.tufano.tufanomovil.R;

import java.io.File;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * Creado por Gerson on 11/01/2016.
 */
public class Funciones
{
    private static final String TAG = "Funciones";
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Metodo para encriptar un texto.
     * @param key El Key aleatoriamente generado con el cual se va a encriptar
     * @param plainText El Texto a encriptar
     * @return El texto encriptado bajo un array de bytes.
     * @throws GeneralSecurityException
     */
    public static byte[] encrypt(String key, String plainText) throws GeneralSecurityException
    {
        SecretKey secret_key = new SecretKeySpec(key.getBytes(), Constantes.ALGORITM);

        Cipher cipher = Cipher.getInstance(Constantes.ALGORITM);
        cipher.init(Cipher.ENCRYPT_MODE, secret_key);

        return cipher.doFinal(plainText.getBytes());
    }

    /**
     * Metodo para des-encriptar un texto.
     * @param key El Key aleatoriamente generado con el cual se va a des-encriptar
     * @param encryptedText El Texto a des-encriptar
     * @return El texto des-encriptado.
     * @throws GeneralSecurityException
     */
    @SuppressWarnings("unused")
    public static String decrypt(String key, byte[] encryptedText) throws GeneralSecurityException
    {
        SecretKey secret_key = new SecretKeySpec(key.getBytes(), Constantes.ALGORITM);

        Cipher cipher = Cipher.getInstance(Constantes.ALGORITM);
        cipher.init(Cipher.DECRYPT_MODE, secret_key);

        byte[] decrypted = cipher.doFinal(encryptedText);

        return new String(decrypted);
    }

    /**
     * Busca la posicion de la cadena ingresada dentro del spinner dado.
     * @param cadena Cadena a buscar.
     * @param spinner Spinner donde se buscara la cadena
     * @return Posicion de la cadena dentro del spinner. Retorna -1 si no lo encontro.
     */
    public static int buscarPosicionElemento(String cadena, Spinner spinner)
    {
        Log.i(TAG, "Buscando al elemento '" + cadena + "'");
        Adapter adap = spinner.getAdapter();

        for (int i = 0; i < adap.getCount(); i++)
        {
            //Log.i(TAG, "Comparando con '"+adap.getItem(i).toString()+"'");
            if(adap.getItem(i).toString().equals(cadena))
            {
                Log.i(TAG, "Elemento "+cadena+" encontrado!!");
                return i;
            }
        }
        return -1;
    }

    /**
     * Metodo para buscar la posicion de un caracter dentro de una cadena.
     * @param cadena Cadena en la cual se buscara el caracter dado.
     * @param busqueda Caracter a buscar dentro de la cadena.
     * @return La posicion dentro de la cadena en la cual se ubica el caracter.
     */
    public static int buscarCaracter(String cadena, char busqueda)
    {
        for (int i = 0; i < cadena.length(); i++)
        {
            if (cadena.charAt(i) == busqueda)
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Generate a value suitable for use in .
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId()
    {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue))
            {
                Log.i(TAG, "Generated ID: "+result);
                return result;
            }
        }
    }

    /**
     * Verifica si el email es valido
     * @param target Email a validar.
     * @return True si el email es valido, false en caso contrario.
     */
    public static boolean isValidEmail(CharSequence target)
    {
        return TextUtils.isEmpty(target) || !android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /**
     * Metodo para convertir una cadena a su equivalente con palabras capitalizadas.
     * @param line La cadena a gestionar.
     * @return La cadena con las palabras capitalizadas.
     */
    public static String capitalizeWords(final String line)
    {
        String res = String.valueOf(Character.toUpperCase(line.charAt(0)));

        for (int i = 1; i < line.length(); i++)
        {
            if( i>1 && String.valueOf(line.charAt(i-1)).equals(" ") )
                res += Character.toUpperCase(line.charAt(i));
            else
                res += Character.toLowerCase(line.charAt(i));
        }
        return res;
    }

    /**
     * Formatea una cadena que contiene un numero de telefono. Por Ej, 0212-500.1015.
     * @param telefono Numero de telefono a formatear.
     * @return Numero de telefono con el formato (Guion + Punto).
     */
    public static String formatoTelefono(String telefono)
    {
        return telefono.substring(0,4) + "-" + telefono.substring(4, 7) + "." + telefono.substring(7);
    }

    /**
     * Formatea una cadena que contiene un rif. Por Ej, J-34.000.111.
     * @param rif Rif a formatear.
     * @return Rif con el formato (Guion + Puntos).
     */
    public static String formatoRif(String rif)
    {
        String newRif = rif.substring(0,1) + "-" + rif.substring(1);
        if(newRif.length()==12)
            newRif = newRif.substring(0,4) + "." + newRif.substring(4,7) + "." + newRif.substring(7);
        else if(newRif.length()==11)
            newRif = newRif.substring(0,3) + "." + newRif.substring(3,6) + "." + newRif.substring(6);
        return newRif;
    }

    /**
     * Formatea el precio ingresado con separadores (Comas y Puntos)
     * @param precio El precio que necesita ser formateado.
     * @return El precio con el formato propuesto.
     */
    public static String formatoPrecio(String precio)
    {
        final DecimalFormat priceFormat = new DecimalFormat("###,###.##");
        return priceFormat.format(Double.parseDouble(precio));
    }

    /**
     * Busca el drawable equivalente al valor int (R.drawable.00000) ingresado.
     * @param contexto Contexto de la aplicacion.
     * @param image Valor entero del drawable.
     * @return Drawable obtenido a partir del Int ingresado.
     */
    public static Drawable intToDrawable(Context contexto, int image)
    {
        return ContextCompat.getDrawable(contexto, image);

            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            {
                return resources.getDrawable(id, context.getTheme());
            }
            else
            {
                return resources.getDrawable(id);
            }*/
    }

    /**
     * Generador del key aleatorio para usarse en conjunto con el usuario.
     * @return El key generado.
     */
    public static String generateKey()
    {
        try
        {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // for example
            SecretKey secretKey = keyGen.generateKey();

            Log.d(TAG, "Secret Key generated succesfully.. (" +
                    Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT) + ")");

            /* get base64 encoded version of the key */
            return Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Metodo para obtener el Drawable de Flecha Ascendente utilizado en las tablas.
     * @return El drawable de la imagen.
     */
    @SuppressWarnings("SameReturnValue")
    public static int getAscDrawable()
    {
        //return ContextCompat.getDrawable(contexto, android.R.drawable.arrow_up_float);
        //return ResourcesCompat.getDrawable(getResources(), android.R.drawable.arrow_up_float, null);
        return R.drawable.arrow_up;
    }

    /**
     * Metodo para obtener el Drawable de Flecha Descendente utilizado en las tablas.
     * @return El drawable de la imagen.
     */
    @SuppressWarnings("SameReturnValue")
    public static int getDescDrawable()
    {
        return R.drawable.arrow_down;
        //return ContextCompat.getDrawable(contexto, android.R.drawable.arrow_down_float);
        //return getResources().getDrawable(android.R.drawable.arrow_up_float);
    }

    /**
     * Metodo para setear los datos por defecto del login (Para efectos de DEBUG)
     * @param username Nombre de usuario a utilizar.
     * @param password Contraseña a utilizar.
     * @param campo_usuario_login TextView a rellenar con el usuario.
     * @param campo_contrasena_login TextView a rellenar con la contraseña.
     */
    @SuppressWarnings("SameParameterValue")
    public static void setLoginCredentials(String username, String password, TextView campo_usuario_login, TextView campo_contrasena_login)
    {
        campo_usuario_login.setText(username);
        campo_contrasena_login.setText(password);
    }

    /**
     * Crear un equivalente bajo en peso de la imagen.
     * @param file Archivo donde se encuentra la imagen.
     * @param reqWidth Ancho requerido.
     * @param reqHeight Alto requerido.
     * @return Imagen optimizada.
     */
    public static Bitmap decodeSampledBitmapFromResource(File file, int reqWidth, int reqHeight)
    {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //BitmapFactory.decodeResource(res, resId, options);
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        //return BitmapFactory.decodeResource(res, resId, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Metodo utilizado para imprimir las tablas existentes en la BD.
     * @param db Base de Datos sqlite.
     */
    @SuppressWarnings("unused")
    public static void imprimirBaseDatos(SQLiteDatabase db)
    {
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst())
        {
            while ( !c.isAfterLast() )
            {
                imprimirTabla(db, c.getString(0));
                c.moveToNext();
            }
        }

        c.close();
    }

    /**
     * Imprime la tabla indicada.
     * @param db Base de Datos sqlite.
     * @param tableName Nombre de la tabla a imprimir.
     */
    private static void imprimirTabla(SQLiteDatabase db, String tableName)
    {
        Log.d(TAG, "Imprimiento tabla: "+tableName);
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + tableName, null);

        if (allRows.moveToFirst() )
        {
            String[] columnNames = allRows.getColumnNames();
            do
            {
                for (String name: columnNames)
                {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            }
            while (allRows.moveToNext());
        }

        allRows.close();

        Log.d(TAG, tableString);
    }

    private void mostrarGestionTipos(String usuario, Context contexto, Context from, Class<?> where)
    {
        Intent c = new Intent(from, where);
        c.putExtra("usuario", usuario);
        contexto.startActivity(c);
    }
}
