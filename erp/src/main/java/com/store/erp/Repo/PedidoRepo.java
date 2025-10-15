package com.store.erp.Repo;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.store.erp.Models.PedidoDTO;
import com.store.erp.Models.PedidoDetalleDTO;

@Repository
public class PedidoRepo extends Mappers {

    @Autowired private JdbcTemplate jdbcTemplate;

    public List<PedidoDTO> listarPedidos() {
        String sql = "EXEC SP_PEDIDO_LISTAR_COMPLETO";
        return jdbcTemplate.query(sql, (ResultSet rs) -> mapPedidosConDetalles(rs));
    }

    public PedidoDTO obtenerPedidoPorId(int idPedido) {
        return jdbcTemplate.query(
            "EXEC SP_PEDIDO_OBTENER_DETALLE ?",
            new Object[]{idPedido},
            (ResultSet rs) -> {
                PedidoDTO pedido = null;
                Map<Integer, PedidoDetalleDTO> detallesMap = new LinkedHashMap<>();

                while (rs.next()) {
                    if (pedido == null) {
                        pedido = Mappers.mapPedidoConDetalle(rs);
                    }

                    int idDetalle = RepoUtils.safeInt(rs, "ID_DETALLE_P");
                    if (!detallesMap.containsKey(idDetalle)) {
                        PedidoDetalleDTO detalle = Mappers.mapDetallePedido(rs);
                        detalle.setIdPedido(pedido.getIdPedido());
                        detallesMap.put(idDetalle, detalle);
                    }
                }

                if (pedido != null)
                    pedido.setDetalles(new ArrayList<>(detallesMap.values()));

                return pedido;
            }
        );
    }

    public int registrarPedido(PedidoDTO pedido) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_PEDIDO_INSERTAR ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?",
            Integer.class,
            pedido.getUsuario(),
            pedido.getCliente(),
            pedido.getEstadoPedido(),
            pedido.getEmpresaEntrega(),
            pedido.getDocumento(),
            pedido.getRegalo(),
            pedido.getSubtotal(),
            pedido.getIgv(),
            pedido.getAdelanto(),
            pedido.getMontoTotal(),
            pedido.getCiudad(),
            pedido.getTipoPago(),
            pedido.getTipoComprobante(),
            pedido.getMontoCobrado(),
            pedido.getObservacion()
        );
    }

    public void registrarDetalle(PedidoDetalleDTO detalle) {
        jdbcTemplate.update(
            "EXEC SP_DETALLE_PEDIDO_INSERTAR ?, ?, ?, ?, ?",
            detalle.getIdPedido(),
            detalle.getProducto(),
            detalle.getCantidad(),
            detalle.getPrecioUnitario(),
            detalle.getPrecioTotal()
        );
    }

}
