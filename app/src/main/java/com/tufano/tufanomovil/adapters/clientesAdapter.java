package com.tufano.tufanomovil.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tufano.tufanomovil.R;
import com.tufano.tufanomovil.gestion.clientes.EditarCliente;
import com.tufano.tufanomovil.objetos.Cliente;

import java.util.ArrayList;
import java.util.List;

public class clientesAdapter extends BaseAdapter
{
    private static final String         TAG      = "clientesAdapter";
    private static       LayoutInflater inflater = null;
    private Context contexto;
    private List<Cliente> clientes = new ArrayList<>();
    private Activity ac;
    private String   usuario;

    public clientesAdapter(Activity a, List<Cliente> datos, Context contexto, String usuario)
    {
        for (int i = 0; i < datos.size(); i++)
        {
            clientes.add(datos.get(i));
        }

        ac = a;
        this.contexto = contexto;
        this.usuario = usuario;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add(Cliente c)
    {
        clientes.add(c);
        Log.i(TAG, "Cliente agregado al adapter.");
    }

    public int getCount()
    {
        return clientes.size();
    }

    public Object getItem(int position)
    {
        return position;
    }

    public long getItemId(int position)
    {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View vi = convertView;

        if (convertView == null)
            vi = inflater.inflate(R.layout.consultar_cliente_items, null);

        TextView rs         = (TextView) vi.findViewById(R.id.razon_social);
        TextView rifTV      = (TextView) vi.findViewById(R.id.rif);
        TextView estadoTV   = (TextView) vi.findViewById(R.id.estado);
        TextView telefonoTV = (TextView) vi.findViewById(R.id.telefono);
        TextView emailTV    = (TextView) vi.findViewById(R.id.email);

        final String id_cliente   = clientes.get(position).getID();
        final String razon_social = clientes.get(position).getRazonSocial();
        final String rif          = clientes.get(position).getRif();
        final String estado       = clientes.get(position).getEstado();
        final String telefono     = clientes.get(position).getTelefono();
        final String email        = clientes.get(position).getEmail();
        final String direccion    = clientes.get(position).getDireccion();
        final String estatus      = clientes.get(position).getEstatus();
        ImageView    editar       = (ImageView) vi.findViewById(R.id.editar);

        rs.setText(razon_social);
        rifTV.setText(rif);
        estadoTV.setText(estado);
        telefonoTV.setText(telefono);
        emailTV.setText(email);

        editar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent c = new Intent(ac, EditarCliente.class);
                c.putExtra("usuario", usuario);
                c.putExtra("id_cliente", id_cliente);
                c.putExtra("rs", razon_social);
                c.putExtra("rif", rif);
                c.putExtra("estados", estado);
                c.putExtra("tlf", telefono);
                c.putExtra("mail", email);
                c.putExtra("dir", direccion);
                c.putExtra("estatus", estatus);
                c.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                contexto.startActivity(c);
            }
        });

        return vi;
    }
}