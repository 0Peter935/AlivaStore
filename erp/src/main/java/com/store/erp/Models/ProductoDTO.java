package com.store.erp.Models;

import java.time.LocalDateTime;

public class ProductoDTO {
    private Long idProducto;
    private String codProducto;
    private String descProducto;
    private String imagen;
    private LocalDateTime fechaRegistro;
    private Boolean estado;

    public ProductoDTO() {
    }

    public ProductoDTO(Long idProducto, String codProducto, String descProducto, String imagen,
            LocalDateTime fechaRegistro, Boolean estado) {
        this.idProducto = idProducto;
        this.codProducto = codProducto;
        this.descProducto = descProducto;
        this.imagen = imagen;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
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

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}
