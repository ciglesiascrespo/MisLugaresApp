package com.example.ciglesias_pc.pruebasainet.Lugares;

/**
 * Created by Ciglesias-pc on 06/06/2016.
 */
public class Lugar {

    private String Nombre,Descripcion, ImageEncoded;
    private Double Latitud,Longitud;

    public Lugar(String nombre, String descripcion, Double latitud, Double longitud,String imageEncoded) {
        Nombre = nombre;
        Descripcion = descripcion;
        Latitud = latitud;
        Longitud = longitud;
        ImageEncoded = imageEncoded;
    }

    public Double getLatitud() {
        return Latitud;
    }

    public Double getLongitud() {
        return Longitud;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public String getNombre() {
        return Nombre;
    }

    public String getImageEncoded() {
        return ImageEncoded;
    }
}
