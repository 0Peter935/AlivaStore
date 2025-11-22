package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoNotaDTO {
  
  private int idNotaPedido;
  private String codPedido;
  private String titulo;
  private String descripcion;

}
