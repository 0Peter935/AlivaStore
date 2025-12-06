package com.store.erp.Models;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PedidoPredictivoDTO {
    private LocalDate fecha;
    private int totalPedidos;
    private int diaSemana;
    private int mes;
    private int diaAnio;
}