package com.store.erp.Models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoLogDTO {
    
    private Long idLog;
    private Long idUsuario;
    private String codPedido;
    private int idEstadoP;
    private String motivoLog;
    private LocalDateTime fechaLog;

}
