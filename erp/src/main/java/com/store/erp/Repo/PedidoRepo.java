package com.store.erp.Repo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
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

    public List<PedidoDTO> listarPedidosPorVendedor(int idVendedor) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement("EXEC SP_PEDIDO_LISTAR_POR_VENDEDOR ?");
            ps.setInt(1, idVendedor);
            return ps;
        }, (ResultSet rs) -> mapPedidosConDetalles(rs));
    }

    public List<PedidoDTO> listarPedidosParaLogistica() {
        return jdbcTemplate.query("EXEC SP_PEDIDO_LISTAR_PARA_LOGISTICA", (ResultSet rs) -> mapPedidosConDetalles(rs));
    }

    public PedidoDTO obtenerPedidoPorCod(String codPedido) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement("EXEC SP_PEDIDO_OBTENER_DETALLE ?");
            ps.setString(1, codPedido);
            return ps;
        }, rs -> {

            PedidoDTO pedido = null;

            // 1️⃣ Primer resultset → PEDIDO
            if (rs.next()) {
                pedido = Mappers.mapPedido(rs);
            }

            // ⏭ Ir al siguiente resultset → DETALLES
            if (rs.getStatement().getMoreResults()) {
                ResultSet rsDetalles = rs.getStatement().getResultSet();
                while (rsDetalles.next()) {
                    pedido.getDetalles().add(Mappers.mapDetallePedido(rsDetalles));
                }
            }

            // ⏭ Siguiente resultset → NOTAS
            if (rs.getStatement().getMoreResults()) {
                ResultSet rsNotas = rs.getStatement().getResultSet();
                while (rsNotas.next()) {
                    pedido.getNotas().add(Mappers.mapNotaPedido(rsNotas));
                }
            }

            return pedido;
        });
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
                String sql = "EXEC SP_PEDIDO_SINCRO ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";

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
                    ps.setBoolean(10, pedido.getAdelanto());
                    ps.setBigDecimal(11, BigDecimal.valueOf(pedido.getMontoTotal()));
                    ps.setString(12, pedido.getCiudad());
                    ps.setString(13, pedido.getTipoPago());
                    ps.setString(14, pedido.getTipoComprobante());
                    ps.setBigDecimal(15, BigDecimal.valueOf(pedido.getMontoAdelanto()));
                    ps.setString(16, pedido.getObservacion());
                    ps.setObject(17, pedido.getFechaReg());
                    ps.setObject(18, tvpDetalles);
                    ps.setObject(19, tvpNotas);

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

                System.out.println("📌 Construyendo TVP DETALLE_PEDIDO...");

                // ===========================
                // 1️⃣ Crear TVP Detalles
                // ===========================
                SQLServerDataTable tvp = new SQLServerDataTable();

                tvp.addColumnMetadata("COD_PRODUCTO", java.sql.Types.NVARCHAR);
                tvp.addColumnMetadata("COD_VARIANTE", java.sql.Types.NVARCHAR);
                tvp.addColumnMetadata("NOMBRE_PRODUCTO", java.sql.Types.NVARCHAR);
                tvp.addColumnMetadata("CANTIDAD", java.sql.Types.INTEGER);
                tvp.addColumnMetadata("PRECIO_UNITARIO", java.sql.Types.DECIMAL);
                tvp.addColumnMetadata("PRECIO_TOTAL", java.sql.Types.DECIMAL);

                if (pedido.getDetalles() != null) {
                    for (PedidoDetalleDTO det : pedido.getDetalles()) {

                        tvp.addRow(
                            det.getCodProducto(),
                            det.getCodVariante(),
                            det.getNombreProducto(),
                            det.getCantidad(),
                            det.getPrecioUnitario(),
                            det.getPrecioTotal()
                        );
                    }
                }

                // ===========================
                // 2️⃣ Ejecutar SP
                // ===========================
                String sql = "EXEC SP_PEDIDO_ACTUALIZAR_COMPLETO ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, pedido.getCodPedido());
                    ps.setString(2, pedido.getDocumento());
                    ps.setString(3, pedido.getTipoComprobante());

                    ps.setBigDecimal(4, BigDecimal.valueOf(pedido.getSubtotal()));
                    ps.setBigDecimal(5, BigDecimal.valueOf(pedido.getIgv()));
                    ps.setBigDecimal(6, BigDecimal.valueOf(pedido.getMontoTotal()));

                    ps.setBoolean(7, pedido.getAdelanto());
                    ps.setBigDecimal(8, BigDecimal.valueOf(pedido.getMontoAdelanto()));

                    ps.setString(9, pedido.getTipoPago());
                    ps.setString(10, pedido.getEvidencia());

                    ps.setLong(11, pedido.getEmpresaEntrega().getIdEmpresaEntrega());
                    ps.setInt(12, pedido.getEstadoPedido().getIdEstadoPedido());

                    ps.setObject(13, tvp);

                    ps.execute();
                }

                System.out.println("Pedido actualizado correctamente");
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error al guardar el pedido completo", e);
            }
        });
    }

}
