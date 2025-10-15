package com.store.erp.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.store.erp.Models.PedidoDTO;
import com.store.erp.Services.PedidoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public List<PedidoDTO> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPedidoPorId(@PathVariable("id") int idPedido) {
        PedidoDTO pedido = pedidoService.obtenerPedidoPorId(idPedido);
        if (pedido == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Pedido no encontrado"));
        return ResponseEntity.ok(pedido);
    }
}
