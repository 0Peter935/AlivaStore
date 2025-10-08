package com.store.erp.Repo;

import com.store.erp.Models.EstadoPedidoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EstadoPedidoRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private EstadoPedidoDTO mapRowToEstadoPedido(ResultSet rs, int rowNum) throws SQLException {
        EstadoPedidoDTO estadoPedido = new EstadoPedidoDTO();
        estadoPedido.setIdEstado(rs.getInt("id_pedido"));
        estadoPedido.setDescripcion(rs.getString("descripcion"));

        return estadoPedido;
    }

    public List<EstadoPedidoDTO> listar() {
        return jdbcTemplate.query("EXEC ListarEstadoPedido", this::mapRowToEstadoPedido);
    }

    public EstadoPedidoDTO buscarPorId(Integer id) {
        return jdbcTemplate.queryForObject("EXEC BuscarEstadoPedido ?", this::mapRowToEstadoPedido, id);
    }

    public void guardar(EstadoPedidoDTO estadoPedido) {
        jdbcTemplate.update("EXEC GuardarEstadoPedido ?, ?",
                estadoPedido.getIdEstado(),
                estadoPedido.getDescripcion());
    }

    public void actualizar(EstadoPedidoDTO estadoPedido) {
        jdbcTemplate.update("EXEC ActualizarEstadoPedido ?, ?",
                estadoPedido.getIdEstado(),
                estadoPedido.getDescripcion());
    }
}
