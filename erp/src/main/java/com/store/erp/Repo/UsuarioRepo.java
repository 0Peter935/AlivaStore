package com.store.erp.Repo;

import com.store.erp.Models.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UsuarioRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UsuarioDTO mapRowToUsuario(ResultSet rs, int rowNum) throws SQLException {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setUsuario(rs.getString("usuario"));
        usuario.setDescripcion(rs.getString("descripcion"));
        usuario.setClave(rs.getString("clave"));
        usuario.setIdRol(rs.getShort("id_rol"));
        usuario.setEstado(rs.getBoolean("estado"));

        return usuario;
    }

    public List<UsuarioDTO> listar() {
        return jdbcTemplate.query("EXEC ListarUsuarios", this::mapRowToUsuario);
    }

    public UsuarioDTO buscarPorId(Integer id) {
        return jdbcTemplate.queryForObject("EXEC BuscarUsuario ?", this::mapRowToUsuario, id);
    }

    public void guardar(UsuarioDTO usuario) {
        jdbcTemplate.update("EXEC GuardarUsuario ?, ?, ?, ?, ?",
                usuario.getUsuario(),
                usuario.getDescripcion(),
                usuario.getClave(),
                usuario.getIdRol(),
                usuario.getEstado());
    }

    public void actualizar(UsuarioDTO usuario) {
        jdbcTemplate.update("EXEC ActualizarUsuario ?, ?, ?, ?, ?, ?",
                usuario.getIdUsuario(),
                usuario.getUsuario(),
                usuario.getDescripcion(),
                usuario.getClave(),
                usuario.getIdRol(),
                usuario.getEstado());
    }
}
