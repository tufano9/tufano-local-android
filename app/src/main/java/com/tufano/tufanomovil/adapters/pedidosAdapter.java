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
import com.tufano.tufanomovil.gestion.pedidos.DetallesPedido;
import com.tufano.tufanomovil.objetos.Pedido;

import java.util.ArrayList;
import java.util.List;

public class pedidosAdapter extends BaseAdapter
{
    private static final String         TAG      = "pedidosAdapter";
    private static       LayoutInflater inflater = null;
    private Context contexto;
    private List<Pedido> pedidos = new ArrayList<>();
    private Activity ac;
    private String   usuario;

    public pedidosAdapter(Activity a, List<Pedido> datos, Context contexto, String usuario)
    {
        for (int i = 0; i < datos.size(); i++)
        {
            pedidos.add(datos.get(i));
        }

        ac = a;
        this.contexto = contexto;
        this.usuario = usuario;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add(Pedido p)
    {
        pedidos.add(p);
        Log.i(TAG, "Pedido agregado al adapter.");
    }

    public int getCount()
    {
        return pedidos.size();
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
            vi = inflater.inflate(R.layout.consultar_pedido_items, null);

        final String id_pedido = pedidos.get(position).getId_pedido();
        final String vendedor  = pedidos.get(position).getNombre_vendedor();
        final String cliente   = pedidos.get(position).getRazon_social();
        final String fecha     = pedidos.get(position).getFecha_pedido();
        final String monto     = pedidos.get(position).getMonto_pedido();
        final String estatus   = pedidos.get(position).getEstatus_pedido();

        TextView  num_pedido      = (TextView) vi.findViewById(R.id.num_pedido);
        TextView  vendedor_pedido = (TextView) vi.findViewById(R.id.vendedor_pedido);
        TextView  cliente_pedido  = (TextView) vi.findViewById(R.id.cliente_pedido);
        TextView  fecha_pedido    = (TextView) vi.findViewById(R.id.fecha_pedido);
        TextView  monto_pedido    = (TextView) vi.findViewById(R.id.monto_pedido);
        TextView  estatus_pedido  = (TextView) vi.findViewById(R.id.estatus_pedido);
        ImageView editar          = (ImageView) vi.findViewById(R.id.editar_pedido);

        num_pedido.setText(id_pedido);
        vendedor_pedido.setText(vendedor);
        cliente_pedido.setText(cliente);
        fecha_pedido.setText(fecha);
        monto_pedido.setText(monto);
        estatus_pedido.setText(estatus);

        editar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent c = new Intent(ac, DetallesPedido.class);
                c.putExtra("usuario", usuario);
                c.putExtra("id_pedido", id_pedido);
                c.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                contexto.startActivity(c);
            }
        });

        return vi;
    }
}