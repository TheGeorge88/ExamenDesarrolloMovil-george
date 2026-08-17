package com.example.veterinarialemas.models;

import com.google.firebase.firestore.Exclude;

public class AsistenciaMedicaModels {

    public AsistenciaMedicaModels(String nombreDuenio, String nombreMascota, String tipoMascota, String razaMascota, String edadMascota) {
        this.nombreDuenio = nombreDuenio;
        this.nombreMascota = nombreMascota;
        this.tipoMascota = tipoMascota;
        this.razaMascota = razaMascota;
        EdadMascota = edadMascota;
    }

    public AsistenciaMedicaModels() {
        // Constructor vacío requerido por Firebase
    }

    private String nombreDuenio;
    private String nombreMascota;
    private String tipoMascota;
    private String razaMascota;
    private String EdadMascota;
    private String urlImagen;

    private String id;

    public String getNombreDuenio() {
        return nombreDuenio;
    }

    public void setNombreDuenio(String nombreDuenio) {
        this.nombreDuenio = nombreDuenio;
    }

    public String getNombreMascota() {
        return nombreMascota;
    }

    public void setNombreMascota(String nombreMascota) {
        this.nombreMascota = nombreMascota;
    }

    public String getTipoMascota() {
        return tipoMascota;
    }

    public void setTipoMascota(String tipoMascota) {
        this.tipoMascota = tipoMascota;
    }

    public String getRazaMascota() {
        return razaMascota;
    }

    public void setRazaMascota(String razaMascota) {
        this.razaMascota = razaMascota;
    }

    public String getEdadMascota() {
        return EdadMascota;
    }

    public void setEdadMascota(String edadMascota) {
        EdadMascota = edadMascota;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
