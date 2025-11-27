package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReportePedidosVendedorDTO {

    private String Usuario;
    private int totalPedidos;
    
}
