package com.store.erp.Repo;

import com.store.erp.Models.ClienteLogDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ClienteLogRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ClienteLogDTO mapRowToClienteLog(ResultSet rs, int rowNum) throws SQLException {
        ClienteLogDTO clienteLog = new ClienteLogDTO();
        clienteLog.setIdClienteLog(rs.getLong("id_cliente_log"));
        clienteLog.setIdCliente(rs.getInt("id_cliente"));
        clienteLog.setNombreCompleto(rs.getString("nombre_completo"));
        clienteLog.setTelefono(rs.getString("telefono"));
        clienteLog.setFechaActividad(rs.getTimestamp("fecha_actividad").toLocalDateTime());

        return clienteLog;
    }

    public List<ClienteLogDTO> listar() {
        return jdbcTemplate.query("EXEC ListarClientesLog", this::mapRowToClienteLog);
    }

    public ClienteLogDTO buscarPorId(Long id) {
        return jdbcTemplate.queryForObject("EXEC BuscarClienteLog ?", this::mapRowToClienteLog, id);
    }

    public void guardar(ClienteLogDTO clienteLog) {
        jdbcTemplate.update("EXEC GuardarClienteLog ?, ?, ?, ?",
                clienteLog.getIdClienteLog(),
                clienteLog.getIdCliente(),
                clienteLog.getNombreCompleto(),
                clienteLog.getTelefono(),
                clienteLog.getFechaActividad());
    }

    public void actualizar(ClienteLogDTO clienteLog) {
        jdbcTemplate.update("EXEC ActualizarClienteLog ?, ?, ?, ?",
                clienteLog.getIdClienteLog(),
                clienteLog.getIdCliente(),
                clienteLog.getNombreCompleto(),
                clienteLog.getTelefono(),
                clienteLog.getFechaActividad());
    }
}
