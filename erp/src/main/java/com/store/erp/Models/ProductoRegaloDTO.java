package com.store.erp.Models;

import java.time.LocalDateTime;

public class ProductoRegaloDTO {
    private Integer idRegalo;
    private String codProducto;
    private String descProducto;
    private String imagen;
    private LocalDateTime fechaRegistro;
    private Boolean estadoRegalo;

    public ProductoRegaloDTO() {
    }

    public ProductoRegaloDTO(Integer idRegalo, String codProducto, String descProducto, String imagen,
            LocalDateTime fechaRegistro, Boolean estadoRegalo) {
        this.idRegalo = idRegalo;
        this.codProducto = codProducto;
        this.descProducto = descProducto;
        this.imagen = imagen;
        this.fechaRegistro = fechaRegistro;
        this.estadoRegalo = estadoRegalo;
    }

    public Integer getIdRegalo() {
        return idRegalo;
    }

    public void setIdRegalo(Integer idRegalo) {
        this.idRegalo = idRegalo;
    }

    public String getCodProducto() {
        return codProducto;
    }

    public void setCodProducto(String codProducto) {
        this.codProducto = codProducto;
    }

    public String getDescProducto() {
        return descProducto;
    }

    public void setDescProducto(String descProducto) {
        this.descProducto = descProducto;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Boolean getEstadoRegalo() {
        return estadoRegalo;
    }

    public void setEstadoRegalo(Boolean estadoRegalo) {
        this.estadoRegalo = estadoRegalo;
    }
}
