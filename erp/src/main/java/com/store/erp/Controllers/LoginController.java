package com.store.erp.Controllers;

import com.store.erp.Models.UsuarioDTO;
import com.store.erp.Services.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    public static class LoginRequest {
        public String usuario;
        public String clave;
    }

    @PostMapping()
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        UsuarioDTO user = usuarioService.login(request.usuario, request.clave);

        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Credenciales inválidas o usuario inactivo");
            return ResponseEntity.status(401).body(response);
        }

        return ResponseEntity.ok(user);
    }
}
