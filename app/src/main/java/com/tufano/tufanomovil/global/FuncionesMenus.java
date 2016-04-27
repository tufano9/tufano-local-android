package com.tufano.tufanomovil.global;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.database.DBHelper;
import com.tufano.tufanomovil.gestion.perfil.EditarPerfil;

import java.io.File;

public class FuncionesMenus
{
    /**
     * Metodo con el cual se mostrara el perfil del usuario conectado actualmente.
     */
    public static void mostrarPerfil(String usuario, Context contexto, Context from)
    {
        Intent c = new Intent(from, EditarPerfil.class);
        c.putExtra("usuario", usuario);
        c.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        contexto.startActivity(c);
    }

    /**
     * Metodo con el cual se mostrara las configuraciones del usuario conectado actualmente.
     */
    public static void mostrarConfiguracion(String usuario, Context contexto, Context from)
    {
        /*Intent c = new Intent(from, EditarPerfil.class);
        c.putExtra("usuario", usuario);
        c.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        contexto.startActivity(c);*/
    }

    public static void getBackup(Context contexto, Context from)
    {
        verifyDB task = new verifyDB(contexto, from);
        task.execute();
    }

    public static void doBackup(Context contexto, Context from)
    {
        backUpDB task = new backUpDB(contexto, from);
        task.execute();
    }

    /**
     * Hace una verificacion en segundo plano usando la base de datos para determinar si existe
     * un respaldo de la base de datos, y poder asi recuperarla y utilizarla.
     */
    static class verifyDB extends AsyncTask<String, String, String>
    {
        private Context contexto, from;

        public verifyDB(Context contexto, Context from)
        {
            this.contexto = contexto;
            this.from = from;
        }

        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                //enviamos y recibimos y analizamos los datos en segundo plano.
                if (existCopyDB())
                {
                    return "yes";
                }
                else
                {
                    return "no";
                }
            }
            catch (RuntimeException e)
            {
                Log.d("verifyDB", "err2: " + e);
                return "err";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            switch (result)
            {
                case "yes":
                    AlertDialog.Builder dialog = new AlertDialog.Builder(from);

                    dialog.setTitle(R.string.confirmacion);
                    dialog.setMessage(R.string.confirmacion_recuperar_bd);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("SI", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            DBHelper.recoverDB(contexto);
                        }
                    });

                    dialog.setNegativeButton("NO", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                    break;
                case "no":
                    Toast.makeText(contexto, "No se pudo localizar ningun tipo de respaldo!",
                            Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(contexto, "Ha ocurrido un error, por favor intentelo nuevamente!",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }

        private boolean existCopyDB()
        {
            File output_folder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
            {
                output_folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/TufanoMovilFiles/backup/db_copy.db");
            }
            else
            {
                output_folder = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TufanoMovilFiles/backup/db_copy.db");
            }

            File file_copy = new File(output_folder.getPath());

            /*long size_copy = file_copy.length();

            File file_original = new File(contexto.getDatabasePath(DBHelper.DB_NAME).toString());
            long size_original = file_original.length();

            if(size_copy>size_original)
            {
                Log.i(TAG, "La BD de respaldo es mas grande.. (Posible respaldo) "+size_copy+">"+size_original);
                Log.i(TAG, "file_copy_dir: "+file_copy.getPath()+", file_original_dir: "+file_original.getPath());
                return true;
            }
            else
            {
                Log.i(TAG, "La BD de respaldo es mas pequeña.. (No respaldar) "+size_copy+"<"+size_original);
                Log.i(TAG, "file_copy_dir: "+file_copy.getPath()+", file_original_dir: "+file_original.getPath());
                return false;
            }*/

            return file_copy.exists();
        }
    }

    /**
     * Hace una verificacion en segundo plano usando la base de datos para determinar si existe
     * un respaldo de la base de datos, y poder asi recuperarla y utilizarla.
     */
    static class backUpDB extends AsyncTask<String, String, String>
    {
        private Context contexto, from;
        private AlertDialog.Builder dialog;

        public backUpDB(Context contexto, Context from)
        {
            this.contexto = contexto;
            this.from = from;
        }

        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected String doInBackground(String... params)
        {
            dialog = new AlertDialog.Builder(from);

            dialog.setTitle(R.string.confirmacion);
            dialog.setMessage(R.string.confirmacion_respaldar_bd);
            dialog.setCancelable(false);
            dialog.setPositiveButton("SI", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    DBHelper.backUpDB(contexto);
                }
            });

            dialog.setNegativeButton("NO", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            dialog.show();
        }
    }
}
