package com.store.erp.Repo;

import com.store.erp.Models.RolDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RolRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<RolDTO> listarRoles() {
        return jdbcTemplate.query("SP_ROL_LISTAR", this::mapRol);
    }

    public RolDTO buscarRol(short idRol) {
        return jdbcTemplate.queryForObject(
                "exec BuscarRol ?",
                this::mapRol,
                idRol
        );
    }

    private RolDTO mapRol(ResultSet rs, int rowNum) throws SQLException {
        return new RolDTO(
                rs.getShort("ID_ROL"),
                rs.getString("DESCRIPCION")
        );
    }
}
