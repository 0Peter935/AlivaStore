package com.store.erp.Models;

import java.time.LocalDateTime;

public class ClienteDTO {
    private Integer idCliente;
    private String nombreCompleto;
    private String telefono;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    public ClienteDTO() {
    }

    public ClienteDTO(Integer idCliente, String nombreCompleto, String telefono, LocalDateTime fechaRegistro,
            LocalDateTime fechaActualizacion) {
        this.idCliente = idCliente;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
