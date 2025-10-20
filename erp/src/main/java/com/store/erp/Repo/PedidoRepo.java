package com.store.erp.Repo;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
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

    public void actualizarPedidoCompleto(PedidoDTO pedido) {
        jdbcTemplate.execute((Connection con) -> {
            try {
                // 🧱 Crear TVP (Table-Valued Parameter) para los detalles
                SQLServerDataTable tvp = new SQLServerDataTable();
                tvp.addColumnMetadata("ID_PRODUCTO", java.sql.Types.INTEGER);
                tvp.addColumnMetadata("CANTIDAD", java.sql.Types.INTEGER);
                tvp.addColumnMetadata("PRECIO_UNITARIO", java.sql.Types.DECIMAL);
                tvp.addColumnMetadata("PRECIO_TOTAL", java.sql.Types.DECIMAL);

                if (pedido.getDetalles() != null) {
                    pedido.getDetalles().forEach(det -> {
                        try {
                            tvp.addRow(
                                det.getProducto() != null ? det.getProducto().getIdProducto() : 0,
                                det.getCantidad(),
                                det.getPrecioUnitario(),
                                det.getPrecioTotal()
                            );
                        } catch (SQLException e) {
                            throw new RuntimeException("Error al agregar fila al TVP", e);
                        }
                    });
                }

                // ⚙️ Consulta con parámetros posicionales
                String sql = "EXEC SP_PEDIDO_GUARDAR_COMPLETO ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, pedido.getIdPedido());
                    ps.setBigDecimal(2, BigDecimal.valueOf(pedido.getSubtotal()));
                    ps.setBigDecimal(3, BigDecimal.valueOf(pedido.getIgv()));
                    ps.setBigDecimal(4, BigDecimal.valueOf(pedido.getMontoTotal()));
                    ps.setBigDecimal(5, BigDecimal.valueOf(pedido.getAdelanto()));
                    ps.setString(6, pedido.getTipoPago());
                    ps.setString(7, pedido.getEvidencia());
                    ps.setLong(8, pedido.getEmpresaEntrega().getIdEmpresaEntrega());
                    ps.setString(9, pedido.getTipoComprobante());
                    ps.setObject(10, tvp);

                    ps.execute();
                }

                return null;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error al guardar el pedido completo", e);
            }
        });
    }

}
