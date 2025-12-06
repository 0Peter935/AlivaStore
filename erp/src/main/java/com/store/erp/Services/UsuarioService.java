package com.store.erp.Services;

import com.store.erp.Models.UsuarioDTO;
import com.store.erp.Repo.UsuarioRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepo usuarioRepo;

    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepo.listarUsuarios();
    }

    public UsuarioDTO login(String usuario, String clave) {
        return usuarioRepo.login(usuario, clave);
    }

    public UsuarioDTO buscarUsuario(int idUsuario) {
        return usuarioRepo.obtenerUsuarioPorId(idUsuario);
    }

    public UsuarioDTO claveUsuario(int idUsuario) {
        return usuarioRepo.claveUsuario(idUsuario);
    }

    public String registrarUsuario(UsuarioDTO usuario) {
        return usuarioRepo.registrarUsuario(usuario);
    }

    public void actualizarUsuario(UsuarioDTO usuario) {
        usuarioRepo.actualizarUsuario(usuario);
    }

    public void cambiarEstado(int idUsuario, boolean estado) {
        usuarioRepo.cambiarEstado(idUsuario, estado);
    }

    public void actualizarPerfil(UsuarioDTO u) {
        usuarioRepo.actualizarPerfil(u);
    }

    public void cambiarPassword(Integer idUsuario, String nuevaClave, String confirmar) throws Exception {

        if (idUsuario == null) {
            throw new Exception("ID de usuario inválido");
        }

        if (nuevaClave == null || nuevaClave.isBlank()) {
            throw new Exception("La nueva contraseña no puede estar vacía");
        }

        if (!nuevaClave.equals(confirmar)) {
            throw new Exception("Las contraseñas no coinciden");
        }

        usuarioRepo.cambiarPassword(idUsuario, nuevaClave);
    }

}
