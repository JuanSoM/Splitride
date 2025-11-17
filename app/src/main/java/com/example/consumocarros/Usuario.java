package com.example.consumocarros;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Usuario implements Serializable {

    private String contrasena;
    private String usuario;
    public String nombre;
    public String apellidos;
    public List<Car> coches; // lista de coches del usuario
    public Car cochemasusado;
    private String idUsuario;
    private static SecureRandom random = new SecureRandom();


    public Usuario(String contrasena, String usuario, String nombre, String apellidos) {
        this.contrasena = contrasena;
        this.usuario = usuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.coches = new ArrayList<>();
        this.idUsuario = this.generarIDUsuario();
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    private String generarIDUsuario() {
        return new BigInteger(130, random).toString(32);
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
    public void aumentaruso(Car coche){

        for(Car rayo: this.coches){
            if(rayo.equals(coche)){
                rayo.vecesusado++;
                break;
            }
        }
    }
    public Car cochemasusado(){
        Car piston = new Car("rayo", "macqueen", "0","Copapisaton","joder","nosequemasponer");
        for(Car rayo: this.coches){
            if(piston.vecesusado<= rayo.vecesusado){
                piston = rayo;
            }
        }
        return piston;
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

    // --- CLASE Car MODIFICADA ---
    public static class Car implements Serializable {
        private String brand;
        private String model;
        private String year;
        // Campos nuevos
        private String cityKmpl;
        private String highwayKmpl;
        private String avgKmpl;
        private int capacidaddeposito;
        private int capacidadactual;
        private int vecesusado;

        // Constructor modificado
        public Car(String brand, String model, String year, String cityKmpl, String highwayKmpl, String avgKmpl) {
            this.brand = brand;
            this.model = model;
            this.year = year;
            this.cityKmpl = cityKmpl;
            this.highwayKmpl = highwayKmpl;
            this.avgKmpl = avgKmpl;
            this.capacidaddeposito = -1;
            this.vecesusado = 0;
            this.capacidadactual = -1;
        }

        public int getCapacidadactual() {
            return capacidadactual;
        }

        public void setCapacidadactual(int capacidadactual) {
            this.capacidadactual = capacidadactual;
        }

        // Getters antiguos
        public String getBrand() { return brand; }
        public String getModel() { return model; }
        public String getYear() { return year; }

        // Getters nuevos
        public String getCityKmpl() { return cityKmpl; }
        public String getHighwayKmpl() { return highwayKmpl; }
        public String getAvgKmpl() { return avgKmpl; }
        public int getcapacidaddeposito() { return capacidaddeposito; }
        public int getvecesusado() { return vecesusado; }

        public void setCapacidaddeposito(int capacidaddeposito) {
            this.capacidaddeposito = capacidaddeposito;
        }

        @Override
        public String toString() {
            // Modificamos el toString para que sea más útil
            return brand + " " + model + " " + year + " (Avg: " + avgKmpl + " km/L)";
        }
    }
}