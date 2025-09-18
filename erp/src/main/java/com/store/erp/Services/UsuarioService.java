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
        return usuarioRepo.listar();
    }

    public UsuarioDTO buscarUsuarioPorId(Integer id) {
        return usuarioRepo.buscarPorId(id);
    }

    public void guardarUsuario(UsuarioDTO usuario) {
        usuarioRepo.guardar(usuario);
    }

    public void actualizarUsuario(UsuarioDTO usuario) {
        usuarioRepo.actualizar(usuario);
    }
}
