package com.store.erp.Repo;

import com.store.erp.Models.ClienteDTO;
import com.store.erp.Models.ClienteLogDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClienteRepo extends Mappers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ClienteDTO> listarClientes() {
        return jdbcTemplate.query("EXEC SP_CLIENTE_LISTAR",
            (rs, _) -> mapCliente(rs)
        );
    }

    public List<ClienteLogDTO> listarLogsPorCliente(int idCliente) {
        return jdbcTemplate.query("EXEC SP_CLIENTE_LOG_LISTAR ?",
            (rs, _) -> mapClienteLog(rs),
            idCliente
        );
    }

    public ClienteDTO ObtenerPorCodigo(String cod_cliente) {
        return jdbcTemplate.queryForObject("EXEC SP_CLIENTE_BUSCAR_COD ?",
            (rs, _) -> mapCliente(rs),
            cod_cliente
        );
    }

    public int sincronizarClientes(ClienteDTO dto) {
        return jdbcTemplate.update(
            "EXEC SP_CLIENTE_SINCRO ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?",
            dto.getCodigoCliente(),
            dto.getNombres(),
            dto.getDni(),
            dto.getCorreo(),
            dto.getTelefono(),
            dto.getCanOrdenes(),
            dto.getDireccion(),
            dto.getCiudad(),
            dto.getProvincia(),
            dto.getPais(),
            dto.getFechaReg(),
            dto.getFechaAct()
        );
    }


}
