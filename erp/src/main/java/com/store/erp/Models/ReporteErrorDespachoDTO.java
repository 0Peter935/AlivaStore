package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReporteErrorDespachoDTO {

    private int Cantidad_Pedidos_Estado;
    private double Porcentaje_Error_Despacho;
}

