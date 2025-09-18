package com.store.erp.Models;

public class EmpresaEntregaDTO {
    private Integer idEmpresaEntrega;
    private String razonSocial;
    private String ruc;
    private String direccionFiscal;

    public EmpresaEntregaDTO() {
    }

    public EmpresaEntregaDTO(Integer idEmpresaEntrega, String razonSocial, String ruc, String direccionFiscal) {
        this.idEmpresaEntrega = idEmpresaEntrega;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.direccionFiscal = direccionFiscal;
    }

    public Integer getIdEmpresaEntrega() {
        return idEmpresaEntrega;
    }

    public void setIdEmpresaEntrega(Integer idEmpresaEntrega) {
        this.idEmpresaEntrega = idEmpresaEntrega;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDireccionFiscal() {
        return direccionFiscal;
    }

    public void setDireccionFiscal(String direccionFiscal) {
        this.direccionFiscal = direccionFiscal;
    }
}
