package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDetalleDTO {
    
    private int idDetallePedido;
    private String codPedido;
    private String codProducto;
    private String codVariante;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;
    private double precioTotal;

}
