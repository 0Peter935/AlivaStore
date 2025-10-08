package com.store.erp.Controllers;

import com.store.erp.Models.UsuarioDTO;
import com.store.erp.Services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}/buscar")
    public ResponseEntity<?> obtenerUsuario(@PathVariable("id") int id) {
        try {
            UsuarioDTO usuario = usuarioService.buscarUsuario(id);
            if (usuario == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado"));
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/clave")
    public ResponseEntity<?> obtenerClave(@PathVariable("id") int idUsuario) {
        try {

            UsuarioDTO usuario = usuarioService.claveUsuario(idUsuario);
            return ResponseEntity.ok(usuario);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "mensaje", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/actualizar")
    public ResponseEntity<String> actualizarUsuario(
            @PathVariable("id") int id,
            @RequestBody UsuarioDTO usuario) {

        usuario.setIdUsuario(id);
        usuarioService.actualizarUsuario(usuario);
        return ResponseEntity.ok("Usuario actualizado correctamente");
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable("id") int id, @RequestBody Map<String, Object> body) {
        try {
            boolean estado = Boolean.parseBoolean(body.get("estado").toString());
            usuarioService.cambiarEstado(id, estado);
            return ResponseEntity.ok(Map.of("message", "Estado actualizado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar estado", "details", e.getMessage()));
        }
    }
}
