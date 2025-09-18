package com.store.erp.Repo;

import com.store.erp.Models.ClienteDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ClienteRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ClienteDTO mapRowToCliente(ResultSet rs, int rowNum) throws SQLException {
        ClienteDTO cliente = new ClienteDTO();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombreCompleto(rs.getString("nombre_completo"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        cliente.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion") != null
                ? rs.getTimestamp("fecha_actualizacion").toLocalDateTime()
                : null);

        return cliente;
    }

    public List<ClienteDTO> listar() {
        return jdbcTemplate.query("EXEC ListarClientes", this::mapRowToCliente);
    }

    public ClienteDTO buscarPorId(Integer id) {
        return jdbcTemplate.queryForObject("EXEC BuscarCliente ?", this::mapRowToCliente, id);
    }

    public void guardar(ClienteDTO cliente) {
        jdbcTemplate.update("EXEC GuardarCliente ?, ?, ?, ?, ?",
                cliente.getIdCliente(),
                cliente.getNombreCompleto(),
                cliente.getTelefono(),
                cliente.getFechaRegistro(),
                cliente.getFechaActualizacion());
    }

    public void actualizar(ClienteDTO cliente) {
        jdbcTemplate.update("EXEC ActualizarCliente ?, ?, ?, ?",
                cliente.getIdCliente(),
                cliente.getNombreCompleto(),
                cliente.getTelefono(),
                cliente.getFechaActualizacion());
    }
}
