package com.store.erp.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String mostrarLogin() {
        return "Login";
    }

    @GetMapping("/")
    public String mostrarHome() {
        return "Home";
    }

    @GetMapping("/usuarios/listaUsuarios")
    public String mostrarListaUsuarios() {
        return "ListaUsuarios";
    }

    @GetMapping("/productos/listaProductos")
    public String mostrarListaProductos() {
        return "ListaProductos";
    }

    @GetMapping("/clientes/listaClientes")
    public String mostrarListaClientes() {
        return "ListaClientes";
    }

    @GetMapping("/pedidos/listaPedidos")
    public String mostrarListaPedidos() {
        return "ListaPedidos";
    }

    @GetMapping("/pedidos/detallePedido")
    public String mostrarDetallePedido() {
        return "DetallePedido";
    }

    @GetMapping("/configuracion/sincronizar")
    public String mostrarSincronizar() {
        return "Sincronizar";
    }

}
