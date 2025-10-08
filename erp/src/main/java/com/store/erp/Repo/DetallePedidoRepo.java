package com.store.erp.Repo;

import com.store.erp.Models.DetallePedidoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DetallePedidoRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private DetallePedidoDTO mapRowToDetallePedido(ResultSet rs, int rowNum) throws SQLException {
        DetallePedidoDTO detallePedido = new DetallePedidoDTO();
        detallePedido.setIdDetalleP(rs.getLong("id_detalle_p"));
        detallePedido.setIdPedido(rs.getLong("id_pedido"));
        detallePedido.setIdProducto(rs.getLong("id_producto"));
        detallePedido.setCantidad(rs.getInt("cantidad"));
        detallePedido.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        detallePedido.setPrecioTotal(rs.getBigDecimal("precio_total"));
        detallePedido.setIdStock(rs.getBigDecimal("id_stock"));

        return detallePedido;
    }

    public List<DetallePedidoDTO> listar() {
        return jdbcTemplate.query("EXEC ListarDetallePedidos", this::mapRowToDetallePedido);
    }

    public List<DetallePedidoDTO> buscarPorId(Long id) {
        return jdbcTemplate.query("EXEC BuscarDetallePedido ?", this::mapRowToDetallePedido, id);
    }

    public void guardar(DetallePedidoDTO detallePedido) {
        jdbcTemplate.update("EXEC GuardarDetallePedido ?, ?, ?, ?, ?, ?, ?",
                detallePedido.getIdDetalleP(),
                detallePedido.getIdPedido(),
                detallePedido.getIdProducto(),
                detallePedido.getCantidad(),
                detallePedido.getPrecioUnitario(),
                detallePedido.getPrecioTotal(),
                detallePedido.getIdStock());
    }

    public void actualizar(DetallePedidoDTO detallePedido) {
        jdbcTemplate.update("EXEC ActualizarDetallePedido ?, ?, ?, ?, ?, ?, ?",
                detallePedido.getIdDetalleP(),
                detallePedido.getIdPedido(),
                detallePedido.getIdProducto(),
                detallePedido.getCantidad(),
                detallePedido.getPrecioUnitario(),
                detallePedido.getPrecioTotal(),
                detallePedido.getIdStock());
    }
}
