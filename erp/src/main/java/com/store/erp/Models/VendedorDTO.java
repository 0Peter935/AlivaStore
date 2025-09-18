package com.store.erp.Models;

public class VendedorDTO {
    private Integer idVendedor;
    private String codVendedor;
    private String nombreVendedor;
    private Boolean estado;

    public VendedorDTO() {
    }

    public VendedorDTO(Integer idVendedor, String codVendedor, String nombreVendedor, Boolean estado) {
        this.idVendedor = idVendedor;
        this.codVendedor = codVendedor;
        this.nombreVendedor = nombreVendedor;
        this.estado = estado;
    }

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Integer idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getCodVendedor() {
        return codVendedor;
    }

    public void setCodVendedor(String codVendedor) {
        this.codVendedor = codVendedor;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public void setNombreVendedor(String nombreVendedor) {
        this.nombreVendedor = nombreVendedor;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}