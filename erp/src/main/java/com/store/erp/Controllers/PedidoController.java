package com.store.erp.Controllers;

import com.store.erp.Models.PedidoDTO;
import com.store.erp.Services.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping("/")
    public List<PedidoDTO> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @GetMapping("/{id}")
    public PedidoDTO buscarPedidoPorId(@PathVariable Long id) {
        return pedidoService.buscarPedidoPorId(id);
    }

    @PostMapping("/")
    public void guardarPedido(@RequestBody PedidoDTO pedido) {
        pedidoService.guardarPedido(pedido);
    }

    @PutMapping("/{id}")
    public void actualizarPedido(@PathVariable Long id, @RequestBody PedidoDTO pedido) {
        pedido.setIdPedido(id);
        pedidoService.actualizarPedido(pedido);
    }
}
