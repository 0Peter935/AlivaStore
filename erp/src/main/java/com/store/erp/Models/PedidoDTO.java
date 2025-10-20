package com.store.erp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

    private int idPedido;
    private UsuarioDTO usuario;
    private ClienteDTO cliente;
    private EstadoPedidoDTO estadoPedido;
    private EmpresaEntregaDTO empresaEntrega;
    private String documento;
    private String evidencia;
    private double subtotal;
    private double igv;
    private double adelanto;
    private double montoTotal;
    private String ciudad;
    private String tipoPago;
    private String tipoComprobante;
    private double montoCobrado;
    private String observacion;
    private LocalDateTime fechaRegistro;

    private List<PedidoDetalleDTO> detalles;

}
