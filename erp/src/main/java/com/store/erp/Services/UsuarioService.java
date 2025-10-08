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

    public void actualizarUsuario(UsuarioDTO usuario) {
        usuarioRepo.actualizarUsuario(usuario);
    }

    public void cambiarEstado(int idUsuario, boolean estado) {
        usuarioRepo.cambiarEstado(idUsuario, estado);
    }
}
