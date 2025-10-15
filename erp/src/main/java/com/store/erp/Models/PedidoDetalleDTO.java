package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDetalleDTO {
    
    private int idDetallePedido;
    private int idPedido;
    private ProductoDTO producto;
    private int cantidad;
    private double precioUnitario;
    private double precioTotal;

}
