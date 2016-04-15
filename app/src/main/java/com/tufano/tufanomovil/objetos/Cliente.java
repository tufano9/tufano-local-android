package com.tufano.tufanomovil.objetos;

public class Cliente
{
    private String id, razon_social, rif, estado, telefono, email, direccion, estatus;

    public Cliente(String id, String razon_social, String rif, String estado, String telefono,
                   String email, String direccion, String estatus)
    {
        this.id = id;
        this.razon_social = razon_social;
        this.rif = rif;
        this.estado = estado;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.estatus = estatus;
    }

    public String getID()
    {
        return id;
    }

    public String getRazonSocial()
    {
        return razon_social;
    }

    public String getRif()
    {
        return rif;
    }

    public String getEstado()
    {
        return estado;
    }

    public String getTelefono()
    {
        return telefono;
    }

    public String getEmail()
    {
        return email;
    }

    public String getDireccion()
    {
        return direccion;
    }

    public String getEstatus()
    {
        return estatus;
    }
}