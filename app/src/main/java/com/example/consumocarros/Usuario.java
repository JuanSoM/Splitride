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
    public List<Car> coches;
    public List<Viaje> viajes;
    public List<String> listaAmigosIds;
    public Car cochemasusado;
    private String idUsuario;
    private static SecureRandom random = new SecureRandom();

    public Usuario(String contrasena, String usuario, String nombre, String apellidos) {
        this.contrasena = contrasena;
        this.usuario = usuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.coches = new ArrayList<>();
        this.viajes = new ArrayList<>();
        this.listaAmigosIds = new ArrayList<>();
        this.idUsuario = this.generarIDUsuario();
    }

    // --- Métodos para Viajes ---
    public List<Viaje> getViajes() {
        if (viajes == null) {
            viajes = new ArrayList<>();
        }
        return viajes;
    }

    public void agregarViaje(Viaje viaje) {
        if (viajes == null) {
            viajes = new ArrayList<>();
        }
        this.viajes.add(viaje);
    }
    // --- FIN ---

    // --- MÉTODOS PARA AMIGOS ---
    public List<String> getListaAmigosIds() {
        if (listaAmigosIds == null) {
            listaAmigosIds = new ArrayList<>();
        }
        return listaAmigosIds;
    }

    public void agregarAmigo(String idAmigo) {
        if (this.listaAmigosIds == null) {
            this.listaAmigosIds = new ArrayList<>();
        }
        if (!this.listaAmigosIds.contains(idAmigo)) {
            this.listaAmigosIds.add(idAmigo);
        }
    }
    // --- FIN ---

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

    public List<Car> getCoches() {
        if(coches == null) coches = new ArrayList<>();
        return coches;
    }

    public void agregarCoche(Car coche) {
        if(coches == null) coches = new ArrayList<>();
        this.coches.add(coche);
    }

    public void eliminarCoche(Car coche) {
        if(coches != null) this.coches.remove(coche);
    }

    public void aumentaruso(Car coche){
        if(coches == null) return;
        for(Car rayo: this.coches){
            // Nota: Para que esto funcione bien al 100%, Car debería implementar equals()
            // Pero por ahora comparamos referencias u objetos
            if(rayo.equals(coche)){
                rayo.vecesusado++;
                break;
            }
        }
    }

    public Car cochemasusado(){
        if(coches == null || coches.isEmpty()) return null;
        Car piston = coches.get(0);
        for(Car rayo: this.coches){
            if(piston.vecesusado <= rayo.vecesusado){
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

    // --- Clase Viaje ---
    public static class Viaje implements Serializable {
        private final String origen;
        private final String destino;
        private final double distanciaKm;
        private final double litrosConsumidos;
        private final String cocheUsado;
        private final long timestamp;

        public Viaje(String origen, String destino, double distanciaKm, double litrosConsumidos, String cocheUsado) {
            this.origen = origen;
            this.destino = destino;
            this.distanciaKm = distanciaKm;
            this.litrosConsumidos = litrosConsumidos;
            this.cocheUsado = cocheUsado;
            this.timestamp = System.currentTimeMillis();
        }

        public String getOrigen() { return origen; }
        public String getDestino() { return destino; }
        public double getDistanciaKm() { return distanciaKm; }
        public double getLitrosConsumidos() { return litrosConsumidos; }
        public String getCocheUsado() { return cocheUsado; }
        public long getTimestamp() { return timestamp; }
    }

    // --- Clase Car (YA ACTUALIZADA) ---
    public static class Car implements Serializable {
        private String brand;
        private String model;
        private String year;

        // Estos son los 3 campos nuevos para guardar lo que viene de MySQL
        private String cityKmpl;
        private String highwayKmpl;
        private String avgKmpl;

        private int capacidaddeposito;
        private int capacidadactual;
        private int vecesusado;

        // El constructor ya recibe los 6 parámetros. ¡Perfecto!
        public Car(String brand, String model, String year, String cityKmpl, String highwayKmpl, String avgKmpl) {
            this.brand = brand;
            this.model = model;
            this.year = year;
            this.cityKmpl = cityKmpl;
            this.highwayKmpl = highwayKmpl;
            this.avgKmpl = avgKmpl;
            this.capacidaddeposito = -1; // Valor por defecto
            this.vecesusado = 0;
            this.capacidadactual = -1;
        }

        // Getters y Setters
        public int getCapacidadactual() { return capacidadactual; }
        public void setCapacidadactual(int capacidadactual) { this.capacidadactual = capacidadactual; }

        public String getBrand() { return brand; }
        public String getModel() { return model; }
        public String getYear() { return year; }

        // Kotlin usará estos Getters automáticamente cuando llames a .cityKmpl
        public String getCityKmpl() { return cityKmpl; }
        public String getHighwayKmpl() { return highwayKmpl; }
        public String getAvgKmpl() { return avgKmpl; }

        public int getcapacidaddeposito() { return capacidaddeposito; }
        public void setCapacidaddeposito(int capacidaddeposito) { this.capacidaddeposito = capacidaddeposito; }

        public int getvecesusado() { return vecesusado; }

        @Override
        public String toString() {
            return brand + " " + model + " " + year;
        }
    }
}