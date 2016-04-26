package com.tufano.tufanomovil.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.tufano.tufanomovil.global.Funciones;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import static com.tufano.tufanomovil.database.altern.Producto.ALTER_TABLE_PRODUCTO_ADDDESTACADO;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_CLIENTES;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_COLORES;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_PEDIDOS;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_PEDIDOS_DETALLES;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_PEDIDOS_DETALLES_EDITAR;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_PEDIDOS_EDITAR;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_PEDIDOS_TEMPORALES;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_PRODUCTO;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_TALLAS;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_TIPOS;
import static com.tufano.tufanomovil.database.creates.CreateSentences.CREAR_TABLA_USUARIO;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_APELLIDO;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_CEDULA;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_EMAIL;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_ESTADO;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_KEY;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_NOMBRE;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_NOMBRE_USUARIO;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_PASSWORD;
import static com.tufano.tufanomovil.database.tables.Usuarios.CN_TELEFONO;
import static com.tufano.tufanomovil.database.tables.Usuarios.TABLA_USUARIO;

/**
 * Created por Usuario Tufano on 11/01/2016.
 */
public class DBHelper extends SQLiteOpenHelper
{
    //Ruta por defecto de las bases de datos en el sistema Android
    //private static String DB_PATH =  "/data/data/paquete.tufanoapp/databases/";

    public static final  String DB_NAME    = "tufanomovil.db";
    private final static int    DB_VERSION = 3;
    private static final String TAG        = "DBHelper";
    private static String DB_RUTA;
    private static DBHelper mInstance = null;
    private final Context myContext;
    private SQLiteDatabase myDataBase;

    /**
     * Constructor
     * <p/>
     * Toma referencia hacia el contexto de la aplicación que lo invoca para poder acceder a los
     * 'assets' y 'resources' de la aplicación.
     * <p/>
     * Crea un objeto DBOpenHelper que nos permitirá controlar la apertura de la base de datos.
     * <p/>
     * Constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     *
     * @param context Contexto de la app
     */
    private DBHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
        this.myContext = context;
        DB_RUTA = context.getDatabasePath(DB_NAME).toString();

