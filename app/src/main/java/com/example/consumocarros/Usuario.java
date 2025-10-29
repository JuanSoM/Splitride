package com.example.consumocarros;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Usuario implements Serializable {

    private String contrasena;
    private String usuario;
    public String nombre;
    public String apellidos;
    public List<String> coches; // lista de coches del usuario

    public Usuario(String contrasena, String usuario, String nombre, String apellidos) {
        this.contrasena = contrasena;
        this.usuario = usuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.coches = new ArrayList<>();
    }

    // Getters y setters
    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public List<String> getCoches() {
        return coches;
    }

    public void agregarCoche(String coche) {
        this.coches.add(coche);
    }


    @Override
    public String toString() {
        return "Usuario{" +
                "usuario='" + usuario + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", coches=" + coches +
                '}';
    }
}
