package com.store.erp.Repo;

import com.store.erp.Models.ZonaEmpresaEntregaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ZonaEmpresaEntregaRepo extends Mappers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ZonaEmpresaEntregaDTO> listarZonas() {
        return jdbcTemplate.query(
            "EXEC SP_ZONA_EMPRESA_ENTREGA_LISTAR",
            (rs, _) -> Mappers.mapZona(rs)
        );
    }

    public ZonaEmpresaEntregaDTO buscarPorId(int id) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_ZONA_EMPRESA_ENTREGA_BUSCAR_ID ?",
            (rs, _) -> Mappers.mapZona(rs),
            id
        );
    }

}
