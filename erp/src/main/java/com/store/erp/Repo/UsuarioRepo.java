package com.store.erp.Repo;

import com.store.erp.Models.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UsuarioRepo extends Mappers {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public UsuarioDTO login(String usuario, String clave) {
        try {
            return jdbcTemplate.queryForObject(
                "EXEC SP_USUARIO_LOGIN ?, ?",
                (rs, _) -> mapUsuario(rs),
                usuario, clave
            );
        } catch (Exception e) {
            return null;
        }
    }

    public List<UsuarioDTO> listarUsuarios() {
        return jdbcTemplate.query(
            "EXEC SP_USUARIO_LISTAR",
            (rs, _) -> mapUsuario(rs)
        );
    }

    public UsuarioDTO obtenerUsuarioPorId(int idUsuario) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_USUARIO_BUSCAR_ID ?",
            (rs, _) -> mapUsuario(rs),
            idUsuario
        );
    }


    public UsuarioDTO claveUsuario(int idUsuario) {
        return jdbcTemplate.queryForObject(
            "EXEC SP_USUARIO_CLAVE ?",
            (rs, _) -> mapUsuario(rs),
            idUsuario          
        );
    }

    public String registrarUsuario(UsuarioDTO usuario) {
        String sql = "EXEC SP_USUARIO_REGISTRAR ?, ?, ?, ?, ?, ?, ?, ?, ?";
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

    public void actualizarPerfil(UsuarioDTO u) {
        jdbcTemplate.update(
            "EXEC SP_USUARIO_ACTUALIZAR_PERFIL ?, ?, ?, ?, ?, ?",
            u.getIdUsuario(),
            u.getNombre(),
            u.getApPaterno(),
            u.getApMaterno(),
            u.getCorreo(),
            u.getTelefono()
        );
    }

}
