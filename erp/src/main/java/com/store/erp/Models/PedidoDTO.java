package com.store.erp.Models;

import java.math.BigDecimal;

public class PedidoDTO {
    private Long idPedido;
    private Integer idVendedor;
    private Integer idCliente;
    private Integer idEstadoPedido;
    private Integer idEmpresaEntrega;
    private String documento;
    private Integer idRegalo;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private Integer adelanto;
    private BigDecimal montoTotal;
    private String ciudad;
    private String tipoPago;
    private String tipoComprobante;
    private BigDecimal montoCobrado;
    private String observacion;

    public PedidoDTO() {
    }

    public PedidoDTO(Long idPedido, Integer idVendedor, Integer idCliente, Integer idEstadoPedido,
            Integer idEmpresaEntrega,
            String documento, Integer idRegalo, BigDecimal subtotal, BigDecimal igv, Integer adelanto,
            BigDecimal montoTotal, String ciudad, String tipoPago, String tipoComprobante,
            BigDecimal montoCobrado, String observacion) {
        this.idPedido = idPedido;
        this.idVendedor = idVendedor;
        this.idCliente = idCliente;
        this.idEstadoPedido = idEstadoPedido;
        this.idEmpresaEntrega = idEmpresaEntrega;
        this.documento = documento;
        this.idRegalo = idRegalo;
        this.subtotal = subtotal;
        this.igv = igv;
        this.adelanto = adelanto;
        this.montoTotal = montoTotal;
        this.ciudad = ciudad;
        this.tipoPago = tipoPago;
        this.tipoComprobante = tipoComprobante;
        this.montoCobrado = montoCobrado;
        this.observacion = observacion;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Integer idVendedor) {
        this.idVendedor = idVendedor;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdEstadoPedido() {
        return idEstadoPedido;
    }

    public void setIdEstadoPedido(Integer idEstadoPedido) {
        this.idEstadoPedido = idEstadoPedido;
    }

    public Integer getIdEmpresaEntrega() {
        return idEmpresaEntrega;
    }

    public void setIdEmpresaEntrega(Integer idEmpresaEntrega) {
        this.idEmpresaEntrega = idEmpresaEntrega;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public Integer getIdRegalo() {
        return idRegalo;
    }

    public void setIdRegalo(Integer idRegalo) {
        this.idRegalo = idRegalo;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public void setIgv(BigDecimal igv) {
        this.igv = igv;
    }

    public Integer getAdelanto() {
        return adelanto;
    }

    public void setAdelanto(Integer adelanto) {
        this.adelanto = adelanto;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public BigDecimal getMontoCobrado() {
        return montoCobrado;
    }

    public void setMontoCobrado(BigDecimal montoCobrado) {
        this.montoCobrado = montoCobrado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
