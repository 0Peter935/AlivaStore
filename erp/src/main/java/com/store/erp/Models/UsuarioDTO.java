package com.store.erp.Models;

public class UsuarioDTO {
    private Integer idUsuario;
    private String usuario;
    private String descripcion;
    private String clave;
    private Short idRol;
    private Boolean estado;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Integer idUsuario, String usuario, String descripcion, String clave, Short idRol,
            Boolean estado) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.descripcion = descripcion;
        this.clave = clave;
        this.idRol = idRol;
        this.estado = estado;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Short getIdRol() {
        return idRol;
    }

    public void setIdRol(Short idRol) {
        this.idRol = idRol;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}
