package com.store.erp.Repo;

import com.store.erp.Models.AlmacenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AlmacenRepo extends Mappers {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public List<AlmacenDTO> listarAlmacenes() {
        return jdbcTemplate.query("EXEC SP_ALMACEN_LISTAR", (rs, _) -> Mappers.mapAlmacen(rs));
    }

}
