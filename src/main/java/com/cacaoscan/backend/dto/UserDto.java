package com.cacaoscan.backend.dto;

import com.cacaoscan.backend.model.Rol;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String nombre;
    private String email;
    private Rol rol;

    public UserDto() {}

    public UserDto(UUID id, String nombre, String email, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
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
}
