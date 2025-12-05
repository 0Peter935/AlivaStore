package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

    private int idPedido;
    
    private String codPedido;
    private UsuarioDTO usuario;
    private ClienteDTO cliente;
    private EstadoPedidoDTO estadoPedido;
    private EmpresaEntregaDTO empresaEntrega;

    private String documento;
    private double subtotal;
    private double igv;
    private double montoTotal;
    private String ciudad;
    private String tipoPago;
    private String tipoComprobante;
    private Boolean adelanto;
    private double montoAdelanto;
    private String observacion;
    private OffsetDateTime fechaReg;
    private LocalDateTime fechaAprobado;

    private List<PedidoNotaDTO> notas;
    private List<PedidoDetalleDTO> detalles;
    private List<PedidoEvidenciaDTO> evidencias;
    private List<PedidoLogDTO> logs;

    // extras
    private List<Integer> evidenciasEliminar;
    private PedidoLogDTO logNuevo;

}
