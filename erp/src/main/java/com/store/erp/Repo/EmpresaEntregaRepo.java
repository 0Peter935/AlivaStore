package com.store.erp.Repo;

import com.store.erp.Models.EmpresaEntregaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmpresaEntregaRepo extends Mappers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<EmpresaEntregaDTO> listarEmpresas() {
        return jdbcTemplate.query(
            "EXEC SP_EMPRESA_ENTREGA_LISTAR",
            (rs, _) -> Mappers.mapEmpresaEntrega(rs)
        );
    }

    public EmpresaEntregaDTO buscarPorId(int idEmpresaEntrega) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_EMPRESA_ENTREGA_BUSCAR_ID ?",
            (rs, _) -> Mappers.mapEmpresaEntrega(rs),
            idEmpresaEntrega
        );
    }

}
