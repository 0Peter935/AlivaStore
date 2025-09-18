package com.store.erp.Repo;

import com.store.erp.Models.PedidoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PedidoRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PedidoDTO mapRowToPedido(ResultSet rs, int rowNum) throws SQLException {
        PedidoDTO pedido = new PedidoDTO();
        pedido.setIdPedido(rs.getLong("id_pedido"));
        pedido.setIdVendedor(rs.getInt("id_vendedor"));
        pedido.setIdCliente(rs.getInt("id_cliente"));
        pedido.setIdEstadoPedido(rs.getInt("id_estado_pedido"));
        pedido.setIdEmpresaEntrega(rs.getInt("id_empresa_entrega"));
        pedido.setDocumento(rs.getString("documento"));
        pedido.setIdRegalo(rs.getInt("id_regalo"));
        pedido.setSubtotal(rs.getBigDecimal("subtotal"));
        pedido.setIgv(rs.getBigDecimal("igv"));
        pedido.setAdelanto(rs.getInt("adelanto"));
        pedido.setMontoTotal(rs.getBigDecimal("monto_total"));
        pedido.setCiudad(rs.getString("ciudad"));
        pedido.setTipoPago(rs.getString("tipo_pago"));
        pedido.setTipoComprobante(rs.getString("tipo_comprobante"));
        pedido.setMontoCobrado(rs.getBigDecimal("monto_cobrado"));
        pedido.setObservacion(rs.getString("observacion"));

        return pedido;
    }

    public List<PedidoDTO> listar() {
        return jdbcTemplate.query("EXEC ListarPedidos", this::mapRowToPedido);
    }

    public PedidoDTO buscarPorId(Long id) {
        return jdbcTemplate.queryForObject("EXEC BuscarPedido ?", this::mapRowToPedido, id);
    }

    public void guardar(PedidoDTO pedido) {
        jdbcTemplate.update("EXEC GuardarPedido ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?",
                pedido.getIdPedido(),
                pedido.getIdVendedor(),
                pedido.getIdCliente(),
                pedido.getIdEstadoPedido(),
                pedido.getIdEmpresaEntrega(),
                pedido.getDocumento(),
                pedido.getIdRegalo(),
                pedido.getSubtotal(),
                pedido.getIgv(),
                pedido.getAdelanto(),
                pedido.getMontoTotal(),
                pedido.getCiudad(),
                pedido.getTipoPago(),
                pedido.getTipoComprobante(),
                pedido.getMontoCobrado(),
                pedido.getObservacion());
    }

    public void actualizar(PedidoDTO pedido) {
        jdbcTemplate.update("EXEC ActualizarPedido ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?",
                pedido.getIdPedido(),
                pedido.getIdVendedor(),
                pedido.getIdCliente(),
                pedido.getIdEstadoPedido(),
                pedido.getIdEmpresaEntrega(),
                pedido.getDocumento(),
                pedido.getIdRegalo(),
                pedido.getSubtotal(),
                pedido.getIgv(),
                pedido.getAdelanto(),
                pedido.getMontoTotal(),
                pedido.getCiudad(),
                pedido.getTipoPago(),
                pedido.getTipoComprobante(),
                pedido.getMontoCobrado(),
                pedido.getObservacion());
    }
}
