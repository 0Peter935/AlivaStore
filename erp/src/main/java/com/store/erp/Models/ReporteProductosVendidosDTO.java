package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReporteProductosVendidosDTO {

    private String NombreProductoCorto;
    private String NombreProducto;
    private int TotalVendido;
    
}
