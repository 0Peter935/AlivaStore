package com.store.erp.Models;

import java.math.BigDecimal;

public class PedidoDTO {
    private Long idPedido;

    private VendedorDTO vendedor;          
    private ClienteDTO cliente;            
    private EstadoPedidoDTO estadoPedido;  
    private EmpresaEntregaDTO empresaEntrega; 
    private ProductoRegaloDTO regalo;      

    private String documento;              
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

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public VendedorDTO getVendedor() {
        return vendedor;
    }

    public void setVendedor(VendedorDTO vendedor) {
        this.vendedor = vendedor;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }

    public EstadoPedidoDTO getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(EstadoPedidoDTO estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public EmpresaEntregaDTO getEmpresaEntrega() {
        return empresaEntrega;
    }

    public void setEmpresaEntrega(EmpresaEntregaDTO empresaEntrega) {
        this.empresaEntrega = empresaEntrega;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public ProductoRegaloDTO getRegalo() {
        return regalo;
    }

    public void setRegalo(ProductoRegaloDTO regalo) {
        this.regalo = regalo;
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
