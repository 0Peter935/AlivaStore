package com.store.erp.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/usuarios")
    public String mostrarUsuarios() {
        return "Usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuarioView() {
        return "NuevoUsuario";
    }

    @GetMapping("/productos")
    public String mostrarProductos() {
        return "Productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevoProductoView() {
        return "NuevoProducto";
    }

}
