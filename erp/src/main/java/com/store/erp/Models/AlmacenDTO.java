package com.store.erp.Models;

public class AlmacenDTO {
    private Integer idAlmacen;
    private Long idProducto;
    private Integer stockReal;
    private Integer stockSeparado;

    public AlmacenDTO() {
    }

    public AlmacenDTO(Integer idAlmacen, Long idProducto, Integer stockReal, Integer stockSeparado) {
        this.idAlmacen = idAlmacen;
        this.idProducto = idProducto;
        this.stockReal = stockReal;
        this.stockSeparado = stockSeparado;
    }

    public Integer getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(Integer idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getStockReal() {
        return stockReal;
    }

    public void setStockReal(Integer stockReal) {
        this.stockReal = stockReal;
    }

    public Integer getStockSeparado() {
        return stockSeparado;
    }

    public void setStockSeparado(Integer stockSeparado) {
        this.stockSeparado = stockSeparado;
    }
}
