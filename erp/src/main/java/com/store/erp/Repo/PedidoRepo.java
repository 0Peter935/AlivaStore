package com.store.erp.Repo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.store.erp.Models.PedidoDTO;
import com.store.erp.Models.PedidoDetalleDTO;
import com.store.erp.Models.PedidoEvidenciaDTO;
import com.store.erp.Models.PedidoLogDTO;
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

    public List<PedidoDTO> listarPedidosPorEstado(int idEstado, Integer idUsuario) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement("EXEC SP_PEDIDO_LISTAR_POR_ESTADO ?, NULL");
            ps.setInt(1, idEstado);
            return ps;
        }, (ResultSet rs) -> mapPedidosConDetalles(rs));
    }

    public List<PedidoDTO> listarPedidosPorEstadoyUsuario(int idEstado, Integer idUsuario) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement("EXEC SP_PEDIDO_LISTAR_POR_ESTADO ?, ?");
            ps.setInt(1, idEstado);
            ps.setInt(2, idUsuario);
            return ps;
        }, (ResultSet rs) -> mapPedidosConDetalles(rs));
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
                        det.getVariante().getCodVariante(),
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
                    ps.setString(3, pedido.getCliente().getCodCliente());
                    ps.setInt(4, pedido.getEstadoPedido().getIdEstadoPedido());
                    ps.setInt(5, pedido.getEmpresaEntrega().getIdEmpresaEntrega());
                    ps.setString(6, pedido.getDocumento());
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

    public void actualizarPedidoConDetalle(PedidoDTO pedido) {

        jdbcTemplate.execute((Connection con) -> {
            try {

                System.out.println("📌 Construyendo TVP DETALLE_PEDIDO...");

                // ===========================
                // TVP Detalles
                // ===========================
                SQLServerDataTable tvp = new SQLServerDataTable();

                tvp.addColumnMetadata("COD_PRODUCTO", java.sql.Types.NVARCHAR);
                tvp.addColumnMetadata("COD_VARIANTE", java.sql.Types.NVARCHAR);
                tvp.addColumnMetadata("NOMBRE_PRODUCTO", java.sql.Types.NVARCHAR);
                tvp.addColumnMetadata("CANTIDAD", java.sql.Types.INTEGER);
                tvp.addColumnMetadata("PRECIO_UNITARIO", java.sql.Types.DECIMAL);
                tvp.addColumnMetadata("PRECIO_TOTAL", java.sql.Types.DECIMAL);
                tvp.addColumnMetadata("DP_REGALO", java.sql.Types.BIT);

                if (pedido.getDetalles() != null) {
                    for (PedidoDetalleDTO det : pedido.getDetalles()) {

                        tvp.addRow(
                            det.getCodProducto(),
                            det.getVariante().getCodVariante(),
                            det.getNombreProducto(),
                            det.getCantidad(),
                            det.getPrecioUnitario(),
                            det.getPrecioTotal(),
                            det.getEsRegalo()
                        );
                    }
                }

                // ===========================
                // Ejecutar SP
                // ===========================
                String sql = "EXEC SP_PEDIDO_ACTUALIZAR_COMPLETO ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, pedido.getCodPedido());
                    ps.setString(2, pedido.getTipoComprobante());

                    ps.setBigDecimal(3, BigDecimal.valueOf(pedido.getSubtotal()));
                    ps.setBigDecimal(4, BigDecimal.valueOf(pedido.getIgv()));
                    ps.setBigDecimal(5, BigDecimal.valueOf(pedido.getMontoTotal()));

                    ps.setBoolean(6, pedido.getAdelanto());
                    ps.setBigDecimal(7, BigDecimal.valueOf(pedido.getMontoAdelanto()));

                    ps.setString(8, pedido.getTipoPago());
                    ps.setString(9,pedido.getObservacion());

                    ps.setLong(10, pedido.getEmpresaEntrega().getIdEmpresaEntrega());
                    ps.setInt(11, pedido.getEstadoPedido().getIdEstadoPedido());

                    ps.setObject(12, tvp);

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

    public List<PedidoEvidenciaDTO> listarEvidenciasPedido(String codPedido){
        return jdbcTemplate.query(
            "EXEC SP_PEDIDO_LISTAR_EVIDENCIA ?",
            (rs, _) -> Mappers.mapEvidenciaPedido(rs),
            codPedido
        );
    }

    public void agregarEvidenciaPedido(PedidoEvidenciaDTO evidencia) {
        jdbcTemplate.update("EXEC dbo.SP_PEDIDO_AGREGAR_EVIDENCIA ?, ?, ?", 
            evidencia.getCodPedido(), 
            evidencia.getMotivo(), 
            evidencia.getUrl());
    }

    public void eliminarEvidenciaPedidoPorId(Integer idEvidencia) {
        String sql = "EXEC SP_PEDIDO_EVIDENCIA_ELIMINAR ?";
        jdbcTemplate.update(sql, idEvidencia);
    }

    public List<PedidoLogDTO> listarlogsPedido(String codPedido){
        return jdbcTemplate.query(
            "EXEC SP_PEDIDO_LOG_LISTAR ?",
            (rs, _) -> Mappers.mapPedidoLog(rs),
            codPedido
        );
    }

    public void insertarLog(PedidoLogDTO log) {
        jdbcTemplate.update("EXEC SP_PEDIDO_LOG_INSERTAR ?, ?, ?, ?",
            log.getIdUsuario(),
            log.getCodPedido(),
            log.getIdEstadoP(),
            log.getMotivoLog());
    }

    public void actualizarEstado(PedidoDTO pedido) {
        jdbcTemplate.update("EXEC SP_PEDIDO_ACTUALIZAR_REGRESO ?, ?",
            pedido.getCodPedido(),
            pedido.getEstadoPedido().getIdEstadoPedido()
        );
    }

    public Map<Integer, Integer> obtenerCargaPedidosPorVendedor() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT ID_USUARIO, COUNT(*) AS total FROM PEDIDO GROUP BY ID_USUARIO"
        );

        Map<Integer, Integer> carga = new HashMap<>();
        for (Map<String, Object> row : rows) {
            carga.put((Integer) row.get("idUsuario"), ((Long) row.get("total")).intValue());
        }
        return carga;
    }


}
