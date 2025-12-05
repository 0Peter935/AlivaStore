package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEvidenciaDTO {

    private Long idEvidenciaPedido;
    private String codPedido;
    private String motivo;
    private String url;
    
}
