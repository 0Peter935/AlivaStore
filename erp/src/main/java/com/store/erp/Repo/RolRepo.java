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

    private RolDTO mapRowToRol(ResultSet rs, int rowNum) throws SQLException {
        RolDTO rol = new RolDTO();
        rol.setIdRol(rs.getShort("id_rol"));
        rol.setDescripcion(rs.getString("descripcion"));

        return rol;
    }

    public List<RolDTO> listar() {
        return jdbcTemplate.query("EXEC ListarRoles", this::mapRowToRol);
    }

    public RolDTO buscarPorId(Short id) {
        return jdbcTemplate.queryForObject("EXEC BuscarRol ?", this::mapRowToRol, id);
    }
}
