package com.store.erp.Repo;

import com.store.erp.Models.TipoAlmacenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TipoAlmacenRepo extends Mappers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<TipoAlmacenDTO> listarTipos() {
        return jdbcTemplate.query(
            "EXEC SP_TIPO_ALMACEN_LISTAR",
            (rs, _) -> Mappers.mapTipoAlmacen(rs)
        );
    }

    public TipoAlmacenDTO buscarPorId(int idTipo) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_TIPO_ALMACEN_BUSCAR_ID ?",
            (rs, _) -> Mappers.mapTipoAlmacen(rs),
            idTipo
        );
    }

}
