package com.store.erp.Repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.store.erp.Models.UtilsDTO;

@Repository   
public class UtilsRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<UtilsDTO.ListarCiudad> listarCiudad() {
        return jdbcTemplate.query(
            "EXEC SP_UTILS_LISTAR_CIUDAD",
            (rs, rowNum) -> Mappers.mapListarCiudad(rs)
        );
    }
}
