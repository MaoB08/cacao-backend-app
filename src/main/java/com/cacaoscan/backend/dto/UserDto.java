package com.cacaoscan.backend.dto;

import com.cacaoscan.backend.model.Rol;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String nombre;
    private String email;
    private Rol rol;

    private String telefono;
    private String departamento;
    private String municipio;
    private String nombreFinca;

    public UserDto() {}

    public UserDto(UUID id, String nombre, String email, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    public UserDto(UUID id, String nombre, String email, Rol rol, String telefono, String departamento, String municipio, String nombreFinca) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.telefono = telefono;
        this.departamento = departamento;
        this.municipio = municipio;
        this.nombreFinca = nombreFinca;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getNombreFinca() {
        return nombreFinca;
    }

    public void setNombreFinca(String nombreFinca) {
        this.nombreFinca = nombreFinca;
    }
}
