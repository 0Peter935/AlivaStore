package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReporteIndicadoresCardsDTO {

    private double ventasTotales;
    private int numeroPedidos;
    private double promedioPedidosPorDia;
    private int cantidadClientes;

}
