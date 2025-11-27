package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReportePedidosFechaDTO {

    private String fecha;
    private int totalPedidos;
    
}
