package com.store.erp.Models;

import java.math.BigDecimal;

public class DetallePedidoDTO {
    private Long idDetalleP;
    private Long idPedido;
    private Long idProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal precioTotal;
    private BigDecimal idStock;

    public DetallePedidoDTO() {
    }

    public DetallePedidoDTO(Long idDetalleP, Long idPedido, Long idProducto, Integer cantidad,
            BigDecimal precioUnitario, BigDecimal precioTotal, BigDecimal idStock) {
        this.idDetalleP = idDetalleP;
        this.idPedido = idPedido;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.precioTotal = precioTotal;
        this.idStock = idStock;
    }

    public Long getIdDetalleP() {
        return idDetalleP;
    }

    public void setIdDetalleP(Long idDetalleP) {
        this.idDetalleP = idDetalleP;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }

    public BigDecimal getIdStock() {
        return idStock;
    }

    public void setIdStock(BigDecimal idStock) {
        this.idStock = idStock;
    }
}
