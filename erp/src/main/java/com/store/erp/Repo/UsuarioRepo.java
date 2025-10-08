package com.store.erp.Repo;

import com.store.erp.Models.UsuarioDTO;
import com.store.erp.Models.RolDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UsuarioRepo extends RepoUtils {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public UsuarioDTO login(String usuario, String clave) {
        try {
            return jdbcTemplate.queryForObject(
                "EXEC SP_USUARIO_LOGIN ?, ?",
                this::mapUsuario,
                usuario, clave
            );
        } catch (Exception e) {
            return null;
        }
    }

    public List<UsuarioDTO> listarUsuarios() {
        return jdbcTemplate.query(
            "EXEC SP_USUARIO_LISTAR",
            this::mapUsuario
        );
    }

    public UsuarioDTO obtenerUsuarioPorId(int idUsuario) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_USUARIO_BUSCAR_ID ?",
            this::mapUsuario, 
            idUsuario
        );
    }


    public UsuarioDTO claveUsuario(int idUsuario) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_USUARIO_CLAVE ?",
            this::mapUsuario,  
            idUsuario          
        );
    }

    public String registrarUsuario(UsuarioDTO usuario) {
        String sql = "EXEC SP_USUARIO_REGISTRAR ?, ?, ?, ?, ?, ?, ?, ?";
        jdbcTemplate.update(sql,
            usuario.getNombre(),
            usuario.getApPaterno(),
            usuario.getApMaterno(),
            usuario.getUsuario(),
            usuario.getCorreo(),
            usuario.getTelefono(),
            usuario.getClave(),
            usuario.isEstado(),
            usuario.getRol().getIdRol()
        );
        return "Usuario registrado correctamente";
    }


    public void actualizarUsuario(UsuarioDTO usuario) {
        String sql = "EXEC SP_USUARIO_ACTUALIZAR ?, ?, ?, ?, ?, ?, ?, ?";
        jdbcTemplate.update(
            sql,
            usuario.getIdUsuario(),
            usuario.getNombre(),
            usuario.getApPaterno(),
            usuario.getApMaterno(),
            usuario.getCorreo(),
            usuario.getTelefono(),
            usuario.getClave(),
            usuario.getRol().getIdRol()
        );
    }

    public void cambiarEstado(int idUsuario, boolean estado) {;
        jdbcTemplate.update(
            "EXEC SP_USUARIO_CAMBIAR_ESTADO ?, ?",
            idUsuario, estado
        );
    }

    private UsuarioDTO mapUsuario(ResultSet rs, int rowNum) throws SQLException {
        RolDTO rol = new RolDTO(
            hasColumn(rs, "ID_ROL") ? rs.getShort("ID_ROL") : 0,
            hasColumn(rs, "ROL_DESC") ? rs.getString("ROL_DESC")
                : hasColumn(rs, "ROL") ? rs.getString("ROL")
                : null
        );

        return new UsuarioDTO(
            rs.getInt("ID_USUARIO"),
            rs.getString("USUARIO"),
            hasColumn(rs, "CORREO") ? rs.getString("CORREO") : null,
            hasColumn(rs, "CLAVE") ? rs.getString("CLAVE") : "*********",
            hasColumn(rs, "TELEFONO") ? rs.getString("TELEFONO") : null,
            rol,
            hasColumn(rs, "ESTADO") ? rs.getBoolean("ESTADO") : false,
            hasColumn(rs, "NOMBRES") ? rs.getString("NOMBRES") : null,
            hasColumn(rs, "APELLIDO_PATERNO") ? rs.getString("APELLIDO_PATERNO") : null,
            hasColumn(rs, "APELLIDO_MATERNO") ? rs.getString("APELLIDO_MATERNO") : null,
            hasColumn(rs, "FECHA_REGISTRO") && rs.getDate("FECHA_REGISTRO") != null
                ? rs.getDate("FECHA_REGISTRO").toLocalDate()
                : null
        );
    }
}
