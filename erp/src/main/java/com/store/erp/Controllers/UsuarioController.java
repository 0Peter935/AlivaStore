package com.store.erp.Controllers;

import com.store.erp.Models.UsuarioDTO;
import com.store.erp.Services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public UsuarioDTO buscarUsuarioPorId(@PathVariable Integer id) {
        return usuarioService.buscarUsuarioPorId(id);
    }

    @PostMapping("/")
    public void guardarUsuario(@RequestBody UsuarioDTO usuario) {
        usuarioService.guardarUsuario(usuario);
    }

    @PutMapping("/{id}")
    public void actualizarUsuario(@PathVariable Integer id, @RequestBody UsuarioDTO usuario) {
        usuario.setIdUsuario(id);
        usuarioService.actualizarUsuario(usuario);
    }
}
