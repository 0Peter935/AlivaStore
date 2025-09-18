package com.store.erp.Controllers;

import com.store.erp.Models.EstadoPedidoDTO;
import com.store.erp.Services.EstadoPedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estado-pedidos")
public class EstadoPedidoController {

    @Autowired
    private EstadoPedidoService estadoPedidoService;

    @GetMapping("/")
    public List<EstadoPedidoDTO> listarEstadoPedido() {
        return estadoPedidoService.listarEstadoPedido();
    }

    @GetMapping("/{id}")
    public EstadoPedidoDTO buscarEstadoPedidoPorId(@PathVariable Integer id) {
        return estadoPedidoService.buscarEstadoPedidoPorId(id);
    }

    @PostMapping("/")
    public void guardarEstadoPedido(@RequestBody EstadoPedidoDTO estadoPedido) {
        estadoPedidoService.guardarEstadoPedido(estadoPedido);
    }

    @PutMapping("/{id}")
    public void actualizarEstadoPedido(@PathVariable Integer id, @RequestBody EstadoPedidoDTO estadoPedido) {
        estadoPedido.setIdPedido(id);
        estadoPedidoService.actualizarEstadoPedido(estadoPedido);
    }
}
