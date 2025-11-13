package com.example.consumocarros;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Usuario implements Serializable {

    private String contrasena;
    private String usuario;
    public String nombre;
    public String apellidos;
    public List<Car> coches; // lista de coches del usuario

    public Usuario(String contrasena, String usuario, String nombre, String apellidos) {
        this.contrasena = contrasena;
        this.usuario = usuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.coches = new ArrayList<>();
    }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public List<Car> getCoches() { return coches; }

    public void agregarCoche(Car coche) {
        this.coches.add(coche);
    }

    public void eliminarCoche(Car coche) {
        this.coches.remove(coche);
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

    public static class Car implements Serializable {
        private String brand;
        private String model;
        private String year;

        public Car(String brand, String model, String year) {
            this.brand = brand;
            this.model = model;
            this.year = year;
        }

        public String getBrand() { return brand; }
        public String getModel() { return model; }
        public String getYear() { return year; }

        @Override
        public String toString() {
            return brand + " " + model + " " + year;
        }
    }
}