        //Log.i("DBHelper", "(" + context.getFilesDir().getPath() + ") DB_RUTA:  (" + context.getDatabasePath(DB_NAME));
    }

    public static DBHelper getInstance(Context ctx)
    {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null)
        {
            mInstance = new DBHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @SuppressWarnings("unused")
    public static void deleteDB(Context contexto)
    {
        Log.i(TAG, "deleteDB initiation..");
        contexto.deleteDatabase(DBHelper.DB_NAME);
        Log.i(TAG, "deleteDB finished..");
    }

    @SuppressWarnings("unused")
    public static void backUpDB()
    {
        try
        {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            {
                Log.i(TAG, "backUpDB initiation..");

                File output_folder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
                {
                    output_folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/backup");
                }
                else
                {
                    output_folder = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/backup");
                }
                if (!output_folder.exists())
                {
                    if (output_folder.mkdirs())
                        Log.i(TAG, "Carpeta \"" + output_folder.getPath() + "\" creada exitosamente");
                    else
                    {
                        Log.e(TAG, "La carpeta no pudo ser creada..");
                    }
                }

                File            dbFile = new File(DB_RUTA);
                FileInputStream fis    = new FileInputStream(dbFile);

                String outFileName = output_folder.getPath() + "/db_copy.db";
                //Log.i(TAG, "1) backUpDB will be done in the following the directory: " + outFileName);
                //Log.i(TAG, "2) backUpDB will be done in the following the directory: " + Environment.getExternalStorageDirectory() + "/db_copy.db");

                // Open the empty db as the output stream
                OutputStream output = new FileOutputStream(outFileName);

                // Transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int    length;

                while ((length = fis.read(buffer)) > 0)
                {
                    output.write(buffer, 0, length);
                }
                output.flush();
                output.close();
                fis.close();
                Log.i(TAG, "backUpDB finished in following the directory: " + outFileName
                        + ", origin: " + DB_RUTA);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void recoverDB(Context contexto)
    {
        try
        {
            Log.i(TAG, "recoverDB INIT");
            //File dbFile = new File(DB_RUTA);

            File output_folder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
            {
                output_folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/backup");
            }
            else
            {
                output_folder = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/backup");
            }
            if (!output_folder.exists())
            {
                if (output_folder.mkdirs())
                    Log.i(TAG, "Carpeta \"" + output_folder.getPath() + "\" creada exitosamente");
                else
                {
                    Log.e(TAG, "La carpeta no pudo ser creada..");
                }
            }

            String copy_dir = output_folder.getPath() + "/db_copy.db";
            // Open the empty db as the input stream
            FileInputStream input = new FileInputStream(copy_dir);

            OutputStream output = new FileOutputStream(DB_RUTA);

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;

            while ((length = input.read(buffer)) > 0)
            {
                output.write(buffer, 0, length);
            }

            // Close the streams
            output.flush();
            output.close();
            output.close();
            Log.i(TAG, "recoverDB finished from: " + copy_dir + " to: " + DB_RUTA);
            Toast.makeText(contexto, "Se ha restaurado la base de datos exitosamente!", Toast.LENGTH_LONG).show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Crea una base de datos vacía en el sistema y la reescribe con nuestro fichero de base de datos.
     */
    private void createDataBase()
    {
        boolean dbExist = checkDataBase();
        Log.i("DBHelper", "createDataBase!");

        if (dbExist)
        {
            //la base de datos existe y no hacemos nada.
            Log.i("DBHelper", "dbExist!");
        }
        else
        {
            //Llamando a este método se crea la base de datos vacía en la ruta por defecto del sistema
            //de nuestra aplicación por lo que podremos sobreescribirla con nuestra base de datos.
            this.getReadableDatabase();
            Log.i("DBHelper", "!dbExist!");

            try
            {
                copyDataBase();
            }
            catch (IOException e)
            {
                throw new Error("Error copiando Base de Datos");
            }
        }
    }

    /**
     * Comprueba si la base de datos existe para evitar copiar siempre el fichero cada vez que se abra la aplicación.
     *
     * @return true si existe, false si no existe
     */
    private boolean checkDataBase()
    {
        Log.i("checkDataBase", "INTRO");
        SQLiteDatabase checkDB = null;

        try
        {
            //String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(DB_RUTA, null, SQLiteDatabase.OPEN_READONLY);
        }
        catch (SQLiteException e)
        {
            //si llegamos aqui es porque la base de datos no existe todavía.
        }
        if (checkDB != null)
        {
            checkDB.close();
        }
        return checkDB != null;
    }

    /**
     * Copia nuestra base de datos desde la carpeta assets a la recién creada
     * base de datos en la carpeta de sistema, desde dónde podremos acceder a ella.
     * Esto se hace con bytestream.
     */
    private void copyDataBase() throws IOException
    {
        Log.i("copyDataBase", "INTRO");
        //Abrimos el fichero de base de datos como entrada
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        //Ruta a la base de datos vacía recién creada
        //String outFileName = DB_PATH + DB_NAME;

        //Abrimos la base de datos vacía como salida
        OutputStream myOutput = new FileOutputStream(DB_RUTA);

        //Transferimos los bytes desde el fichero de entrada al de salida
        byte[] buffer = new byte[1024];
        int length;

        while ((length = myInput.read(buffer)) > 0)
        {
            myOutput.write(buffer, 0, length);
        }

        //Liberamos los streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    @SuppressWarnings("unused")
    public void open()
    {
        //Abre la base de datos
        createDataBase();
        //String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(DB_RUTA, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close()
    {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.i(TAG, "Creando Base de datos");
        // Crear la base de datos
        //db.execSQL("PRAGMA encoding = \"UTF-8\"");
        db.execSQL(CREAR_TABLA_USUARIO);
        db.execSQL(CREAR_TABLA_PRODUCTO);
        db.execSQL(CREAR_TABLA_TALLAS);
        db.execSQL(CREAR_TABLA_TIPOS);
        db.execSQL(CREAR_TABLA_COLORES);
        db.execSQL(CREAR_TABLA_CLIENTES);
        db.execSQL(CREAR_TABLA_PEDIDOS);
        db.execSQL(CREAR_TABLA_PEDIDOS_DETALLES);
        db.execSQL(CREAR_TABLA_PEDIDOS_TEMPORALES);
        db.execSQL(CREAR_TABLA_PEDIDOS_EDITAR);
        db.execSQL(CREAR_TABLA_PEDIDOS_DETALLES_EDITAR);

        /*
            El siguiente bloque Try-Catch crea un usuario por defecto con los siguientes datos:

            -Usuario: ftufano
            -Password: 1324
         */

        try
        {
            // Crea un key aleatoriamente que sera usado para la encriptacion.
            String key = Funciones.generateKey();
            String default_password = "1234";

            //Encripta el password por defecto utilizando el key previamente generado.
            String generatedPass = new String(Funciones.encrypt(key, default_password), "UTF-8");

            ContentValues valores = new ContentValues();
            valores.put(CN_NOMBRE_USUARIO, "ftufano");
            valores.put(CN_CEDULA, "20123456");
            valores.put(CN_NOMBRE, "Francesco");
            valores.put(CN_APELLIDO, "Tufano");
            valores.put(CN_TELEFONO, "0414123456");
            valores.put(CN_EMAIL, "tufano9@yahoo.es");
            valores.put(CN_PASSWORD, generatedPass);
            valores.put(CN_ESTADO, "Carabobo");
            valores.put(CN_KEY, key);

            if (db.insert(TABLA_USUARIO, null, valores) == -1)
                Log.e(TAG, "El usuario por defecto no pudo ser creado..");
            else
                Log.i(TAG, "El usuario por defecto fue creado exitosamente..");
        }
        catch (UnsupportedEncodingException | GeneralSecurityException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Actualizar la base de datos (Tomando en cuenta la version)
        Log.i("DBHelper", "Se actualizara la BD desde la version " + oldVersion + ", a la version " + newVersion);

        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion)
        {
            Log.i("DBHelper", "Actualizando BD a la version " + upgradeTo);
            switch (upgradeTo)
            {
                case 2:
                    db.execSQL(CREAR_TABLA_PEDIDOS_EDITAR);
                    db.execSQL(CREAR_TABLA_PEDIDOS_DETALLES_EDITAR);
                    break;
                case 3:
                    db.execSQL(ALTER_TABLE_PRODUCTO_ADDDESTACADO);
                    break;
            }
            upgradeTo++;
        }
    }
}