package com.store.erp.Models;

import java.time.LocalDateTime;

public class ClienteLogDTO {
    private Long idClienteLog;
    private Integer idCliente;
    private String nombreCompleto;
    private String telefono;
    private LocalDateTime fechaActividad;

    public ClienteLogDTO() {
    }

    public ClienteLogDTO(Long idClienteLog, Integer idCliente, String nombreCompleto, String telefono,
            LocalDateTime fechaActividad) {
        this.idClienteLog = idClienteLog;
        this.idCliente = idCliente;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.fechaActividad = fechaActividad;
    }

    public Long getIdClienteLog() {
        return idClienteLog;
    }

    public void setIdClienteLog(Long idClienteLog) {
        this.idClienteLog = idClienteLog;
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

    public LocalDateTime getFechaActividad() {
        return fechaActividad;
    }

    public void setFechaActividad(LocalDateTime fechaActividad) {
        this.fechaActividad = fechaActividad;
    }
}
