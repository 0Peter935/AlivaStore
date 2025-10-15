package com.store.erp.Repo;

import com.store.erp.Models.RolDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RolRepo extends Mappers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<RolDTO> listarRoles() {
        return jdbcTemplate.query("EXEC SP_ROL_LISTAR", (rs, _) -> Mappers.mapRol(rs));
    }

}
