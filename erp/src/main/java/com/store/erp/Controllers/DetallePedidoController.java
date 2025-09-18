package com.store.erp.Controllers;

import com.store.erp.Models.DetallePedidoDTO;
import com.store.erp.Services.DetallePedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-pedidos")
public class DetallePedidoController {

    @Autowired
    private DetallePedidoService detallePedidoService;

    @GetMapping("/")
    public List<DetallePedidoDTO> listarDetallePedidos() {
        return detallePedidoService.listarDetallePedidos();
    }

    @GetMapping("/{id}")
    public DetallePedidoDTO buscarDetallePedidoPorId(@PathVariable Long id) {
        return detallePedidoService.buscarDetallePedidoPorId(id);
    }

    @PostMapping("/")
    public void guardarDetallePedido(@RequestBody DetallePedidoDTO detallePedido) {
        detallePedidoService.guardarDetallePedido(detallePedido);
    }

    @PutMapping("/{id}")
    public void actualizarDetallePedido(@PathVariable Long id, @RequestBody DetallePedidoDTO detallePedido) {
        detallePedido.setIdDetalleP(id);
        detallePedidoService.actualizarDetallePedido(detallePedido);
    }
}
