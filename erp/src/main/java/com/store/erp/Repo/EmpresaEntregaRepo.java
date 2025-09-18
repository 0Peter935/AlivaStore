package com.store.erp.Repo;

import com.store.erp.Models.EmpresaEntregaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EmpresaEntregaRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private EmpresaEntregaDTO mapRowToEmpresaEntrega(ResultSet rs, int rowNum) throws SQLException {
        EmpresaEntregaDTO empresaEntrega = new EmpresaEntregaDTO();
        empresaEntrega.setIdEmpresaEntrega(rs.getInt("id_empresa_entrega"));
        empresaEntrega.setRazonSocial(rs.getString("razon_social"));
        empresaEntrega.setRuc(rs.getString("ruc"));
        empresaEntrega.setDireccionFiscal(rs.getString("direccion_fiscal"));

        return empresaEntrega;
    }

    public List<EmpresaEntregaDTO> listar() {
        return jdbcTemplate.query("EXEC ListarEmpresaEntrega", this::mapRowToEmpresaEntrega);
    }

    public EmpresaEntregaDTO buscarPorId(Integer id) {
        return jdbcTemplate.queryForObject("EXEC BuscarEmpresaEntrega ?", this::mapRowToEmpresaEntrega, id);
    }

    public void guardar(EmpresaEntregaDTO empresaEntrega) {
        jdbcTemplate.update("EXEC GuardarEmpresaEntrega ?, ?, ?, ?",
                empresaEntrega.getIdEmpresaEntrega(),
                empresaEntrega.getRazonSocial(),
                empresaEntrega.getRuc(),
                empresaEntrega.getDireccionFiscal());
    }

    public void actualizar(EmpresaEntregaDTO empresaEntrega) {
        jdbcTemplate.update("EXEC ActualizarEmpresaEntrega ?, ?, ?, ?",
                empresaEntrega.getIdEmpresaEntrega(),
                empresaEntrega.getRazonSocial(),
                empresaEntrega.getRuc(),
                empresaEntrega.getDireccionFiscal());
    }
}
