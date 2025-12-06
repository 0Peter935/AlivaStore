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

    @GetMapping("/pedidos/admin/lista")
    public String vistaAdmin() {
        return "ListaPedidosAdmin";
    }

    @GetMapping("/pedidos/vendedor/lista")
    public String vistaVendedor() {
        return "ListaPedidosVendedor";
    }

    @GetMapping("/pedidos/logistica/lista")
    public String vistaLogistica() {
        return "ListaPedidosLogistica";
    }

    @GetMapping("/pedidos/despacho")
    public String mostrarDespachoPedido() {
        return "DespachoPedido";
    }

    @GetMapping("/pedidos/revision")
    public String mostrarRevisionPedido() {
        return "RevisionPedido";
    }

    @GetMapping("/pedidos/editar")
    public String mostrarEditarPedido() {
        return "DetallePedido";
    }

    @GetMapping("/configuracion/sincronizar")
    public String mostrarSincronizar() {
        return "Sincronizar";
    }

    @GetMapping("/reporteria/indicadores")
    public String mostrarReporteriaIndicadores() {
        return "Reporteria";
    }

    @GetMapping("/excel/merge-ui")
    public String excelUI() {
        return "merge-excel";
    }
}
