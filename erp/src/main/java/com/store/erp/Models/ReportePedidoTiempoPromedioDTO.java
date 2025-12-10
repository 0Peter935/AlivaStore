package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReportePedidoTiempoPromedioDTO {
  
    private double Promedio_P_A;
    private double Promedio_P_E;
    private double Promedio_Pedido;

}
