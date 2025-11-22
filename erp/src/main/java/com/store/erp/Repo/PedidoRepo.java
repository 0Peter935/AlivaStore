package com.store.erp.Repo;

import java.math.BigDecimal;
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
import com.store.erp.Models.PedidoNotaDTO;

@Repository
public class PedidoRepo extends Mappers {

    @Autowired private JdbcTemplate jdbcTemplate;

    public List<PedidoDTO> listarPedidos() {
        return jdbcTemplate.query("EXEC SP_PEDIDO_LISTAR_COMPLETO", (ResultSet rs) -> mapPedidosConDetalles(rs));
    }

    public PedidoDTO obtenerPedidoPorCod(String codPedido) {
        return jdbcTemplate.query(
            "EXEC SP_PEDIDO_OBTENER_CON_DETALLE ?",
            new Object[]{codPedido},
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
                        detalle.setCodPedido(pedido.getCodPedido());
                        detallesMap.put(idDetalle, detalle);
                    }
                }

                if (pedido != null)
                    pedido.setDetalles(new ArrayList<>(detallesMap.values()));

                return pedido;
            }
        );
    }

    public int guardarPedidoCompleto(PedidoDTO pedido) {
        return jdbcTemplate.execute((Connection con) -> {
            try {
                // ===============================
                // TVP DETALLES
                // ===============================
                SQLServerDataTable tvpDetalles = new SQLServerDataTable();

                tvpDetalles.addColumnMetadata("COD_PRODUCTO", java.sql.Types.NVARCHAR);
                tvpDetalles.addColumnMetadata("COD_VARIANTE", java.sql.Types.NVARCHAR);
                tvpDetalles.addColumnMetadata("NOMBRE_PRODUCTO", java.sql.Types.NVARCHAR);
                tvpDetalles.addColumnMetadata("CANTIDAD", java.sql.Types.INTEGER);
                tvpDetalles.addColumnMetadata("PRECIO_UNITARIO", java.sql.Types.DECIMAL);
                tvpDetalles.addColumnMetadata("PRECIO_TOTAL", java.sql.Types.DECIMAL);

                for (PedidoDetalleDTO det : pedido.getDetalles()) {
                    tvpDetalles.addRow(
                        det.getCodProducto(),
                        det.getCodVariante(),
                        det.getNombreProducto(),
                        det.getCantidad(),
                        det.getPrecioUnitario(),
                        det.getPrecioTotal()
                    );
                }

                // ===============================
                // TVP NOTAS
                // ===============================
                SQLServerDataTable tvpNotas = new SQLServerDataTable();

                tvpNotas.addColumnMetadata("TITULO", java.sql.Types.NVARCHAR);
                tvpNotas.addColumnMetadata("DESCRIPCION", java.sql.Types.NVARCHAR);

                for (PedidoNotaDTO nota : pedido.getNotas()) {
                    tvpNotas.addRow(
                        nota.getTitulo(),
                        nota.getDescripcion()
                    );
                }

                // ===============================
                // PROCEDURE CALL
                // ===============================
                String sql = "EXEC SP_PEDIDO_GUARDAR_COMPLETO ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, pedido.getCodPedido());
                    ps.setInt(2, pedido.getUsuario().getIdUsuario());
                    ps.setString(3, pedido.getCliente().getCodigoCliente());
                    ps.setInt(4, pedido.getEstadoPedido().getIdEstadoPedido());
                    ps.setInt(5, pedido.getEmpresaEntrega().getIdEmpresaEntrega());
                    ps.setString(6, pedido.getDocumento());
                    ps.setString(7, pedido.getEvidencia());
                    ps.setBigDecimal(8, BigDecimal.valueOf(pedido.getSubtotal()));
                    ps.setBigDecimal(9, BigDecimal.valueOf(pedido.getIgv()));
                    ps.setBigDecimal(10, BigDecimal.valueOf(pedido.getAdelanto()));
                    ps.setBigDecimal(11, BigDecimal.valueOf(pedido.getMontoTotal()));
                    ps.setString(12, pedido.getCiudad());
                    ps.setString(13, pedido.getTipoPago());
                    ps.setString(14, pedido.getTipoComprobante());
                    ps.setBigDecimal(15, BigDecimal.valueOf(pedido.getMontoCobrado()));
                    ps.setString(16, pedido.getObservacion());
                    ps.setObject(17, pedido.getFechaReg());
                    ps.setObject(18, tvpDetalles);   // TVP DETALLES
                    ps.setObject(19, tvpNotas);      // TVP NOTAS  ⭐ NUEVO

                    return ps.executeUpdate();
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error al guardar pedido completo", e);
            }
        });
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
                String sql = "EXEC SP_PEDIDO_ACTUALIZAR_COMPLETO ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";

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
